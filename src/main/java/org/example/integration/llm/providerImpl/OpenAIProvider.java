package org.example.integration.llm.providerImpl;

import org.example.integration.ApiResponse;
import org.example.integration.llm.APIConfig;
import org.example.integration.llm.ILLMProvider;
import org.example.integration.IApiResponse;
import org.example.integration.llm.LLMConfig;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.*;
import java.time.Duration;
import java.util.stream.Stream;

public class OpenAIProvider implements ILLMProvider {
    private final APIConfig _apiConfig;
    private final HttpClient _httpClient;

    public OpenAIProvider(APIConfig config) {
        this._apiConfig = config;
        this._httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build(); // Create a reusable HttpClient :contentReference[oaicite:6]{index=6}
    }

    @Override
    public IApiResponse<String> getLLMResponse(LLMConfig config, String input) {
        return OpenAIChatRequest
            .from(_apiConfig, config, input)
            .getResponse(_httpClient, OpenAIChatResponseDTO.class)
            .map(dto -> ApiResponse.success(dto.choices()[0].message().content()),
                ApiResponse::failure);
    }
}
