package org.example.dbOutput;

import org.example.sourceImport.ModelExtractor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

public class FieldExporter extends Exporter {

    public FieldExporter(ModelExtractor extractor, Neo4jService neo4jService) {
        super(extractor, neo4jService);
    }

    public void createFieldNodes() {
        for (CtField<?> ctField : _extractor.extractFields()) {
            String declaringTypeName = ctField.getDeclaringType().getQualifiedName();
            String fieldName = ctField.getSimpleName();
            String modifiers = ctField.getModifiers().toString();
            String fieldSourceSnippet = ctField.toString(); // or a more refined snippet

            LOGGER.trace("Creating field node: " + declaringTypeName + "." + fieldName);
            _neo4jService.createFieldNode(declaringTypeName, fieldName, modifiers, fieldSourceSnippet);
            if (fieldName.equals("DATA_BABY_ID")) {
                LOGGER.info("Found field DATA_BABY_ID on type " + declaringTypeName);
            }

            // Link field to its declaring type
            LOGGER.trace("Linking " + declaringTypeName + " to field " + fieldName);
            _neo4jService.linkTypeHasField(declaringTypeName, declaringTypeName + "." + fieldName);
        }
    }

    public void linkFieldTypes() {
        for (CtField<?> ctField : _extractor.extractFields()) {
            String declaringTypeName = ctField.getDeclaringType().getQualifiedName();
            String fieldName = ctField.getSimpleName();
            CtTypeReference<?> fieldTypeRef = ctField.getType();

            // Link the field's type
            if (fieldTypeRef != null) {
                linkFieldType(declaringTypeName, fieldName, fieldTypeRef);
            }
        }
    }

    private void linkFieldType(String declaringTypeName, String fieldName, CtTypeReference<?> fieldTypeRef) {
        // Resolve the field's type name
        String fieldTypeName = fieldTypeRef.getQualifiedName();
        LOGGER.trace("Linking field " + declaringTypeName + "." + fieldName + " to type " + fieldTypeName);
        _neo4jService.linkFieldOfType(declaringTypeName + "." + fieldName, fieldTypeName);

        // Handle generics if any
        if (!fieldTypeRef.getActualTypeArguments().isEmpty()) {
            fieldTypeRef.getActualTypeArguments().forEach(typeArgRef -> {
                // Create a TypeArgument node and link it
                String argId = _neo4jService.createTypeArgumentNode(typeArgRef);
                _neo4jService.linkFieldHasTypeArgument(declaringTypeName + "." + fieldName, argId);

                // Link the argument to its OF_TYPE
                if (typeArgRef != null && typeArgRef.getQualifiedName() != null) {
                    _neo4jService.linkTypeArgumentOfType(argId, typeArgRef.getQualifiedName());
                }
            });
        }
    }

    @Override
    public void export() {
        LOGGER.info("FieldExporter: Creating field nodes...");
        createFieldNodes();

        LOGGER.info("FieldExporter: Linking field types and generics...");
        linkFieldTypes();

        LOGGER.info("FieldExporter: Export complete.");
    }
}
