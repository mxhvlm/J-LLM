package org.example.dbOutput;

import org.example.sourceImport.ModelExtractor;
import spoon.reflect.declaration.CtType;

public class TypeExporter extends Exporter {

    public TypeExporter(ModelExtractor extractor, Neo4jService neo4jService) {
        super(extractor, neo4jService);
        LOGGER.info("TypeExporter: Initializing...");
    }

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
    public void createTypeNodes() {
        for (CtType<?> ctType : _extractor.extractTypes()) {
            String typeName = ctType.getQualifiedName();
            String simpleName = ctType.getSimpleName();
            String typeKind = getTypeKind(ctType);
            String modifiers = ctType.getModifiers().toString();
            String javadoc = ctType.getDocComment();

            LOGGER.info("Creating type node: " + typeName);
            _neo4jService.createTypeNode(typeName, simpleName, typeKind, modifiers, javadoc);

            String packageName = ctType.getPackage().getQualifiedName();
            LOGGER.info("Linking type " + typeName + " to package " + packageName);
            _neo4jService.linkPackageToType(packageName, typeName);
        }
    }

    /**
     * Second Pass: Link types (parent-child relationships, inheritance, and interfaces).
     */
    public void linkTypeRelationships() {
        for (CtType<?> ctType : _extractor.extractTypes()) {
            String typeName = ctType.getQualifiedName();

            // 1. Link nested types
            for (CtType<?> nestedType : ctType.getNestedTypes()) {
                String childTypeName = nestedType.getQualifiedName();
                LOGGER.info("Linking " + typeName + " to nested type " + childTypeName);
                _neo4jService.linkTypeToType(typeName, childTypeName);
            }

            // 2. Link superclass
            if (ctType.getSuperclass() != null) {
                String superTypeName = ctType.getSuperclass().getQualifiedName();
                LOGGER.info("Linking " + typeName + " to superclass " + superTypeName);
                _neo4jService.linkTypeExtends(typeName, superTypeName);
            }

            // 3. Link interfaces
            ctType.getSuperInterfaces().forEach(interfaceRef -> {
                String interfaceName = interfaceRef.getQualifiedName();
                LOGGER.info("Linking " + typeName + " to interface " + interfaceName);
                _neo4jService.linkTypeImplements(typeName, interfaceName);
            });
        }
    }

    /**
     * Exporter entry point that runs the two-pass export process.
     */
    public void export() {
        LOGGER.info("TypeExporter: Running first pass (creating type nodes)...");
        createTypeNodes();

        LOGGER.info("TypeExporter: Running second pass (linking type relationships)...");
        linkTypeRelationships();

        LOGGER.info("TypeExporter: Export complete.");
    }
}
