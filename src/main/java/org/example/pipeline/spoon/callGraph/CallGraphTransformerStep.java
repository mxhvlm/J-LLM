package org.example.pipeline.spoon.callGraph;

import org.example.data.Neo4JLinkObject;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;

import java.util.List;
import java.util.stream.Stream;

import static org.example.pipeline.spoon.SafeExtractor.safelyExtract;

public class CallGraphTransformerStep implements PipelineStep<List<CtMethod<?>>, List<Neo4JLinkObject>> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CallGraphTransformerStep.class);

    private List<Neo4JLinkObject> linkMethodCalls(List<CtMethod<?>> methods) {
        return methods.stream().flatMap(method -> {
            String callerMethodId = safelyExtract(() -> method.getDeclaringType().getQualifiedName() + "#" + method.getSignature(), "ERROR", LOGGER);

            if (method.getBody() != null) {
                return method.getBody().filterChildren(CtInvocation.class::isInstance).map(invocation -> {
                    CtInvocation<?> methodCall = (CtInvocation<?>) invocation;
                    CtExecutableReference<?> executableRef = methodCall.getExecutable();
                    String calleeMethodId = safelyExtract(() -> executableRef.getDeclaringType().getQualifiedName() + "#" + executableRef.getSignature(), "ERROR", LOGGER);

                    LOGGER.trace("Linking method " + callerMethodId + " calls " + calleeMethodId);

                    return Neo4JLinkObject.Builder.create()
                            .withLabel("CALLS")
                            .betweenLabels("Method")
                            .betweenProps("id")
                            .parentValue(callerMethodId)
                            .childValue(calleeMethodId)
                            .build();
                }).list(Neo4JLinkObject.class).stream();
            } else {
                return Stream.empty();
            }
        }).toList();
    }

    @Override
    public List<Neo4JLinkObject> process(List<CtMethod<?>> input) {
        return linkMethodCalls(input);
    }
}
