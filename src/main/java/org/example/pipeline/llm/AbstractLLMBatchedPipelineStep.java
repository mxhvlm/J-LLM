package org.example.pipeline.llm;

import org.example.integration.api.llm.ILLMConfig;
import org.example.integration.api.llm.ILLMProvider;
import org.example.pipeline.meta.AbstractBatchedPipelineStep;

public abstract class AbstractLLMBatchedPipelineStep extends AbstractBatchedPipelineStep {
    protected final ILLMConfig _llmConfig;
    protected final ILLMProvider _llmProvider;

    public AbstractLLMBatchedPipelineStep(int batchSize, ILLMConfig config, ILLMProvider llmProvider) {
        super(batchSize);
        _llmConfig = config;
        _llmProvider = llmProvider;
    }
}
