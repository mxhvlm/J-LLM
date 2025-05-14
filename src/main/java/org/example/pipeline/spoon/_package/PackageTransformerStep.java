package org.example.pipeline.spoon._package;

import org.example.data.Neo4JLinkObject;
import org.example.data.Neo4jPackageObject;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

public class PackageTransformerStep implements PipelineStep<Stream<String>, Stream<PackageTransformerStep.PackageOutput>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageTransformerStep.class);

    public sealed interface PackageOutput permits PackageNode, PackageLink {}
    public record PackageNode(Neo4jPackageObject object) implements PackageOutput {}
    public record PackageLink(Neo4JLinkObject link) implements PackageOutput {}


    @Override
    public Stream<PackageOutput> process(Stream<String> input) {
        LOGGER.info("PackageExporter: Exporting packages...");
        return input.flatMap(this::createPackageHierarchy).distinct();
    }

    private Stream<PackageOutput> createPackageHierarchy(String fullPackageName) {
        String[] parts = fullPackageName.split("\\.");

        List<PackageOutput> results = new ArrayList<>();

        String parentPath = null;
        String currentPath;

        for (int i = 0; i < parts.length; i++) {
            currentPath = (i == 0) ? parts[i] : parentPath + "." + parts[i];
            results.add(new PackageNode(new Neo4jPackageObject(currentPath, parts[i])));
            if (parentPath != null) {
                Neo4JLinkObject link = Neo4JLinkObject.Builder.create()
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

