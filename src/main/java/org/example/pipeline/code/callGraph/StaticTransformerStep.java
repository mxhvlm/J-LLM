package org.example.pipeline.code.callGraph;

import org.apache.commons.lang3.tuple.Pair;
import org.example.datamodel.impl.code.CodeModel;
import org.example.datamodel.impl.neo4j.Neo4JLink;
import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.stream.Stream;

import static org.example.pipeline.code.SafeExtractor.safelyExtract;

public class StaticTransformerStep implements IPipelineStep<
        Pair<CodeModel, Stream<? extends CtExecutableReference<?>>>, Stream<Neo4JLink>> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StaticTransformerStep.class);

    @Override
    public Stream<Neo4JLink> process(Pair<CodeModel, Stream<? extends CtExecutableReference<?>>> input) {
        LOGGER.info("StaticCallGraphTransformer: Linking method calls...");
        return input
                .getRight()
                .filter(executableRef -> {
                    // Filter out static calls to constructors
                    if (executableRef.getDeclaringType() == null) {
                        LOGGER.info(
                                "Executable reference {} has no declaring type. Skipping.",
                                executableRef.getSignature());
                        return false;
                    }
                    CtTypeReference<?> declaringType = executableRef.getDeclaringType();
                    return declaringType != null && !declaringType.isPrimitive();
                })
                .map(executableRef -> {
                    CtTypeReference<?> declaringType = executableRef.getDeclaringType();
                    // Callee
                    String calleeMethodId = declaringType.getQualifiedName() + "#" + executableRef.getSignature();

                    // Try to find the calling method by walking up from the reference's parent
                    String callerMethodId = safelyExtract(() -> {
                        if (executableRef.getParent() == null) {
                            throw new IllegalStateException("No parent for executable reference.");
                        }
                        return executableRef.getParent()
                                .getParent(CtMethod.class) // traverse up the tree to find the enclosing method
                                .getDeclaringType().getQualifiedName()
                                + "#"
                                + executableRef.getParent().getParent(CtMethod.class).getSignature();
                    }, "ERROR#ERROR", LOGGER);

                    LOGGER.trace("Linking method {} calls {}", callerMethodId, calleeMethodId);

                    return Neo4JLink.Builder.create()
                            .withLabel("CALLS_STATIC")
                            .betweenLabels("Method")
                            .betweenProps("id")
                            .parentValue(callerMethodId)
                            .childValue(calleeMethodId)
                            .build();
                });
    }

    public sealed interface IStaticCallGraphOutput permits StaticTransformerStep.StaticCall {
    }

    record StaticCall(Neo4JLink object) implements IStaticCallGraphOutput {
    }
}
