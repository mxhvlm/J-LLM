package org.example.pipeline.spoon.type;

import org.example.CodeModel;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import spoon.reflect.declaration.CtType;

import java.util.HashSet;
import java.util.List;

public class TypeExtractorStep implements PipelineStep<CodeModel, List<CtType<?>>> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TypeExtractorStep.class);
    @Override
    public List<CtType<?>> process(CodeModel input) {
        LOGGER.info("ModelExtractor: Extracting types...");
        var types = new HashSet<>(input.getCtModel().getAllTypes());

        LOGGER.info("ModelExtractor: Extracted " + types.size() + " types.");
        return types.stream().toList();
    }
}
