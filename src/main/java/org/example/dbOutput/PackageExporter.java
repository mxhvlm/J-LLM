package org.example.dbOutput;

import org.example.sourceImport.ModelExtractor;

import java.util.Set;

public class PackageExporter extends Exporter {

    public PackageExporter(ModelExtractor extractor, Neo4jService neo4jService) {
        super(extractor, neo4jService);
        LOGGER.info("PackageExporter: Initializing...");
    }

    public void export() {
        Set<String> packages = _extractor.extractPackages();
        for (String pkg : packages) {
            createPackageHierarchy(pkg);
        }
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

            // Create the package node for the current package
            _neo4jService.createPackageNode(currentPath);

            // Link the parent to the child package
            if (parentPath != null) {
                _neo4jService.linkPackageToParent(parentPath, currentPath);
            }

            parentPath = currentPath;
        }
    }
}

