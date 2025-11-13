package org.example.integration.impl.openAI;

import org.example.integration.api.IApiResponse;
import org.example.integration.api.ITokenAuthConfig;
import org.example.integration.api.llm.ILLMConfig;
import org.example.integration.api.llm.ILLMProvider;
import org.example.integration.impl.ApiResponse;

import java.net.http.HttpClient;
import java.time.Duration;

public class OpenAIProvider implements ILLMProvider {
    private final ITokenAuthConfig _apiConfig;
    private final HttpClient _httpClient;

    public OpenAIProvider(ITokenAuthConfig config) {
        this._apiConfig = config;
        this._httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build(); // Create a reusable HttpClient :contentReference[oaicite:6]{index=6}
    }

    @Override
    public IApiResponse<String> getLLMResponse(ILLMConfig config, String input) {
        return OpenAIChatRequest
                .from(_apiConfig, config, input)
                .getResponse(_httpClient, OpenAIChatResponseDTO.class)
                .map(dto -> ApiResponse.success(dto.choices()[0].message().content()),
                        ApiResponse::failure);
    }
}
