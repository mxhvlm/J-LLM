package org.example.pipeline.spoon.type;

import org.example.data.Neo4JLinkObject;
import org.example.data.Neo4jTypeObject;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtType;

import java.util.stream.Stream;

public class TypeTransformer implements PipelineStep<Stream<CtType<?>>, Stream<TypeTransformer.TypeOutput>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeTransformer.class);

    public sealed interface TypeOutput permits TypeNode, TypeLink {}

    public record TypeNode(Neo4jTypeObject object) implements TypeOutput {}

    public record TypeLink(Neo4JLinkObject link) implements TypeOutput {}

    @Override
    public Stream<TypeOutput> process(Stream<CtType<?>> input) {
        return input.flatMap(this::transformSingleType).distinct();
    }

    private Stream<TypeOutput> transformSingleType(CtType<?> ctType) {
        Stream.Builder<TypeOutput> builder = Stream.builder();

        // === Type Node ===
        String typeName = ctType.getQualifiedName();
        String simpleName = ctType.getSimpleName();
        String typeKind = getTypeKind(ctType);
        String modifiers = ctType.getModifiers().toString();
        String javadoc = ctType.getDocComment();

        builder.add(new TypeNode(new Neo4jTypeObject(typeName, simpleName, typeKind, modifiers, javadoc)));

        // === Package Link ===
        String packageName = ctType.getPackage().getQualifiedName();
        builder.add(new TypeLink(
                Neo4JLinkObject.Builder.create()
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
                    Neo4JLinkObject.Builder.create()
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
                    Neo4JLinkObject.Builder.create()
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
                    Neo4JLinkObject.Builder.create()
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
