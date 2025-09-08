package org.example.pipeline.code.method;

import org.example.datamodel.neo4j.Neo4JLink;
import org.example.datamodel.neo4j.Neo4jMethod;
import org.example.datamodel.neo4j.Neo4jParam;
import org.example.datamodel.neo4j.Neo4jTypeArgument;
import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;
import java.util.stream.Stream;


public class TransformerStep implements IPipelineStep<Stream<CtMethod<?>>, Stream<TransformerStep.IMethodTransformerOutput>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerStep.class);

    public sealed interface IMethodTransformerOutput
        permits MethodLinkNode, MethodNode, MethodTypeArgumentNode, MethodParamObject {}

    record MethodNode(Neo4jMethod object) implements IMethodTransformerOutput {}
    record MethodParamObject(Neo4jParam object) implements IMethodTransformerOutput {}
    record MethodLinkNode(Neo4JLink link) implements IMethodTransformerOutput {}
    record MethodTypeArgumentNode(Neo4jTypeArgument object) implements IMethodTransformerOutput {}

    private Stream<IMethodTransformerOutput> createMethodObjects(Stream<CtMethod<?>> methods) {
        return methods
            .map(ctMethod -> {
                String declaringTypeName = safelyExtract(() -> ctMethod.getDeclaringType().getQualifiedName(), "ERROR");
                String methodSignature = safelyExtract(ctMethod::getSignature, "ERROR");
                String modifiers = safelyExtract(() -> ctMethod.getModifiers().toString(), "ERROR");
                String sourceCode = ctMethod.toString();

                String javadoc = safelyExtract(ctMethod::getDocComment, "ERROR");
                String methodName = safelyExtract(ctMethod::getSimpleName, "ERROR");

                LOGGER.trace("Creating method node: " + declaringTypeName + "#" + methodSignature);
                return new Neo4jMethod(methodName, declaringTypeName, methodSignature, modifiers, sourceCode, javadoc);
            }).map(MethodNode::new);
    }

    private List<IMethodTransformerOutput> createLinks(Stream<CtMethod<?>> methods) {
        List<IMethodTransformerOutput> links = new ArrayList<>();

        methods.forEach(ctMethod -> {
            String declaringTypeName = safelyExtract(() -> ctMethod.getDeclaringType().getQualifiedName(), "ERROR");
            String methodSignature = safelyExtract(ctMethod::getSignature, "ERROR");

            // Link method to its declaring type
            links.add(createParentLink(ctMethod));

            // Link overrides
            links.addAll(createOverrideLinks(ctMethod));

            // Link method to its return type
            try {
                links.addAll(createReturnTypeLink(declaringTypeName, methodSignature, ctMethod.getType()));
            } catch (Exception e) {
                LOGGER.warn("Error creating return type link: " + e.getMessage());
            }

            // Link method to its parameters
            ctMethod.getParameters().forEach(parameter -> {
                links.addAll(createParameterLinks(declaringTypeName, methodSignature, parameter));
            });

            // Link method to its field dependencies
            links.addAll(createFieldDepLinks(ctMethod, declaringTypeName, methodSignature));
        });
        return links;
    }

    private IMethodTransformerOutput createParentLink(CtMethod<?> ctMethod) {
            String declaringTypeName = ctMethod.getDeclaringType().getQualifiedName();
            String methodSignature = ctMethod.getSignature();

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
//
//    public void linkMethodSignatures(Stream<CtMethod<?>> methods) {
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
//                // Store this in a dependency Stream for the DependenciesExporter later
//                // e.g., record that declaringTypeName DEPENDS_ON thrownRef.getQualifiedName()
//            }
//        }
//    }

  private List<IMethodTransformerOutput> createOverrideLinks(CtMethod<?> ctMethod) {
    List<IMethodTransformerOutput> links = new ArrayList<>();

    // Subtype (the method we’re analyzing)
    CtType<?> declaringType = ctMethod.getDeclaringType();
    String subTypeName = declaringType.getQualifiedName();
    String subMethodSignature = ctMethod.getSignature();
    String subMethodId = subTypeName + "#" + subMethodSignature;

    // Start from the type reference
    CtTypeReference<?> typeRef = declaringType.getReference();

    // Set to avoid duplicates
    Set<CtTypeReference<?>> visited = new HashSet<>();
    Deque<CtTypeReference<?>> toVisit = new ArrayDeque<>();

    // Add direct superclass and interfaces
    if (typeRef.getSuperclass() != null) {
      toVisit.add(typeRef.getSuperclass());
    }
    toVisit.addAll(typeRef.getSuperInterfaces());

    // Traverse the type hierarchy (BFS)
    while (!toVisit.isEmpty()) {
      CtTypeReference<?> current = toVisit.poll();
      if (!visited.add(current)) continue;

      CtType<?> currentDecl = current.getTypeDeclaration();
      if (currentDecl == null) continue; // skip types outside the model

      // Look for matching methods in the supertype
      currentDecl.getMethods().stream()
          .filter(superMethod -> superMethod.getSignature().equals(subMethodSignature))
          .forEach(superMethod -> {
            String superTypeName = currentDecl.getQualifiedName();
            String superMethodId = superTypeName + "#" + superMethod.getSignature();

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
      if (current.getSuperclass() != null) {
        toVisit.add(current.getSuperclass());
      }
      toVisit.addAll(current.getSuperInterfaces());
    }

    return links;
  }

  private List<IMethodTransformerOutput> createReturnTypeLink(String declaringTypeName, String methodSignature, CtTypeReference<?> returnTypeRef) throws Exception {
        if (returnTypeRef == null) {
            throw new Exception("Return type for method " + declaringTypeName + "#" + methodSignature + " is null");
        }
        String returnTypeName = safelyExtract(returnTypeRef::getQualifiedName, "ERROR");
        LOGGER.trace("Linking method " + declaringTypeName + "#" + methodSignature + " returns " + returnTypeName);
//        Stream<Neo4JLinkObject> links = new LinkedStream<>();
//        links.add(Neo4JLinkObject.Builder.create()
//                .withLabel("RETURNS")
//                .parentLabel("Method")
//                .parentProp("id")
//                .parentValue(declaringTypeName + "#" + methodSignature)
//                .childLabel("Type")
//                .childProp("name")
//                .childValue(returnTypeName)
//                .build());
//        return Pair.of(Stream.of(), links);

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
                    .build()));

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

    private List<IMethodTransformerOutput> createParameterLinks(String declaringTypeName, String methodSignature, CtParameter<?> parameter) {
        String paramName = parameter.getSimpleName();
        CtTypeReference<?> paramTypeRef = parameter.getType();
        String paramId = declaringTypeName + "#" + methodSignature + ":" + paramName;
//        String paramNodeId = _neo4jService.createParameterNode(declaringTypeName, methodSignature, paramName);
        LOGGER.trace("Linking method " + declaringTypeName + "#" + methodSignature + " has parameter " + paramName);
//        _neo4jService.linkMethodHasParameter(declaringTypeName + "#" + methodSignature, paramNodeId);
        Neo4JLink paramMethodLink = Neo4JLink.Builder.create()
            .withLabel("HAS_PARAMETER")
            .parentLabel("Method")
            .parentProp("id")
            .parentValue(declaringTypeName + "#" + methodSignature)
            .childLabel("Parameter")
            .childProp("id")
            .childValue(paramId)
            .build();

        // Link parameter of_type
        String paramTypeName = paramTypeRef.getQualifiedName();
//        _neo4jService.linkParameterOfType(paramNodeId, paramTypeName);
        Neo4JLink paramTypeLink = Neo4JLink.Builder.create()
                .withLabel("OF_TYPE")
                .parentLabel("Parameter")
                .parentProp("id")
                .parentValue(paramId)
                .childLabel("Type")
                .childProp("name")
                .childValue(paramTypeName)
                .build();
//        // Handle generic parameters
//        paramTypeRef.getActualTypeArguments().forEach(typeArgRef -> {
//            String argId = _neo4jService.createTypeArgumentNode(typeArgRef);
//            _neo4jService.linkParameterHasTypeArgument(paramNodeId, argId);
//            if (typeArgRef != null && typeArgRef.getQualifiedName() != null) {
//                _neo4jService.linkTypeArgumentOfType(argId, typeArgRef.getQualifiedName());
//            }
//        });

        Neo4jParam paramObject = new Neo4jParam(paramId, paramName);

        return List.of(
                new MethodParamObject(paramObject),
                new MethodLinkNode(paramMethodLink),
                new MethodLinkNode(paramTypeLink)
        );
    }

    private List<IMethodTransformerOutput> createFieldDepLinks(CtMethod<?> ctMethod, String declaringTypeName, String methodSignature) {
        // Create a map to avoid duplicate links
        Map<String, MethodLinkNode> links = new HashMap<>();
        if (ctMethod.getBody() != null) {
            ctMethod
                .getBody()
                .filterChildren(CtFieldAccess.class::isInstance)
                .forEach(access -> {
                    CtFieldAccess<?> fieldAccess = (CtFieldAccess<?>) access;
                    CtFieldReference<?> fieldRef = fieldAccess.getVariable();
                    String fieldDeclaringType = safelyExtract(() -> fieldRef.getDeclaringType().getQualifiedName(), "ERROR");
                    String fieldName = safelyExtract(fieldRef::getSimpleName, "ERROR");
                    String fieldFullName = fieldDeclaringType + "." + fieldName;

                    LOGGER.trace("Linking method " + declaringTypeName + "#" + methodSignature + " to field " + fieldFullName);

                    if (fieldName.equals("PERSISTED_NBT_TAG")) {
                        LOGGER.info("Linking static field " + fieldFullName + " to method " + declaringTypeName + "#" + methodSignature);
                    }

                    links.computeIfAbsent(fieldFullName, (key) -> new MethodLinkNode(
                        Neo4JLink.Builder.create()
                            .withLabel("DEPENDS_ON")
                            .betweenLabels("Method", "Field")
                            .betweenProps("id")
                            .parentValue(declaringTypeName + "#" + methodSignature)
                            .childValue(fieldRef.isStatic() ? fieldFullName : fieldDeclaringType + "." + fieldName)
                            .build()));
            });
        }
        return new ArrayList<>(links.values());
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
            LOGGER.warn("Error during processing: " + e.getMessage());
            LOGGER.debug("Stack trace: " + Arrays.toString(e.getStackTrace()));
            return fallback;
        }
    }

    @FunctionalInterface
    private interface Extractor<T> {
        T extract() throws Exception;
    }

    @Override
    public Stream<IMethodTransformerOutput> process(Stream<CtMethod<?>> input) {
        LOGGER.info("MethodExporter: Processing methods...");
      List<CtMethod<?>> methods = input.toList();  // or input.collect(Collectors.toList());
      createMethodObjects(methods.stream());
      createLinks(methods.stream());
      return Stream.concat(
            createMethodObjects(methods.stream()),
            createLinks(methods.stream()).stream()
        );
    }
}
