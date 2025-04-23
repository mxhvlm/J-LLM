package org.example.pipeline.spoon.method;

import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.LinkedList;
import java.util.List;

public class MethodExtractorStep implements PipelineStep<List<CtType<?>>, List<CtMethod<?>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodExtractorStep.class);

    public List<CtMethod<?>> extractMethods(List<CtType<?>> types) {
        LOGGER.info("ModelExtractor: Extracting methods...");
        List<CtMethod<?>> methods = new LinkedList<>();
        for (CtType<?> type : types ) {
            // getAllMethods() can be used if you need inherited methods too,
            // but usually type.getMethods() returns declared methods.
            methods.addAll(type.getMethods());
        }
        LOGGER.info("ModelExtractor: Extracted " + methods.size() + " methods.");
        return methods;
    }
    @Override
    public List<CtMethod<?>> process(List<CtType<?>> input) {
        return extractMethods(input);
    }
}
