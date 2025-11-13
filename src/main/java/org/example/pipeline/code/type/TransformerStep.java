package org.example.pipeline.code.type;

import org.example.datamodel.api.code.wrapper.EnumTypeKind;
import org.example.datamodel.api.code.wrapper.IType;
import org.example.datamodel.impl.neo4j.Neo4JLink;
import org.example.datamodel.impl.neo4j.Neo4jType;
import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class TransformerStep implements IPipelineStep<Stream<IType>, Stream<TransformerStep.ITypeOutput>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerStep.class);

    public sealed interface ITypeOutput permits TypeNode, TypeLink {}

    public record TypeNode(Neo4jType object) implements ITypeOutput {}

    public record TypeLink(Neo4JLink link) implements ITypeOutput {}

    @Override
    public Stream<ITypeOutput> process(Stream<IType> input) {
        LOGGER.info("TypeExporter: Processing types...");
        return input.flatMap(this::transformSingleType).distinct();
    }

    private Stream<ITypeOutput> transformSingleType(IType type) {
        Stream.Builder<ITypeOutput> builder = Stream.builder();

        // === Type Node ===
        String typeName = type.getName().getQualifiedName();
        String simpleName = type.getName().getSimpleName();
        String typeKind = type.getTypeKind().toString();
        String modifiers = String.join(",", type.getModifiers());
//        String javadoc = ctType.getDocComment();
        String javadoc = ""; // TODO: Extract javadoc from IType when available

        builder.add(new TypeNode(new Neo4jType(typeName, simpleName, typeKind, modifiers, javadoc)));

        // === Package Link ===
        type.getPackage().ifPresent(p -> {
            String packageName = p.getName().getQualifiedName();
            builder.add(new TypeLink(
                    Neo4JLink.Builder.create()
                            .withLabel("CONTAINS_TYPE")
                            .parentLabel("Package")
                            .childLabel("Type")
                            .betweenProps("name")
                            .parentValue(packageName)
                            .childValue(typeName)
                            .build()
            ));
        });

        // === Nested Types ===
        type.getInnerTypes().forEach(nested -> {
            String nestedName = nested.getName().getQualifiedName();
            builder.add(new TypeLink(
                    Neo4JLink.Builder.create()
                            .withLabel("CONTAINS_TYPE")
                            .betweenLabels("Type")
                            .betweenProps("name")
                            .parentValue(typeName)
                            .childValue(nestedName)
                            .build()
            ));
        });

        // === Super Types ===
        type.getSuperClass().ifPresent(superType -> {
            String superTypeName = superType.getName().getQualifiedName();
            builder.add(new TypeLink(
                    Neo4JLink.Builder.create()
                            .withLabel("EXTENDS")
                            .betweenLabels("Type")
                            .betweenProps("name")
                            .parentValue(typeName)
                            .childValue(superTypeName)
                            .build()
            ));
        });

        // === Implemented Interfaces ===
        type.getInterfaces().forEach(interfaceType -> {
            String interfaceTypeName = interfaceType.getName().getQualifiedName();
            builder.add(new TypeLink(
                    Neo4JLink.Builder.create()
                            // Interface -> Interface relationship uses extends
                            .withLabel(type.getTypeKind() == EnumTypeKind.INTERFACE ? "EXTENDS" : "IMPLEMENTS")
                            .betweenLabels("Type")
                            .betweenProps("name")
                            .parentValue(typeName)
                            .childValue(interfaceTypeName)
                            .build()
            ));
        });

        return builder.build();
    }
}
