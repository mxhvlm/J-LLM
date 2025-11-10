package org.example.integration.llm.providerImpl;

import org.example.integration.llm.ILLMProvider;
import org.example.integration.llm.ILLMResponse;
import org.example.integration.llm.LLMConfig;

public class KoboldCppProvider implements ILLMProvider {
    @Override
    public ILLMResponse getLLMResponse(LLMConfig config, String input) {
        return LLMResponse.failure("KoboldCppProvider not implemented");
    }
}
