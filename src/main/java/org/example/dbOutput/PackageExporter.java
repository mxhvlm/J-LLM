package org.example.dbOutput;

import org.example.sourceImport.ModelExtractor;

import java.util.HashSet;
import java.util.Set;

public class PackageExporter extends Exporter {
    private final Set<String> _createdPackages;

    public PackageExporter(ModelExtractor extractor, Neo4jService neo4jService) {
        super(extractor, neo4jService);
        LOGGER.info("PackageExporter: Initializing...");
        _createdPackages = new HashSet<>();
    }

    public void export() {
        LOGGER.info("PackageExporter: Exporting packages...");
        Set<String> packages = _extractor.extractPackages();
        for (String pkg : packages) {
            createPackageHierarchy(pkg);
        }
        LOGGER.info("PackageExporter: Exported " + packages.size() + " packages.");
    }

    private void createPackageHierarchy(String fullPackageName) {
        String[] parts = fullPackageName.split("\\.");
        String parentPath = null;
        String currentPath = null;

        for (int i = 0; i < parts.length; i++) {
            if (i == 0) {
                currentPath = parts[i];
            } else {
                currentPath = parentPath + "." + parts[i];
            }

            // Only create the package node if it hasn't been created before
            if (!_createdPackages.contains(currentPath)) {
                _neo4jService.createPackageNode(currentPath, parts[i]);
                _createdPackages.add(currentPath);
            }

            // Link the parent to the child package
            if (parentPath != null) {
                // This uses MERGE, so it's safe even if the relationship already exists
                _neo4jService.linkPackageToParent(parentPath, currentPath);
            }

            parentPath = currentPath;
        }
    }
}
