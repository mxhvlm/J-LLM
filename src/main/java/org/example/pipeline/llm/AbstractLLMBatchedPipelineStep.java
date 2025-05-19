package org.example.pipeline.llm;

import org.example.llm.ILLMProvider;
import org.example.llm.LLMConfig;
import org.example.pipeline.meta.AbstractBatchedPipelineStep;

public abstract class AbstractLLMBatchedPipelineStep extends AbstractBatchedPipelineStep {
  protected final LLMConfig _config;
  protected final ILLMProvider _llmProvider;

  public AbstractLLMBatchedPipelineStep(int batchSize, LLMConfig config, ILLMProvider llmProvider) {
    super(batchSize);
    _config = config;
    _llmProvider = llmProvider;
  }
}
