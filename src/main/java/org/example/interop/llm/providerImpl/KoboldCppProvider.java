package org.example.interop.llm.providerImpl;

import org.example.interop.llm.ILLMProvider;
import org.example.interop.llm.ILLMResponse;
import org.example.interop.llm.LLMConfig;

public class KoboldCppProvider implements ILLMProvider {
    @Override
    public ILLMResponse getLLMResponse(LLMConfig config, String input) {
        return LLMResponse.failure("KoboldCppProvider not implemented");
    }
}
