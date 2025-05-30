package org.example.pipeline.spoon.type;

import org.example.data.neo4j.Neo4JLink;
import org.example.data.neo4j.Neo4jType;
import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.util.stream.Stream;

public class Transformer implements IPipelineStep<Stream<CtType<?>>, Stream<Transformer.ITypeOutput>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Transformer.class);

    public sealed interface ITypeOutput permits TypeNode, TypeLink {}

    public record TypeNode(Neo4jType object) implements ITypeOutput {}

    public record TypeLink(Neo4JLink link) implements ITypeOutput {}

    @Override
    public Stream<ITypeOutput> process(Stream<CtType<?>> input) {
        LOGGER.info("TypeExporter: Processing types...");
        return input.flatMap(this::transformSingleType).distinct();
    }

    private Stream<ITypeOutput> transformSingleType(CtType<?> ctType) {
        Stream.Builder<ITypeOutput> builder = Stream.builder();

        // === Type Node ===
        String typeName = ctType.getQualifiedName();
        String simpleName = ctType.getSimpleName();
        String typeKind = getTypeKind(ctType);
        String modifiers = ctType.getModifiers().toString();
        String javadoc = ctType.getDocComment();

        builder.add(new TypeNode(new Neo4jType(typeName, simpleName, typeKind, modifiers, javadoc)));

        // === Package Link ===
        String packageName = ctType.getPackage().getQualifiedName();
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

        // === Nested Types ===
        ctType.getNestedTypes().forEach(nested -> {
            String nestedName = nested.getQualifiedName();
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

        // === Superclass ===
        if (ctType.getSuperclass() != null) {
            String superTypeName = ctType.getSuperclass().getQualifiedName();
            builder.add(new TypeLink(
                    Neo4JLink.Builder.create()
                            .withLabel("EXTENDS")
                            .betweenLabels("Type")
                            .betweenProps("name")
                            .parentValue(typeName)
                            .childValue(superTypeName)
                            .build()
            ));
        }

        // === Interfaces ===
        ctType.getSuperInterfaces().forEach(iface -> {
            String ifaceName = iface.getQualifiedName();
            builder.add(new TypeLink(
                    Neo4JLink.Builder.create()
                            .withLabel("IMPLEMENTS")
                            .betweenLabels("Type")
                            .betweenProps("name")
                            .parentValue(typeName)
                            .childValue(ifaceName)
                            .build()
            ));
        });

        return builder.build();
    }

    private String getTypeKind(CtType<?> ctType) {
        if (ctType.isClass()) return "class";
        if (ctType.isInterface()) return "interface";
        if (ctType.isEnum()) return "enum";
        if (ctType.isAnnotationType()) return "annotation";
        return ctType.getClass().getSimpleName();
    }
}
