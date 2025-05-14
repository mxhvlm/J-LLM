package org.example.pipeline.spoon._package;

import org.example.CodeModel;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtPackage;

import java.util.stream.Stream;

public class PackageExtractorStep implements PipelineStep<CodeModel, Stream<String>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageExtractorStep.class);

    public Stream<String> extractPackages(CodeModel model) {
        LOGGER.info("ModelExtractor: Extracting packages...");
        return model.getCtModel().getAllPackages().parallelStream()
                .unordered()
                .filter(ctPackage -> !ctPackage.isUnnamedPackage())
                .map(CtPackage::getQualifiedName)
                .distinct();
    }

    @Override
    public Stream<String> process(CodeModel input) {
        return extractPackages(input);
    }
}
