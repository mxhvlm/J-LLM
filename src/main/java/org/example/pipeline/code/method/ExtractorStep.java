package org.example.pipeline.code.method;

import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.Collection;
import java.util.stream.Stream;

public class ExtractorStep implements IPipelineStep<Stream<CtType<?>>, Stream<CtMethod<?>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractorStep.class);

    public Stream<CtMethod<?>> extractMethods(Stream<CtType<?>> types) {
        LOGGER.info("ModelExtractor: Extracting methods...");
        return types
                .map(CtType::getMethods)
                .flatMap(Collection::stream)
            .distinct();
    }
    @Override
    public Stream<CtMethod<?>> process(Stream<CtType<?>> input) {
        return extractMethods(input);
    }
}
