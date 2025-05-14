package org.example.pipeline.spoon.type;

import org.example.CodeModel;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import spoon.reflect.declaration.CtType;

import java.util.stream.Stream;

public class TypeExtractorStep implements PipelineStep<CodeModel, Stream<CtType<?>>> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TypeExtractorStep.class);
    @Override
    public Stream<CtType<?>> process(CodeModel input) {
        LOGGER.info("ModelExtractor: Extracting types...");

        return input
                .getCtModel()
                .getAllTypes()
                .parallelStream()
                .unordered()
                .distinct();
    }
}
