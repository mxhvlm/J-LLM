package org.example.pipeline.spoon.field;

import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;

import java.util.List;
import java.util.stream.Stream;

public class ExtractorStep implements IPipelineStep<Stream<CtType<?>>, Stream<CtField<?>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractorStep.class);

    public Stream<CtField<?>> extractFields(Stream<CtType<?>> types) {
        LOGGER.info("ModelExtractor: Extracting fields...");
        return types
            .map(CtType::getFields)
            .flatMap(List::stream);
    }

    @Override
    public Stream<CtField<?>> process(Stream<CtType<?>> input) {
        return extractFields(input);
    }
}
