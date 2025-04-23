package org.example.pipeline.spoon.field;

import org.example.CodeModel;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;

import java.util.LinkedList;
import java.util.List;

public class FieldExtractorStep implements PipelineStep<List<CtType<?>>, List<CtField<?>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldExtractorStep.class);

    public List<CtField<?>> extractFields(List<CtType<?>> types) {
        LOGGER.info("ModelExtractor: Extracting fields...");
        List<CtField<?>> fields = new LinkedList<>();
        for (CtType<?> type : types) {
            var _fields = type.getFields();
            for (CtField<?> f : _fields) {
                if (f.getSimpleName().equals("DATA_BABY_ID")) {
                    LOGGER.info("Found field DATA_BABY_ID on type " + type.getQualifiedName());
                }
            }
            fields.addAll(_fields);
        }
        LOGGER.info("ModelExtractor: Extracted " + fields.size() + " fields.");
        return fields;
    }

    @Override
    public List<CtField<?>> process(List<CtType<?>> input) {
        return extractFields(input);
    }
}
