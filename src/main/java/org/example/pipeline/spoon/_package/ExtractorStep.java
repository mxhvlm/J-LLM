package org.example.pipeline.spoon._package;

import org.example.CodeModel;
import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtPackage;

import java.util.stream.Stream;

public class ExtractorStep implements IPipelineStep<CodeModel, Stream<String>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractorStep.class);

    public Stream<String> extractPackages(CodeModel model) {
        LOGGER.info("ModelExtractor: Extracting packages...");
        return model.getCtModel().getAllPackages()
            .stream()
                .filter(ctPackage -> !ctPackage.isUnnamedPackage())
                .map(CtPackage::getQualifiedName)
                .distinct();
    }

    @Override
    public Stream<String> process(CodeModel input) {
        return extractPackages(input);
    }
}
