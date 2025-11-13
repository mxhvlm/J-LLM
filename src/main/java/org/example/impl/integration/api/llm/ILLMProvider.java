package org.example.impl.integration.api.llm;

import org.example.impl.integration.api.IApiResponse;

public interface ILLMProvider {

    IApiResponse<String> getLLMResponse(ILLMConfig config, String input);
}
