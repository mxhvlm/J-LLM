package org.example.impl.pipeline.code.method;

import org.example.impl.datamodel.api.code.IQualifiedName;
import org.example.impl.datamodel.api.code.wrapper.IMethod;
import org.example.impl.datamodel.api.code.wrapper.INamedElement;
import org.example.impl.datamodel.api.code.wrapper.IParameter;
import org.example.impl.datamodel.api.code.wrapper.IType;
import org.example.impl.datamodel.impl.neo4j.Neo4JLink;
import org.example.impl.datamodel.impl.neo4j.Neo4jMethod;
import org.example.impl.datamodel.impl.neo4j.Neo4jParam;
import org.example.impl.datamodel.impl.neo4j.Neo4jTypeArgument;
import org.example.impl.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;


public class TransformerStep implements IPipelineStep<Stream<IMethod>, Stream<TransformerStep.IMethodTransformerOutput>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerStep.class);

    private Stream<IMethodTransformerOutput> createMethodObjects(Stream<IMethod> methods) {
        return methods
                .map(m -> {
                    String declaringTypeName = m.getDeclaringType()
                            .map(IType::getName)
                            .map(IQualifiedName::getQualifiedName)
                            .orElse("ERROR");
                    String methodSignature = m.getSignature();
                    String modifiers = String.join(",", m.getModifiers());
                    String sourceCode = m.getBody();

                    String javadoc = m.getDocumentation();
                    String methodName = m.getName().getSimpleName();

                    LOGGER.trace("Creating method node: " + declaringTypeName + "#" + methodSignature);
                    return new Neo4jMethod(methodName, declaringTypeName, methodSignature, modifiers, sourceCode, javadoc);
                }).map(MethodNode::new);
    }

    private List<IMethodTransformerOutput> createLinks(Stream<IMethod> methods) {
        List<IMethodTransformerOutput> links = new ArrayList<>();

        methods.forEach(m -> {
            String declaringTypeName = m.getName().getQualifiedName();
            String methodSignature = m.getSignature();

            // Link method to its declaring type
            links.add(createParentLink(m));

            // Link overrides
            links.addAll(createOverrideLinks(m));

            // Link method to its return type
            try {
                links.addAll(createReturnTypeLink(m));
            } catch (Exception e) {
                LOGGER.warn("Error creating return type link: " + e.getMessage());
            }

            // Link method to its parameters
            m.getParameters().forEach(parameter -> {
                links.addAll(createParameterLinks(m, parameter));
            });

            // Link method to its field dependencies
            links.addAll(createFieldDepLinks(m));
        });
        return links;
    }

    private IMethodTransformerOutput createParentLink(IMethod method) {
        String declaringTypeName = method.getDeclaringType()
                .map(IType::getName)
                .map(IQualifiedName::getQualifiedName)
                .orElse("ERROR");

        String methodSignature = method.getSignature();

        LOGGER.trace("Linking " + declaringTypeName + " to method " + methodSignature);
        return new MethodLinkNode(Neo4JLink.Builder.create()
                .withLabel("HAS_METHOD")
                .parentLabel("Type")
                .parentProp("name")
                .parentValue(declaringTypeName)
                .childLabel("Method")
                .childProp("id")
                .childValue(declaringTypeName + "#" + methodSignature)
                .build());
    }

    private List<IMethodTransformerOutput> createOverrideLinks(IMethod method) {
        List<IMethodTransformerOutput> links = new ArrayList<>();

        method.getDeclaringType().ifPresent(declaringType -> {
            String subTypeName = declaringType.getName().getQualifiedName();
            String subMethodSignature = method.getSignature();
            String subMethodId = subTypeName + "#" + subMethodSignature;

            // Set to avoid duplicates by QualifiedName
            Set<String> visitedTypeNames = new HashSet<>();
            Deque<IType> toVisit = new ArrayDeque<>();

            // Add direct superclass and interfaces
            declaringType.getSuperClass().ifPresent(toVisit::add);
            toVisit.addAll(declaringType.getInterfaces());

            while (!toVisit.isEmpty()) {
                IType current = toVisit.poll();
                String currentTypeName = current.getName().getQualifiedName();

                if (!visitedTypeNames.add(currentTypeName)) {
                    continue; // skip already visited types
                }

                current.getMethods().stream()
                        .filter(superMethod -> superMethod.getSignature().equals(subMethodSignature))
                        .forEach(superMethod -> {
                            String superMethodId = superMethod.getName().getQualifiedName();

                            links.add(new MethodLinkNode(
                                    Neo4JLink.Builder.create()
                                            .withLabel("OVERRIDES")
                                            .betweenLabels("Method")
                                            .betweenProps("id")
                                            .parentValue(subMethodId)
                                            .childValue(superMethodId)
                                            .build()
                            ));
                        });

                // Queue next supertypes
                current.getSuperClass().ifPresent(toVisit::add);
                toVisit.addAll(current.getInterfaces());
            }
        });

        return links;
    }

    private List<IMethodTransformerOutput> createReturnTypeLink(IMethod method) throws Exception {
        String declaringTypeName = method.getDeclaringType()
                .map(IType::getName)
                .map(IQualifiedName::getQualifiedName)
                .orElse("ERROR");

        String methodSignature = method.getSignature();

        String returnTypeName = method.getReturnType()
                .map(IType::getName)
                .map(IQualifiedName::getQualifiedName)
                .orElseThrow(() -> new Exception("Return type for method " + declaringTypeName + "#" + methodSignature + " is null"));

        LOGGER.trace("Linking method {}#{} returns {}", declaringTypeName, methodSignature, returnTypeName);

        return List.of(
                new MethodLinkNode(
                        Neo4JLink.Builder.create()
                                .withLabel("RETURNS")
                                .parentLabel("Method")
                                .parentProp("id")
                                .parentValue(declaringTypeName + "#" + methodSignature)
                                .childLabel("Type")
                                .childProp("name")
                                .childValue(returnTypeName)
                                .build()
                )
        );
    }

    private List<IMethodTransformerOutput> createParameterLinks(IMethod method, IParameter parameter) {
        String declaringTypeName = method.getDeclaringType()
                .map(IType::getName)
                .map(IQualifiedName::getQualifiedName)
                .orElse("ERROR");

        String methodSignature = method.getSignature();
        String paramName = parameter.getName().getSimpleName();
        String paramId = parameter.getName().getQualifiedName();

        LOGGER.trace("Linking method {}#{} has parameter {}", declaringTypeName, methodSignature, paramName);

        Neo4JLink paramMethodLink = Neo4JLink.Builder.create()
                .withLabel("HAS_PARAMETER")
                .parentLabel("Method")
                .parentProp("id")
                .parentValue(declaringTypeName + "#" + methodSignature)
                .childLabel("Parameter")
                .childProp("id")
                .childValue(paramId)
                .build();

        String paramTypeName = parameter.getType()
                .map(IType::getName)
                .map(IQualifiedName::getQualifiedName)
                .orElse("ERROR");

        Neo4JLink paramTypeLink = Neo4JLink.Builder.create()
                .withLabel("OF_TYPE")
                .parentLabel("Parameter")
                .parentProp("id")
                .parentValue(paramId)
                .childLabel("Type")
                .childProp("name")
                .childValue(paramTypeName)
                .build();

        Neo4jParam paramObject = new Neo4jParam(paramId, paramName);

        return List.of(
                new MethodParamObject(paramObject),
                new MethodLinkNode(paramMethodLink),
                new MethodLinkNode(paramTypeLink)
        );
    }

    private List<IMethodTransformerOutput> createFieldDepLinks(IMethod method) {
        String declaringTypeName = method.getDeclaringType()
                .map(IType::getName)
                .map(IQualifiedName::getQualifiedName)
                .orElse("ERROR");

        String methodSignature = method.getSignature();

        Map<String, MethodLinkNode> links = new HashMap<>();

        method.getReferencedFields().stream().filter(Objects::isNull).forEach(field -> {
            LOGGER.warn("Method {}#{} has null referenced field!. Non-Null references are: {}", declaringTypeName, methodSignature, method.getReferencedFields().stream().filter(Objects::nonNull).map(INamedElement::getName).toList());
        });
        method.getReferencedFields().stream().filter(Objects::nonNull).forEach(field -> {
            String fieldNameQualified = field.getName().getQualifiedName();
            LOGGER.trace("Linking method {}#{} to field {}", declaringTypeName, methodSignature, fieldNameQualified);
            links.computeIfAbsent(fieldNameQualified, key ->
                    new MethodLinkNode(
                            Neo4JLink.Builder.create()
                                    .withLabel("DEPENDS_ON")
                                    .betweenLabels("Method", "Field")
                                    .betweenProps("id")
                                    .parentValue(method.getName().getQualifiedName())
                                    .childValue(fieldNameQualified)
                                    .build()
                    )
            );
        });

        return new ArrayList<>(links.values());
    }

    @Override
    public Stream<IMethodTransformerOutput> process(Stream<IMethod> input) {
        LOGGER.info("MethodExporter: Processing methods...");
        List<IMethod> methods = input.toList();  // or input.collect(Collectors.toList());
        createMethodObjects(methods.stream());
        createLinks(methods.stream());
        return Stream.concat(
                createMethodObjects(methods.stream()),
                createLinks(methods.stream()).stream()
        );
    }

    public sealed interface IMethodTransformerOutput
            permits MethodLinkNode, MethodNode, MethodTypeArgumentNode, MethodParamObject {
    }

    record MethodNode(Neo4jMethod object) implements IMethodTransformerOutput {
    }

    record MethodParamObject(Neo4jParam object) implements IMethodTransformerOutput {
    }

    record MethodLinkNode(Neo4JLink link) implements IMethodTransformerOutput {
    }

    record MethodTypeArgumentNode(Neo4jTypeArgument object) implements IMethodTransformerOutput {
    }
}
