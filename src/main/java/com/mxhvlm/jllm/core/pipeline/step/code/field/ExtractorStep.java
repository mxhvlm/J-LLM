package com.mxhvlm.jllm.core.pipeline.step.code.field;

import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.IField;
import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.IType;
import com.mxhvlm.jllm.core.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

public class ExtractorStep implements IPipelineStep<Stream<IType>, Stream<IField>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractorStep.class);

    public Stream<IField> extractFields(Stream<IType> types) {
        LOGGER.info("ModelExtractor: Extracting fields...");
        return types
                .map(IType::getFields)
                .flatMap(List::stream);
    }

    @Override
    public Stream<IField> process(Stream<IType> input) {
        return extractFields(input);
    }
}
