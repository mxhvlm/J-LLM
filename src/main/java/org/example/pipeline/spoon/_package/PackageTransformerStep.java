package org.example.pipeline.spoon._package;

import org.apache.commons.lang3.tuple.Pair;
import org.example.data.Neo4JLinkObject;
import org.example.data.Neo4jPackageObject;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PackageTransformerStep implements PipelineStep<Set<String>, Pair<List<Neo4jPackageObject>, List<Neo4JLinkObject>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageTransformerStep.class);
    private final Set<String> _createdPackages;

    public PackageTransformerStep() {
        _createdPackages = new HashSet<>();
    }

    @Override
    public Pair<List<Neo4jPackageObject>, List<Neo4JLinkObject>> process(Set<String> input) {
        LOGGER.info("PackageExporter: Exporting packages...");
//        input.forEach(this::createPackageHierarchy);
        return input.stream().map(this::createPackageHierarchy).reduce(
                Pair.of(new LinkedList<>(), new LinkedList<>()), (acc, pair) -> {
                    acc.getLeft().addAll(pair.getLeft());
                    acc.getRight().addAll(pair.getRight());
                    return acc;
        });
    }

    private Pair<List<Neo4jPackageObject>, List<Neo4JLinkObject>> createPackageHierarchy(String fullPackageName) {
        String[] parts = fullPackageName.split("\\.");
        String parentPath = null;
        String currentPath = null;

        List<Neo4jPackageObject> packages = new LinkedList<>();
        List<Neo4JLinkObject> packageToPackageLinks = new LinkedList<>();

        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                currentPath = parts[i];
            } else {
                currentPath = parentPath + "." + parts[i];
            }

            // Only create the package node if it hasn't been created before
            if (!_createdPackages.contains(currentPath)) {
                packages.add(new Neo4jPackageObject(currentPath, parts[i]));

                _createdPackages.add(currentPath);
            }

            // Link the parent to the child package
            if (parentPath != null) {
                // This uses MERGE, so it's safe even if the relationship already exists
//                _neo4jService.linkPackageToParent(parentPath, currentPath);
//            TODO: Check if this is still using MERGE or if we're causing issues by creating duplicate relationships
                packageToPackageLinks.add(
                        Neo4JLinkObject.Builder.create()
                                .withLabel("CONTAINS_PACKAGE")
                                .betweenLabels("Package")
                                .betweenProps("name")
                                .parentValue(parentPath)
                                .childValue(currentPath)
                                .build());
            }

            parentPath = currentPath;
        }

        return Pair.of(packages, packageToPackageLinks);
    }
}
