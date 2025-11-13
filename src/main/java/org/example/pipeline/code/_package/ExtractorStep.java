package org.example.pipeline.code._package;

import org.example.datamodel.api.code.IQualifiedName;
import org.example.datamodel.api.code.wrapper.IPackage;
import org.example.datamodel.impl.code.CodeModel;
import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

public class ExtractorStep implements IPipelineStep<CodeModel, Stream<String>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractorStep.class);

    public Stream<String> extractPackages(CodeModel model) {
        LOGGER.info("ModelExtractor: Extracting packages...");
        return model.getPackages()
                .stream()
                .distinct()
                .map(IPackage::getName)
                .map(IQualifiedName::toString);
    }

    @Override
    public Stream<String> process(CodeModel input) {
        return extractPackages(input);
    }
}
