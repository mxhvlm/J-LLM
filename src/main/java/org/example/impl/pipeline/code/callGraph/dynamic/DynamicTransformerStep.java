package org.example.impl.pipeline.code.callGraph.dynamic;

import org.apache.commons.lang3.tuple.Pair;
import org.example.impl.datamodel.impl.neo4j.Neo4JLink;
import org.example.impl.datamodel.impl.neo4j.Neo4JLink.Builder;
import org.example.impl.pipeline.IPipelineStep;
import org.slf4j.Logger;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicTransformerStep implements IPipelineStep<
        Pair<Stream<CtTypeReference<?>>, Stream<? extends CtExecutableReference<?>>>,
        Stream<Neo4JLink>> {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DynamicTransformerStep.class);
    private final Config _config;

    public DynamicTransformerStep(Config config) {
        _config = config;
    }

    @Override
    public Stream<Neo4JLink> process(
            Pair<Stream<CtTypeReference<?>>, Stream<? extends CtExecutableReference<?>>> input) {

        LOG.info("DynamicCallTransformer: Linking method calls...");

        // 1) Materialize instantiated types
        LOG.info("DynamicCallTransformer: Materializing instantiated types...");
        Set<CtTypeReference<?>> instTypes = input.getLeft()
                .collect(Collectors.toSet());

        // 2) Build supertype -> { instTypes } index
        LOG.info("DynamicCallTransformer: Building subtype index...");
        Map<CtTypeReference<?>, Set<CtTypeReference<?>>> subtypeIndex = new HashMap<>();
        for (CtTypeReference<?> t : instTypes) {
            // include itself
            subtypeIndex.computeIfAbsent(t, k -> new HashSet<>()).add(t);

            // BFS up the hierarchy
            Deque<CtTypeReference<?>> work = new ArrayDeque<>();
            CtTypeReference<?> sc = t.getSuperclass();
            if (sc != null) work.add(sc);
            work.addAll(t.getSuperInterfaces());
            while (!work.isEmpty()) {
                CtTypeReference<?> sup = work.removeFirst();
                subtypeIndex.computeIfAbsent(sup, k -> new HashSet<>()).add(t);
                CtTypeReference<?> supSc = sup.getSuperclass();
                if (supSc != null) work.add(supSc);
                work.addAll(sup.getSuperInterfaces());
            }
        }

        // 3) Materialize all executable-refs
        LOG.info("DynamicCallTransformer: Materializing all executable references...");
        List<CtExecutableReference<?>> refs = input.getRight()
                .collect(Collectors.toList());

        // 4) Prepare caches for caller-ID and target-ID strings
        Map<CtExecutableReference<?>, String> callerIdCache = new IdentityHashMap<>();
        Map<CtExecutableReference<?>, String> targetIdCache = new IdentityHashMap<>();

        List<Neo4JLink> out = new ArrayList<>(refs.size() * 2);

        // 5) Main loop
        LOG.info("DynamicCallTransformer: Processing {} executable references...", refs.size());
        for (int i = 0; i < refs.size(); i++) {
            if (i % 1000 == 0) {
                LOG.info("DynamicCallTransformer: Processed {} of {} ({}%} executable references...",
                        i, refs.size(), (i * 100) / (float) refs.size());
            }

            CtExecutableReference<?> ref = refs.get(i);
            // skip static calls & constructors
            if (ref.isStatic()
                    || CtExecutableReference.CONSTRUCTOR_NAME.equals(ref.getSimpleName())) {
                continue;
            }
            // skip private methods
            CtExecutable<?> decl = ref.getDeclaration();
            if (decl instanceof CtMethod<?> && ((CtMethod<?>) decl)
                    .hasModifier(ModifierKind.PRIVATE)) {
                continue;
            }
            // scope filter
            String declType = safelyExtractCaller(ref);
            boolean inScope = !declType.equals("ERROR#ERROR") && _config.scope().stream()
                    .anyMatch(declType::startsWith);
            if (!inScope) continue;

            // extract caller ID (cached)
            String callerId = callerIdCache.computeIfAbsent(ref, this::safelyExtractCaller);

            // find the invocation and its receiver type
            CtInvocation<?> inv = ref.getParent(CtInvocation.class);
            if (inv == null || inv.getTarget() == null) continue;
            CtTypeReference<?> recvType = inv.getTarget().getType();

            // lookup all instantiated subtypes in O(1)
            Set<CtTypeReference<?>> cands = subtypeIndex
                    .getOrDefault(recvType, Collections.emptySet());

            for (CtTypeReference<?> cand : cands) {
                CtExecutableReference<?> overrideRef;
                try {
                    overrideRef = ref.getOverridingExecutable(cand);
                } catch (NullPointerException npe) {
                    continue; // Spoon resolution failure
                }
                CtExecutableReference<?> targetRef = (overrideRef != null ? overrideRef : ref);

                // build/cache target ID
                String targetId = targetIdCache.computeIfAbsent(targetRef, tr ->
                        tr.getDeclaringType().getQualifiedName()
                                + "#" + tr.getSignature());

                out.add(Builder.create()
                        .withLabel("CALLS_DYNAMIC")
                        .betweenLabels("Method")
                        .betweenProps("id")
                        .parentValue(callerId)
                        .childValue(targetId)
                        .build());
            }
        }

        // 6) Deduplicate and return
        return out.stream().distinct();
    }

    private String safelyExtractCaller(CtExecutableReference<?> ref) {
        try {
            CtMethod<?> m = ref.getParent().getParent(CtMethod.class);
            if (m != null) {
                CtTypeReference<?> declaringTypeRef = m.getParent(spoon.reflect.declaration.CtType.class).getReference();
                return declaringTypeRef.getQualifiedName() + "#" + m.getSignature();
            }
            // Fallback to constructors or static initializers
            CtExecutable<?> exec = ref.getParent().getParent(CtExecutable.class);
            if (exec != null) {
                CtTypeReference<?> declaringTypeRef = exec.getParent(spoon.reflect.declaration.CtType.class).getReference();
                return declaringTypeRef.getQualifiedName() + "#" + exec.getSignature();
            }
        } catch (Exception e) {
            // fallback or log
        }
        return "ERROR#ERROR";
    }
}
