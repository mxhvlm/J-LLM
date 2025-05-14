package org.example.pipeline.spoon.method;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.example.data.Neo4JLinkObject;
import org.example.data.Neo4jMethodObject;
import org.example.data.Neo4jParamObject;
import org.example.data.Neo4jTypeArgumentNode;
import org.example.dbOutput.SourceCodePrinter;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.LinkedList;
import java.util.List;


public class MethodTransformerStep implements PipelineStep<List<CtMethod<?>>, MethodTransformResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodTransformerStep.class);

    private List<Neo4jMethodObject> createMethodObjects(List<CtMethod<?>> methods) {
        return methods.stream().map(ctMethod -> {
            String declaringTypeName = safelyExtract(() -> ctMethod.getDeclaringType().getQualifiedName(), "ERROR");
            String methodSignature = safelyExtract(ctMethod::getSignature, "ERROR");
            String modifiers = safelyExtract(() -> ctMethod.getModifiers().toString(), "ERROR");
            String sourceCode = SourceCodePrinter.printMethodSource(ctMethod);

            String javadoc = safelyExtract(ctMethod::getDocComment, "ERROR");
            String methodName = safelyExtract(ctMethod::getSimpleName, "ERROR");

            LOGGER.trace("Creating method node: " + declaringTypeName + "#" + methodSignature);
            return new Neo4jMethodObject(methodName, declaringTypeName, methodSignature, modifiers, sourceCode, javadoc);
        }).toList();
    }

    private List<Neo4JLinkObject> linkTypeHasMethod(List<CtMethod<?>> methods) {
        return methods.stream().map(ctMethod -> {
            String declaringTypeName = ctMethod.getDeclaringType().getQualifiedName();
            String methodSignature = ctMethod.getSignature();

            LOGGER.trace("Linking " + declaringTypeName + " to method " + methodSignature);
            return Neo4JLinkObject.Builder.create()
                    .withLabel("HAS_METHOD")
                    .parentLabel("Type")
                    .parentProp("name")
                    .parentValue(declaringTypeName)
                    .childLabel("Method")
                    .childProp("id")
                    .childValue(declaringTypeName + "#" + methodSignature)
                    .build();
        }).toList();
    }
//
//    public void linkMethodSignatures(List<CtMethod<?>> methods) {
//
//        for (CtMethod<?> ctMethod : methods) {
//            String declaringTypeName = ctMethod.getDeclaringType().getQualifiedName();
//            String methodSignature = ctMethod.getSignature();
//
//            // Handle return type
//            CtTypeReference<?> returnTypeRef = ctMethod.getType();
//            if (returnTypeRef != null) {
//                linkMethodReturnType(declaringTypeName, methodSignature, returnTypeRef);
//            }
//
//            // Handle parameters
//            for (CtParameter<?> parameter : ctMethod.getParameters()) {
//                linkMethodParameter(declaringTypeName, methodSignature, parameter);
//            }
//
//            // Handle method -> field deps
//            linkMethodFieldDependencies(ctMethod, declaringTypeName, methodSignature);
//
//            // If you want to consider thrown exceptions as dependencies:
//            for (CtTypeReference<?> thrownRef : ctMethod.getThrownTypes()) {
//                // Store this in a dependency list for the DependenciesExporter later
//                // e.g., record that declaringTypeName DEPENDS_ON thrownRef.getQualifiedName()
//            }
//        }
//    }

    private Pair<List<Neo4jTypeArgumentNode>, List<Neo4JLinkObject>> linkMethodReturnType(String declaringTypeName, String methodSignature, CtTypeReference<?> returnTypeRef) throws Exception {
        if (returnTypeRef == null) {
            throw new Exception("Return type for method " + declaringTypeName + "#" + methodSignature + " is null");
        }
        String returnTypeName = safelyExtract(returnTypeRef::getQualifiedName, "ERROR");
        LOGGER.trace("Linking method " + declaringTypeName + "#" + methodSignature + " returns " + returnTypeName);
        List<Neo4JLinkObject> links = new LinkedList<>();
        links.add(Neo4JLinkObject.Builder.create()
                .withLabel("RETURNS")
                .parentLabel("Method")
                .parentProp("id")
                .parentValue(declaringTypeName + "#" + methodSignature)
                .childLabel("Type")
                .childProp("name")
                .childValue(returnTypeName)
                .build());
        return Pair.of(List.of(), links);
//        // Handle generics in return type
//        returnTypeRef.getActualTypeArguments().forEach(typeArgRef -> {
//            String argId = _neo4jService.createTypeArgumentNode(typeArgRef);
//            _neo4jService.linkMethodHasReturnTypeArgument(declaringTypeName + "#" + methodSignature, argId);
//
//            if (typeArgRef != null && typeArgRef.getQualifiedName() != null) {
//                _neo4jService.linkTypeArgumentOfType(argId, typeArgRef.getQualifiedName());
//            }
//        });
    }

    private Triple<Neo4jParamObject, List<Neo4jTypeArgumentNode>, List<Neo4JLinkObject>> linkMethodParameter(String declaringTypeName, String methodSignature, CtParameter<?> parameter) {
        String paramName = parameter.getSimpleName();
        CtTypeReference<?> paramTypeRef = parameter.getType();
        String paramId = declaringTypeName + "#" + methodSignature + ":" + paramName;
        List<Neo4JLinkObject> links = new LinkedList<>();
//        String paramNodeId = _neo4jService.createParameterNode(declaringTypeName, methodSignature, paramName);
        LOGGER.trace("Linking method " + declaringTypeName + "#" + methodSignature + " has parameter " + paramName);
//        _neo4jService.linkMethodHasParameter(declaringTypeName + "#" + methodSignature, paramNodeId);
        links.add(Neo4JLinkObject.Builder.create()
                .withLabel("HAS_PARAMETER")
                .parentLabel("Method")
                .parentProp("id")
                .parentValue(declaringTypeName + "#" + methodSignature)
                .childLabel("Parameter")
                .childProp("id")
                .childValue(paramId)
                .build());
        // Link parameter of_type
        String paramTypeName = paramTypeRef.getQualifiedName();
//        _neo4jService.linkParameterOfType(paramNodeId, paramTypeName);
        links.add(Neo4JLinkObject.Builder.create()
                .withLabel("OF_TYPE")
                .parentLabel("Parameter")
                .parentProp("id")
                .parentValue(paramId)
                .childLabel("Type")
                .childProp("name")
                .childValue(paramTypeName)
                .build());
//        // Handle generic parameters
//        paramTypeRef.getActualTypeArguments().forEach(typeArgRef -> {
//            String argId = _neo4jService.createTypeArgumentNode(typeArgRef);
//            _neo4jService.linkParameterHasTypeArgument(paramNodeId, argId);
//            if (typeArgRef != null && typeArgRef.getQualifiedName() != null) {
//                _neo4jService.linkTypeArgumentOfType(argId, typeArgRef.getQualifiedName());
//            }
//        });

        return Triple.of(
                new Neo4jParamObject(paramId, paramName),
                List.of(),
                links
        );
    }

    private List<Neo4JLinkObject> linkMethodFieldDependencies(CtMethod<?> ctMethod, String declaringTypeName, String methodSignature) {
        if (ctMethod.getBody() != null) {
            return ctMethod.getBody().filterChildren(CtFieldAccess.class::isInstance).map(access -> {
                CtFieldAccess<?> fieldAccess = (CtFieldAccess<?>) access;
                CtFieldReference<?> fieldRef = fieldAccess.getVariable();
                String fieldDeclaringType = safelyExtract(() -> fieldRef.getDeclaringType().getQualifiedName(), "ERROR");
                String fieldName = safelyExtract(fieldRef::getSimpleName, "ERROR");
                String fieldFullName = fieldDeclaringType + "." + fieldName;

                LOGGER.trace("Linking method " + declaringTypeName + "#" + methodSignature + " to field " + fieldFullName);

                return Neo4JLinkObject.Builder.create()
                        .withLabel("DEPENDS_ON")
                        .betweenLabels("Method", "Field")
                        .betweenProps("id")
                        .parentValue(declaringTypeName + "#" + methodSignature)
                        .childValue(fieldRef.isStatic() ? fieldFullName : fieldDeclaringType + "." + fieldName)
                        .build();
//                if (fieldRef.isStatic()) {
//
//
//                    _neo4jService.linkMethodDependsOnField(declaringTypeName + "#" + methodSignature, fieldFullName);
//                } else {
//                    links.add(Neo4JLinkObject.Builder.create()
//                            .withLabel("DEPENDS_ON")
//                            .betweenLabels("Method", "Field")
//                            .betweenProps("id")
//                            .parentValue(declaringTypeName + "#" + methodSignature)
//                            .childValue(fieldDeclaringType + "." + fieldName)
//                            .build());
//                    _neo4jService.linkMethodDependsOnField(declaringTypeName + "#" + methodSignature, fieldDeclaringType + "." + fieldName);
//                }
            }).list();
        } else return List.of();
    }

//    @Override
//    public void export() {
//        LOGGER.info("MethodExporter: Creating method nodes...");
//        createMethodNodes();
//
//        LOGGER.info("MethodExporter: Linking method return types and parameters...");
//        linkMethodSignatures();
//
//        LOGGER.info("MethodExporter: Export complete.");
//    }

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

    @Override
    public MethodTransformResult process(List<CtMethod<?>> input) {
        List<Neo4jMethodObject> methodObjects = createMethodObjects(input);
        List<Neo4jParamObject> params = new LinkedList<>();
        List<Neo4jTypeArgumentNode> typeArgs = new LinkedList<>();
        List<Neo4JLinkObject> links = new LinkedList<>(linkTypeHasMethod(input));
        input.forEach(ctMethod -> {
            ctMethod.getParameters().forEach(ctParameter -> {
                Triple<Neo4jParamObject, List<Neo4jTypeArgumentNode>, List<Neo4JLinkObject>> paramLinks
                        = linkMethodParameter(ctMethod.getDeclaringType().getQualifiedName(), ctMethod.getSignature(), ctParameter);
                links.addAll(paramLinks.getRight());
                params.add(paramLinks.getLeft());
                typeArgs.addAll(paramLinks.getMiddle());
            });

            try {
                Pair<List<Neo4jTypeArgumentNode>, List<Neo4JLinkObject>> returnTypeLinks = linkMethodReturnType(ctMethod.getDeclaringType().getQualifiedName(), ctMethod.getSignature(), ctMethod.getType());
                typeArgs.addAll(returnTypeLinks.getLeft());
                links.addAll(returnTypeLinks.getRight());
            } catch (Exception ex) {
                LOGGER.error("Error linking method return type: " + ex.getMessage());
            }
            links.addAll(linkMethodFieldDependencies(ctMethod, ctMethod.getDeclaringType().getQualifiedName(), ctMethod.getSignature()));
        });
        return new MethodTransformResult(methodObjects, params, links, typeArgs);
    }
}
