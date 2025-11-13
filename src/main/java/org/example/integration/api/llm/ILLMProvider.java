package org.example.integration.api.llm;

import org.example.integration.api.IApiResponse;

public interface ILLMProvider {

    IApiResponse<String> getLLMResponse(LLMConfig config, String input);
}
