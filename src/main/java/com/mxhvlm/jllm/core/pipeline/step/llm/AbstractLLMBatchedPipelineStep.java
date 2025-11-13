package com.mxhvlm.jllm.core.pipeline.step.llm;

import com.mxhvlm.jllm.core.integration.api.llm.ILLMConfig;
import com.mxhvlm.jllm.core.integration.api.llm.ILLMProvider;
import com.mxhvlm.jllm.core.pipeline.definitions.AbstractBatchedPipelineStep;

public abstract class AbstractLLMBatchedPipelineStep extends AbstractBatchedPipelineStep {
    protected final ILLMConfig _llmConfig;
    protected final ILLMProvider _llmProvider;

    public AbstractLLMBatchedPipelineStep(int batchSize, ILLMConfig config, ILLMProvider llmProvider) {
        super(batchSize);
        _llmConfig = config;
        _llmProvider = llmProvider;
    }
}
