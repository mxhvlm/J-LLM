package org.example.pipeline.code._package;

import org.example.datamodel.neo4j.Neo4JLink;
import org.example.datamodel.neo4j.Neo4jPackage;
import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class TransformerStep implements IPipelineStep<Stream<String>, Stream<TransformerStep.IPackageOutput>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformerStep.class);

    public sealed interface IPackageOutput permits PackageNode, PackageLink {}
    public record PackageNode(Neo4jPackage object) implements IPackageOutput {}
    public record PackageLink(Neo4JLink link) implements IPackageOutput {}


    @Override
    public Stream<IPackageOutput> process(Stream<String> input) {
        LOGGER.info("PackageExporter: Exporting packages...");
        return input.flatMap(this::createPackageHierarchy).distinct();
    }

    private Stream<IPackageOutput> createPackageHierarchy(String fullPackageName) {
        String[] parts = fullPackageName.split("\\.");

        List<IPackageOutput> results = new ArrayList<>();

        String parentPath = null;
        String currentPath;

        for (int i = 0; i < parts.length; i++) {
            currentPath = (i == 0) ? parts[i] : parentPath + "." + parts[i];
            results.add(new PackageNode(new Neo4jPackage(currentPath, parts[i])));
            if (parentPath != null) {
                Neo4JLink link = Neo4JLink.Builder.create()
                        .withLabel("CONTAINS_PACKAGE")
                        .betweenLabels("Package")
                        .betweenProps("name")
                        .parentValue(parentPath)
                        .childValue(currentPath)
                        .build();

                results.add(new PackageLink(link));
            }
            parentPath = currentPath;
        }
        return results.stream();
    }
}

