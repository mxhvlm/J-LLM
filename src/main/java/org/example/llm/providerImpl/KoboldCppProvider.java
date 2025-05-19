package org.example.llm.providerImpl;

import org.example.llm.ILLMProvider;
import org.example.llm.ILLMResponse;
import org.example.llm.LLMConfig;

public class KoboldCppProvider implements ILLMProvider {
    @Override
    public ILLMResponse getLLMResponse(LLMConfig config, String input) {
        return LLMResponse.failure("KoboldCppProvider not implemented");
    }
}
