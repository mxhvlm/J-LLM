package com.mxhvlm.jllm.core.integration.api.llm;

import com.mxhvlm.jllm.core.integration.api.IApiResponse;

public interface ILLMProvider {

    IApiResponse<String> getLLMResponse(ILLMConfig config, String input);
}
