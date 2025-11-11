package org.example.integration.llm;

import org.example.integration.IApiResponse;

public interface ILLMProvider {

    IApiResponse<String> getLLMResponse(LLMConfig config, String input);
}
