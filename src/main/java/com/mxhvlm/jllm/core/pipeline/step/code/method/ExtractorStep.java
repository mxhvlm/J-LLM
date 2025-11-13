package com.mxhvlm.jllm.core.pipeline.step.code.method;

import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.IMethod;
import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.IType;
import com.mxhvlm.jllm.core.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Stream;

public class ExtractorStep implements IPipelineStep<Stream<IType>, Stream<IMethod>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractorStep.class);

    public Stream<IMethod> extractMethods(Stream<IType> types) {
        LOGGER.info("ModelExtractor: Extracting methods...");
        return types
                .map(IType::getMethods)
                .flatMap(Collection::stream)
                .distinct();
    }

    @Override
    public Stream<IMethod> process(Stream<IType> input) {
        return extractMethods(input);
    }
}
