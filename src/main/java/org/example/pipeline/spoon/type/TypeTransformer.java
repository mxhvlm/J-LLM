package org.example.pipeline.spoon.type;

import org.apache.commons.lang3.tuple.Pair;
import org.example.data.Neo4JLinkObject;
import org.example.data.Neo4jTypeObject;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import spoon.reflect.declaration.CtType;

import java.util.List;
import java.util.stream.Stream;

public class TypeTransformer implements PipelineStep<List<CtType<?>>, Pair<List<Neo4jTypeObject>, List<Neo4JLinkObject>>> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TypeTransformer.class);

        private String getTypeKind(CtType<?> ctType) {
            if (ctType.isClass()) return "class";
            if (ctType.isInterface()) return "interface";
            if (ctType.isEnum()) return "enum";
            if (ctType.isAnnotationType()) return "annotation";
            return ctType.getClass().getSimpleName();
        }

        /**
         * First Pass: Create all type nodes and link them to packages.
         */
        private List<Neo4jTypeObject> getTypeNodes(List<CtType<?>> types) {
            return types.stream().map(ctType -> {
                String typeName = ctType.getQualifiedName();
                String simpleName = ctType.getSimpleName();
                String typeKind = getTypeKind(ctType);
                String modifiers = ctType.getModifiers().toString();
                String javadoc = ctType.getDocComment();
                LOGGER.trace("Creating type node: " + typeName);
                return new Neo4jTypeObject(typeName, simpleName, typeKind, modifiers, javadoc);
            }).toList();
        }

        private List<Neo4JLinkObject> getPackageLinkNodes(List<CtType<?>> types) {
            return types.stream().map(ctType -> {
                String typeName = ctType.getQualifiedName();
                String packageName = ctType.getPackage().getQualifiedName();
                LOGGER.trace("Linking type " + typeName + " to package " + packageName);
                return Neo4JLinkObject.Builder.create()
                        .withLabel("CONTAINS_TYPE")
                        .parentLabel("Package")
                        .childLabel("Type")
                        .betweenProps("name")
                        .parentValue(packageName)
                        .childValue(typeName)
                        .build();
            }).toList();
        }

        private List<Neo4JLinkObject> getNestedTypeLinkNodes(List<CtType<?>> types) {
            return types.stream().flatMap(ctType -> {
                String typeName = ctType.getQualifiedName();
                return ctType.getNestedTypes().stream().map(nestedType -> {
                    String childTypeName = nestedType.getQualifiedName();
                    LOGGER.trace("Linking " + typeName + " to nested type " + childTypeName);
                    return Neo4JLinkObject.Builder.create()
                            .withLabel("CONTAINS_TYPE")
                            .betweenLabels("Type")
                            .betweenProps("name")
                            .parentValue(typeName)
                            .childValue(childTypeName)
                            .build();
                });
            }).toList();
        }

        private List<Neo4JLinkObject> getSuperTypeLinkNodes(List<CtType<?>> types) {
            return types.stream().filter(ctType -> ctType.getSuperclass() != null).map(ctType -> {
                String typeName = ctType.getQualifiedName();
                String superTypeName = ctType.getSuperclass().getQualifiedName();
                LOGGER.trace("Linking " + typeName + " to superclass " + superTypeName);
                return Neo4JLinkObject.Builder.create()
                        .withLabel("EXTENDS")
                        .betweenLabels("Type")
                        .betweenProps("name")
                        .parentValue(typeName)
                        .childValue(superTypeName)
                        .build();
            }).toList();
        }

        private List<Neo4JLinkObject> getInterfaceLinkNodes(List<CtType<?>> types) {
            return types.stream().flatMap(ctType -> {
                String typeName = ctType.getQualifiedName();
                return ctType.getSuperInterfaces().stream().map(interfaceRef -> {
                    String interfaceName = interfaceRef.getQualifiedName();
                    LOGGER.trace("Linking " + typeName + " to interface " + interfaceName);
                    return Neo4JLinkObject.Builder.create()
                            .withLabel("IMPLEMENTS")
                            .betweenLabels("Type")
                            .betweenProps("name")
                            .parentValue(typeName)
                            .childValue(interfaceName)
                            .build();
                });
            }).toList();
        }
//
//        /**
//         * Second Pass: Link types (parent-child relationships, inheritance, and interfaces).
//         */
//        public List<Neo> getTypeRelationships() {
//            for (CtType<?> ctType : _extractor.extractTypes()) {
//                String typeName = ctType.getQualifiedName();
//
//                // 1. Link nested types
//                for (CtType<?> nestedType : ctType.getNestedTypes()) {
//                    String childTypeName = nestedType.getQualifiedName();
//                    LOGGER.trace("Linking " + typeName + " to nested type " + childTypeName);
//                    _neo4jService.linkTypeToType(typeName, childTypeName);
//                }
//
//                // 2. Link superclass
//                if (ctType.getSuperclass() != null) {
//                    String superTypeName = ctType.getSuperclass().getQualifiedName();
//                    LOGGER.trace("Linking " + typeName + " to superclass " + superTypeName);
//                    _neo4jService.linkTypeExtends(typeName, superTypeName);
//                }
//
//                // 3. Link interfaces
//                ctType.getSuperInterfaces().forEach(interfaceRef -> {
//                    String interfaceName = interfaceRef.getQualifiedName();
//                    LOGGER.trace("Linking " + typeName + " to interface " + interfaceName);
//                    _neo4jService.linkTypeImplements(typeName, interfaceName);
//                });
//            }
//        }

        /**
         * Exporter entry point that runs the two-pass export process.
         */
//        public void export() {
//            LOGGER.info("TypeExporter: Creating primary type nodes...");
//            _neo4jService.createPrimitiveTypeNodes();
//
//            LOGGER.info("TypeExporter: Running first pass (creating type nodes)...");
//            createTypeNodes();
//
//            LOGGER.info("TypeExporter: Running second pass (linking type relationships)...");
//            linkTypeRelationships();
//
//            LOGGER.info("TypeExporter: Export complete.");
//        }

    @Override
    public Pair<List<Neo4jTypeObject>, List<Neo4JLinkObject>> process(List<CtType<?>> input) {
        return Pair.of(getTypeNodes(input), Stream.of(
                getPackageLinkNodes(input),
                getNestedTypeLinkNodes(input),
                getSuperTypeLinkNodes(input),
                getInterfaceLinkNodes(input)
        ).flatMap(List::stream).toList());
    }
}
