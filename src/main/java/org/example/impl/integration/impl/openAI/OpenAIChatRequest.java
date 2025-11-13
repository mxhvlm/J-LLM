package org.example.impl.integration.impl.openAI;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.impl.integration.api.IApiRequest;
import org.example.impl.integration.api.IApiResponse;
import org.example.impl.integration.api.ITokenAuthConfig;
import org.example.impl.integration.api.llm.ILLMConfig;
import org.example.impl.integration.impl.ApiResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class OpenAIChatRequest implements IApiRequest {
    private final String _model;
    private final JsonArray _messages;
    private final double _temperature;
    private final double _topP;
    private final int _topK;
    private final int _maxTokens;
    private final String _apiUrl;
    private final String _apiKey;
    private final Gson _gson;

    private OpenAIChatRequest(String model, JsonArray messages, double temperature, double topP,
                              int topK, int maxTokens, String apiUrl, String apiKey) {
        _model = model;
        _messages = messages;
        _temperature = temperature;
        _topP = topP;
        _topK = topK;
        _maxTokens = maxTokens;
        _apiUrl = apiUrl;
        _apiKey = apiKey;
        _gson = new Gson();
    }

    public static OpenAIChatRequest from(ITokenAuthConfig apiConfig, ILLMConfig llmConfig, String userInput) {
        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", userInput);
        messages.add(message);

        return new OpenAIChatRequest(
                llmConfig.model(),
                messages,
                llmConfig.temperature(),
                llmConfig.topP(),
                llmConfig.topK(),
                llmConfig.maxTokens(),
                apiConfig.url(),
                apiConfig.token()
        );
    }

    private JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("model", _model);
        json.add("messages", _messages);
        json.addProperty("temperature", _temperature);
        json.addProperty("top_p", _topP);
        json.addProperty("n", _topK);
        json.addProperty("max_tokens", _maxTokens);
        return json;
    }

    private HttpRequest toHttpRequest() {
        return HttpRequest.newBuilder()
                .uri(URI.create(_apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + _apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(toJson().toString()))
                .build();
    }

    @Override
    public <U> IApiResponse<U> getResponse(HttpClient httpClient, Class<U> clazz) {
        try {
//      LOGGER.info("OpenAIProvider: Sending request to OpenAI API: {}", httpRequest.uri());
//      LOGGER.info("OpenAIProvider: Request body: {}", request.toJson());
            HttpResponse<String> response = httpClient.send(toHttpRequest(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return ApiResponse.failure("OpenAI API error: " + response.statusCode() + " - " + response.body());
            }

            return ApiResponse.success(_gson.fromJson(response.body(), clazz));
        } catch (Exception e) {
            return ApiResponse.failure("Exception calling OpenAI API: " + e.getMessage());
        }
    }

    @Override
    public URI getUri() {
        return URI.create(_apiUrl);
    }
}
