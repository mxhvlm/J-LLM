package org.example.pipeline.llm.wiki;

import org.example.integration.api.llm.ILLMConfig;
import org.example.integration.api.llm.ILLMProvider;
import org.example.pipeline.llm.AbstractLLMTransformStep;

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
