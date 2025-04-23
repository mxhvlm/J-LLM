package org.example.pipeline.spoon._package;

import org.example.CodeModel;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtPackage;

import java.util.HashSet;
import java.util.Set;

public class PackageExtractorStep implements PipelineStep<CodeModel, Set<String>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageExtractorStep.class);

    public Set<String> extractPackages(CodeModel model) {
        LOGGER.info("ModelExtractor: Extracting packages...");
        Set<String> packages = new HashSet<>();
        for (CtPackage ctPackage : model.getCtModel().getAllPackages()) {
            // Spoon returns a root package "<empty>" which you might skip if it's not useful
            if (!ctPackage.isUnnamedPackage()) {
                packages.add(ctPackage.getQualifiedName());
            } else {
                LOGGER.warn("Skipping unnamed package.");
            }
        }
        LOGGER.info("ModelExtractor: Extracted " + packages.size() + " packages.");
        return packages;
    }

    @Override
    public Set<String> process(CodeModel input) {
        return extractPackages(input);
    }
}
