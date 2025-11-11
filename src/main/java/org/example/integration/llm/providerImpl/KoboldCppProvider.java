package org.example.integration.llm.providerImpl;

import org.example.integration.ApiResponse;
import org.example.integration.llm.ILLMProvider;
import org.example.integration.IApiResponse;
import org.example.integration.llm.LLMConfig;

public class KoboldCppProvider implements ILLMProvider {
    @Override
    public IApiResponse getLLMResponse(LLMConfig config, String input) {
        return ApiResponse.failure("KoboldCppProvider not implemented");
    }
}
