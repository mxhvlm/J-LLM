package org.example.dbOutput;

import org.example.sourceImport.ModelExtractor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;

import java.util.List;
import java.util.Set;

public class CallGraphExporter extends Exporter {

    public CallGraphExporter(ModelExtractor extractor, Neo4jService neo4jService) {
        super(extractor, neo4jService);
    }

    private void linkMethodCalls() {
        List<CtMethod<?>> methods = _extractor.extractMethods();

        for (CtMethod<?> method : methods) {
            String callerMethodId = safelyExtract(() -> method.getDeclaringType().getQualifiedName() + "#" + method.getSignature(), "ERROR");

            if (method.getBody() != null) {
                method.getBody().filterChildren(CtInvocation.class::isInstance).forEach(invocation -> {
                    CtInvocation<?> methodCall = (CtInvocation<?>) invocation;
                    CtExecutableReference<?> executableRef = methodCall.getExecutable();
                    String calleeMethodId = safelyExtract(() -> executableRef.getDeclaringType().getQualifiedName() + "#" + executableRef.getSignature(), "ERROR");

                    LOGGER.trace("Linking method " + callerMethodId + " calls " + calleeMethodId);
                    _neo4jService.linkMethodCallsMethod(callerMethodId, calleeMethodId);
                });
            }
        }
    }

    private <T> T safelyExtract(Extractor<T> extractor, T fallback) {
        try {
            return extractor.extract();
        } catch (Exception e) {
            LOGGER.error("Error during extraction: " + e.getMessage());
            return fallback;
        }
    }

    @Override
    public void export() {
        LOGGER.info("CallGraphExporter: Exporting method call graph...");
        linkMethodCalls();
        LOGGER.info("CallGraphExporter: Export complete.");
    }

    @FunctionalInterface
    private interface Extractor<T> {
        T extract() throws Exception;
    }
}
