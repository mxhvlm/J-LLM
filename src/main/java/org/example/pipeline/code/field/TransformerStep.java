package org.example.pipeline.code.field;

import org.example.datamodel.api.code.wrapper.IField;
import org.example.datamodel.api.code.wrapper.INamedElement;
import org.example.datamodel.api.code.wrapper.IType;
import org.example.datamodel.impl.neo4j.Neo4JLink;
import org.example.datamodel.impl.neo4j.Neo4jField;
import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class TransformerStep implements IPipelineStep<Stream<IField>, Stream<TransformerStep.IFieldTransformerOutput>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerStep.class);

    public sealed interface IFieldTransformerOutput permits TransformerStep.FieldNode, TransformerStep.FieldLink {}

    public record FieldNode(Neo4jField object) implements IFieldTransformerOutput {}

    public record FieldLink(Neo4JLink link) implements IFieldTransformerOutput {}

    private Stream<Neo4jField> createFieldNodes(Stream<IField> fields) {
        return fields.map(ctField -> {
            INamedElement declType = ctField.getParent().get();
            String declaringTypeName = declType.getName().getQualifiedName();

            String fieldName = ctField.getName().getSimpleName();
            String modifiers = ctField.getModifiers().toString();
            String fieldSourceSnippet = ctField.toString(); // or a more refined snippet

            LOGGER.trace("Creating field node: " + declaringTypeName + "." + fieldName);
            return new Neo4jField(declaringTypeName, fieldName, modifiers, fieldSourceSnippet);
        });
    }

    private Stream<Neo4JLink> linkFieldToDeclaringType(Stream<IField> fields) {
        return fields.map(field -> {
            INamedElement declType = field.getParent().get();
            String declaringTypeName = declType.getName().getQualifiedName();
            String fieldName = field.getName().getSimpleName();

            // Resolve the field's type name
            return Neo4JLink.Builder.create()
                    .withLabel("HAS_FIELD")
                    .parentLabel("Type")
                    .parentProp("name")
                    .parentValue(declaringTypeName)
                    .childLabel("Field")
                    .childProp("id")
                    .childValue(declaringTypeName + "." + fieldName)
                    .build();
        });
    }

    private Stream<Neo4JLink> linkFieldTypes(Stream<IField> fields) {
        return fields
            .filter(f -> f.getType().isPresent())
            .filter(f -> f.getParent().isPresent())
            .flatMap(f -> {
                INamedElement declType = f.getParent().get();
                String declaringTypeName = declType.getName().getQualifiedName();
                String fieldName = declType.getName().getSimpleName();
                IType fieldTypeRef = f.getType().get();

                return linkFieldType(declaringTypeName, fieldName, fieldTypeRef);
            });
    }

    private Stream<Neo4JLink> linkFieldType(String declaringTypeName, String fieldName, IType fieldTypeRef) {
        List<Neo4JLink> links = new LinkedList<>();

        // Resolve the field's type name
        String fieldTypeName = fieldTypeRef.getName().getQualifiedName();
        LOGGER.trace("Linking field " + declaringTypeName + "." + fieldName + " to type " + fieldTypeName);
        links.add(Neo4JLink.Builder.create()
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

        return links.stream();
    }


    @Override
    public Stream<IFieldTransformerOutput> process(Stream<IField> input) {
        LOGGER.info("FieldExporter: Processing fields...");
        Stream.Builder<IFieldTransformerOutput> builder = Stream.builder();
        input.forEach(field -> {
            // Create field nodes
            createFieldNodes(Stream.of(field)).forEach(fieldNode -> builder.add(new FieldNode(fieldNode)));

            // Create links
            linkFieldTypes(Stream.of(field)).forEach(link -> builder.add(new FieldLink(link)));
            linkFieldToDeclaringType(Stream.of(field)).forEach(link -> builder.add(new FieldLink(link)));
        });

        return builder.build();
    }
}
