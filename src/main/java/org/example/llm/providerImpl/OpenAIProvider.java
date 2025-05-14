package org.example.llm.providerImpl;

import org.example.llm.ILLMConfig;
import org.example.llm.ILLMProvider;
import org.example.llm.ILLMResponse;
import org.example.llm.LLMResponse;

public class OpenAIProvider implements ILLMProvider {
    @Override
    public ILLMResponse getLLMResponse(ILLMConfig config, String input) {
        return LLMResponse.failure("OpenAI is not implemented yet.");
    }
}
