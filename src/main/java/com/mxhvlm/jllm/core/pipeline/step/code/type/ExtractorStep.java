package com.mxhvlm.jllm.core.pipeline.step.code.type;

import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.IType;
import com.mxhvlm.jllm.core.datamodel.impl.code.CodeModel;
import com.mxhvlm.jllm.core.pipeline.IPipelineStep;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.stream.Stream;

public class ExtractorStep implements IPipelineStep<CodeModel, Stream<IType>> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ExtractorStep.class);

    @Override
    public Stream<IType> process(CodeModel input) {
        LOGGER.info("ModelExtractor: Extracting types...");

        Collection<IType> types = input.getTypes();

        return types.stream().distinct();
    }
}
