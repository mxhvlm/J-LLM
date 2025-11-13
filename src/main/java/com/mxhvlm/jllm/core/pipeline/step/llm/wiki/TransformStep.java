package com.mxhvlm.jllm.core.pipeline.step.llm.wiki;

import com.mxhvlm.jllm.core.integration.api.llm.ILLMConfig;
import com.mxhvlm.jllm.core.integration.api.llm.ILLMProvider;
import com.mxhvlm.jllm.core.pipeline.step.llm.AbstractLLMTransformStep;

import java.util.Collection;
import java.util.List;

public class TransformStep extends AbstractLLMTransformStep<String, String> {
    public TransformStep(ILLMProvider llmProvider, ILLMConfig config) {
        super(llmProvider, config);
    }

    @Override
    public String getLLMQuery(String input) {
        return "";
    }

    @Override
    public ILLMConfig getLLMConfig(String input) {
        return _config;
    }

    @Override
    public Collection<String> processLLMResult(String input, String resp) {
        return List.of();
    }
}
