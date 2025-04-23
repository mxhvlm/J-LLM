package org.example.dbOutput;

import org.example.sourceImport.ModelExtractor;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

public class MethodExporter extends Exporter {

    public MethodExporter(ModelExtractor extractor, Neo4jService neo4jService) {
        super(extractor, neo4jService);
    }

    public void createMethodNodes() {
        for (CtMethod<?> ctMethod : _extractor.extractMethods()) {
            String declaringTypeName = safelyExtract(() -> ctMethod.getDeclaringType().getQualifiedName(), "ERROR");
            String methodSignature = safelyExtract(ctMethod::getSignature, "ERROR");
            String modifiers = safelyExtract(() -> ctMethod.getModifiers().toString(), "ERROR");
            String sourceCode = SourceCodePrinter.printMethodSource(ctMethod);

//            System.out.println("sourceCode: " + sourceCode + "\n\n");

            String javadoc = safelyExtract(ctMethod::getDocComment, "ERROR");
            String methodName = safelyExtract(ctMethod::getSimpleName, "ERROR");

            LOGGER.trace("Creating method node: " + declaringTypeName + "#" + methodSignature);
            _neo4jService.createMethodNode(methodName, declaringTypeName, methodSignature, modifiers, sourceCode, javadoc);

            LOGGER.trace("Linking " + declaringTypeName + " to method " + methodSignature);
            _neo4jService.linkTypeHasMethod(declaringTypeName, declaringTypeName + "#" + methodSignature);
        }
    }

    public void linkMethodSignatures() {
        for (CtMethod<?> ctMethod : _extractor.extractMethods()) {
            String declaringTypeName = ctMethod.getDeclaringType().getQualifiedName();
            String methodSignature = ctMethod.getSignature();

            // Handle return type
            CtTypeReference<?> returnTypeRef = ctMethod.getType();
            if (returnTypeRef != null) {
                linkMethodReturnType(declaringTypeName, methodSignature, returnTypeRef);
            }

            // Handle parameters
            for (CtParameter<?> parameter : ctMethod.getParameters()) {
                linkMethodParameter(declaringTypeName, methodSignature, parameter);
            }

            // Handle method -> field deps
            linkMethodFieldDependencies(ctMethod, declaringTypeName, methodSignature);

            // If you want to consider thrown exceptions as dependencies:
            for (CtTypeReference<?> thrownRef : ctMethod.getThrownTypes()) {
                // Store this in a dependency list for the DependenciesExporter later
                // e.g., record that declaringTypeName DEPENDS_ON thrownRef.getQualifiedName()
            }
        }
    }

    private void linkMethodReturnType(String declaringTypeName, String methodSignature, CtTypeReference<?> returnTypeRef) {
        String returnTypeName = returnTypeRef.getQualifiedName();
        LOGGER.trace("Linking method " + declaringTypeName + "#" + methodSignature + " returns " + returnTypeName);
        _neo4jService.linkMethodReturnsType(declaringTypeName + "#" + methodSignature, returnTypeName);

        // Handle generics in return type
        returnTypeRef.getActualTypeArguments().forEach(typeArgRef -> {
            String argId = _neo4jService.createTypeArgumentNode(typeArgRef);
            _neo4jService.linkMethodHasReturnTypeArgument(declaringTypeName + "#" + methodSignature, argId);

            if (typeArgRef != null && typeArgRef.getQualifiedName() != null) {
                _neo4jService.linkTypeArgumentOfType(argId, typeArgRef.getQualifiedName());
            }
        });
    }

    private void linkMethodParameter(String declaringTypeName, String methodSignature, CtParameter<?> parameter) {
        String paramName = parameter.getSimpleName();
        CtTypeReference<?> paramTypeRef = parameter.getType();

        String paramNodeId = _neo4jService.createParameterNode(declaringTypeName, methodSignature, paramName);
        LOGGER.trace("Linking method " + declaringTypeName + "#" + methodSignature + " has parameter " + paramName);
        _neo4jService.linkMethodHasParameter(declaringTypeName + "#" + methodSignature, paramNodeId);

        // Link parameter of_type
        String paramTypeName = paramTypeRef.getQualifiedName();
        _neo4jService.linkParameterOfType(paramNodeId, paramTypeName);

        // Handle generic parameters
        paramTypeRef.getActualTypeArguments().forEach(typeArgRef -> {
            String argId = _neo4jService.createTypeArgumentNode(typeArgRef);
            _neo4jService.linkParameterHasTypeArgument(paramNodeId, argId);
            if (typeArgRef != null && typeArgRef.getQualifiedName() != null) {
                _neo4jService.linkTypeArgumentOfType(argId, typeArgRef.getQualifiedName());
            }
        });
    }

    private void linkMethodFieldDependencies(CtMethod<?> ctMethod, String declaringTypeName, String methodSignature) {
        if (ctMethod.getBody() != null) {
            ctMethod.getBody().filterChildren(CtFieldAccess.class::isInstance).forEach(access -> {
                CtFieldAccess<?> fieldAccess = (CtFieldAccess<?>) access;
                CtFieldReference<?> fieldRef = fieldAccess.getVariable();
                String fieldDeclaringType = safelyExtract(() -> fieldRef.getDeclaringType().getQualifiedName(), "ERROR");
                String fieldName = safelyExtract(fieldRef::getSimpleName, "ERROR");
                String fieldFullName = fieldDeclaringType + "." + fieldName;

                LOGGER.trace("Linking method " + declaringTypeName + "#" + methodSignature + " to field " + fieldFullName);
                if (fieldRef.isStatic()) {
                    _neo4jService.linkMethodDependsOnField(declaringTypeName + "#" + methodSignature, fieldFullName);
                } else {
                    _neo4jService.linkMethodDependsOnField(declaringTypeName + "#" + methodSignature, fieldDeclaringType + "." + fieldName);
                }
            });
        }
    }

    @Override
    public void export() {
        LOGGER.info("MethodExporter: Creating method nodes...");
        createMethodNodes();

        LOGGER.info("MethodExporter: Linking method return types and parameters...");
        linkMethodSignatures();

        LOGGER.info("MethodExporter: Export complete.");
    }

    private <T> T safelyExtract(Extractor<T> extractor, T fallback) {
        try {
            return extractor.extract();
        } catch (Exception e) {
            LOGGER.error("Error during extraction: " + e.getMessage());
            return fallback;
        }
    }

    @FunctionalInterface
    private interface Extractor<T> {
        T extract() throws Exception;
    }
}
