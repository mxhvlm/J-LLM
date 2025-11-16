package com.mxhvlm.jllm.core.pipeline.step.code;

import com.mxhvlm.jllm.core.datamodel.api.code.ICodeModel;
import com.mxhvlm.jllm.core.datamodel.impl.code.spoon.SpoonModelBuilder;
import com.mxhvlm.jllm.core.pipeline.IPipelineStep;

import java.util.List;

public class SpoonModelBuilderStep implements IPipelineStep<List<String>, ICodeModel> {
    @Override
    public ICodeModel process(List<String> input) {
        return new SpoonModelBuilder().withInputResources(input).buildModel();
    }
}