package org.example.impl.pipeline.llm;

import org.example.impl.integration.api.llm.ILLMConfig;
import org.example.impl.integration.api.llm.ILLMProvider;
import org.example.impl.pipeline.meta.AbstractBatchedPipelineStep;

public abstract class AbstractLLMBatchedPipelineStep extends AbstractBatchedPipelineStep {
    protected final ILLMConfig _llmConfig;
    protected final ILLMProvider _llmProvider;

    public AbstractLLMBatchedPipelineStep(int batchSize, ILLMConfig config, ILLMProvider llmProvider) {
        super(batchSize);
        _llmConfig = config;
        _llmProvider = llmProvider;
    }
}
