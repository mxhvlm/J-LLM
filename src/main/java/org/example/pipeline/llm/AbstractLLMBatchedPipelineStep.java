package org.example.pipeline.llm;

import org.example.integration.api.llm.ILLMProvider;
import org.example.integration.api.llm.LLMConfig;
import org.example.pipeline.meta.AbstractBatchedPipelineStep;

public abstract class AbstractLLMBatchedPipelineStep extends AbstractBatchedPipelineStep {
  protected final LLMConfig _llmConfig;
  protected final ILLMProvider _llmProvider;

  public AbstractLLMBatchedPipelineStep(int batchSize, LLMConfig config, ILLMProvider llmProvider) {
    super(batchSize);
    _llmConfig = config;
    _llmProvider = llmProvider;
  }
}
