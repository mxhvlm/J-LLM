package org.example.pipeline.spoon.field;

import org.apache.commons.lang3.tuple.Pair;
import org.example.data.Neo4JLinkObject;
import org.example.data.Neo4jFieldObject;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtField;
import spoon.reflect.reference.CtTypeReference;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class FieldTransformerStep implements PipelineStep<List<CtField<?>>, Pair<List<Neo4jFieldObject>, List<Neo4JLinkObject>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldTransformerStep.class);

    private List<Neo4jFieldObject> createFieldNodes(List<CtField<?>> fields) {
        return fields.stream().map(ctField -> {
            String declaringTypeName = ctField.getDeclaringType().getQualifiedName();
            String fieldName = ctField.getSimpleName();
            String modifiers = ctField.getModifiers().toString();
            String fieldSourceSnippet = ctField.toString(); // or a more refined snippet

            LOGGER.trace("Creating field node: " + declaringTypeName + "." + fieldName);
            return new Neo4jFieldObject(declaringTypeName, fieldName, modifiers, fieldSourceSnippet);
        }).toList();
    }

    private List<Neo4JLinkObject> linkFieldToDeclaringType(List<CtField<?>> fields) {
        return fields.stream().map(ctField -> {
            String declaringTypeName = ctField.getDeclaringType().getQualifiedName();
            String fieldName = ctField.getSimpleName();
            CtTypeReference<?> fieldTypeRef = ctField.getType();

            // Resolve the field's type name
            String fieldTypeName = fieldTypeRef.getQualifiedName();
            LOGGER.trace("Linking field " + declaringTypeName + "." + fieldName + " to type " + fieldTypeName);
            return Neo4JLinkObject.Builder.create()
                    .withLabel("HAS_FIELD")
                    .parentLabel("Type")
                    .parentProp("name")
                    .parentValue(declaringTypeName)
                    .childLabel("Field")
                    .childProp("id")
                    .childValue(declaringTypeName + "." + fieldName)
                    .build();
        }).toList();
    }

    private List<Neo4JLinkObject> linkFieldTypes(List<CtField<?>> fields) {
        return fields.stream().filter(ctField -> ctField.getType() != null).map(ctField -> {
            String declaringTypeName = ctField.getDeclaringType().getQualifiedName();
            String fieldName = ctField.getSimpleName();
            CtTypeReference<?> fieldTypeRef = ctField.getType();

            return linkFieldType(declaringTypeName, fieldName, fieldTypeRef);
        }).flatMap(List::stream).toList();
    }

    private List<Neo4JLinkObject> linkFieldType(String declaringTypeName, String fieldName, CtTypeReference<?> fieldTypeRef) {
        List<Neo4JLinkObject> links = new LinkedList<>();

        // Resolve the field's type name
        String fieldTypeName = fieldTypeRef.getQualifiedName();
        LOGGER.trace("Linking field " + declaringTypeName + "." + fieldName + " to type " + fieldTypeName);
        links.add(Neo4JLinkObject.Builder.create()
                .withLabel("OF_TYPE")
                .parentLabel("Field")
                .parentProp("id")
                .parentValue(declaringTypeName + "." + fieldName)
                .childLabel("Type")
                .childProp("name")
                .childValue(fieldTypeName)
                .build());

//        // Handle generics if any
//        if (!fieldTypeRef.getActualTypeArguments().isEmpty()) {
//            fieldTypeRef.getActualTypeArguments().forEach(typeArgRef -> {
//                // Create a TypeArgument node and link it
//                String argId = _neo4jService.createTypeArgumentNode(typeArgRef);
//                _neo4jService.linkFieldHasTypeArgument(declaringTypeName + "." + fieldName, argId);
//
//                // Link the argument to its OF_TYPE
//                if (typeArgRef != null && typeArgRef.getQualifiedName() != null) {
//                    _neo4jService.linkTypeArgumentOfType(argId, typeArgRef.getQualifiedName());
//                }
//            });
//        }

        return links;
    }


    @Override
    public Pair<List<Neo4jFieldObject>, List<Neo4JLinkObject>> process(List<CtField<?>> input) {
        return Pair.of(createFieldNodes(input), Stream.of(
                linkFieldTypes(input),
                linkFieldToDeclaringType(input)
        ).flatMap(List::stream).toList());
    }
}
