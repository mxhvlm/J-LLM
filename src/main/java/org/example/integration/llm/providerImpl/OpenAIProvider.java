package org.example.integration.llm.providerImpl;

import org.example.integration.llm.APIConfig;
import org.example.integration.llm.ILLMProvider;
import org.example.integration.llm.ILLMResponse;
import org.example.integration.llm.LLMConfig;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.*;
import java.time.Duration;

public class OpenAIProvider implements ILLMProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAIProvider.class);
    private final APIConfig _apiConfig;
    private final HttpClient _httpClient;

    public OpenAIProvider(APIConfig config) {
        this._apiConfig = config;
        this._httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build(); // Create a reusable HttpClient :contentReference[oaicite:6]{index=6}
    }

    @Override
    public ILLMResponse getLLMResponse(LLMConfig config, String input) {
        try {
            OpenAIChatRequest request = OpenAIChatRequest.from(_apiConfig, config, input);
            HttpRequest httpRequest = request.toHttpRequest();

            LOGGER.info("OpenAIProvider: Sending request to OpenAI API: {}", httpRequest.uri());
            LOGGER.info("OpenAIProvider: Request body: {}", request.toJson());
            HttpResponse<String> response = _httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return LLMResponse.failure("OpenAI API error: " + response.statusCode() + " - " + response.body());
            }

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray choices = json.getAsJsonArray("choices");
            if (choices.isEmpty()) {
                return LLMResponse.failure("No choices returned by OpenAI API.");
            }

            String content = choices.get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

            return LLMResponse.success(content);
        } catch (Exception e) {
            return LLMResponse.failure("Exception calling OpenAI API: " + e.getMessage());
        }
    }
}
