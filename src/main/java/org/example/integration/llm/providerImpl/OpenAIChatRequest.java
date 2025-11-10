package org.example.integration.llm.providerImpl;

import com.google.gson.*;
import org.example.integration.IApiRequest;
import org.example.integration.llm.APIConfig;
import org.example.integration.llm.LLMConfig;

import java.net.URI;
import java.net.http.HttpRequest;

class OpenAIChatRequest implements IApiRequest {
  private final String _model;
  private final JsonArray _messages;
  private final double _temperature;
  private final double _topP;
  private final int _topK;
  private final int _maxTokens;
  private final String _apiUrl;
  private final String _apiKey;

  private OpenAIChatRequest(String model, JsonArray messages, double temperature, double topP,
      int topK, int maxTokens, String apiUrl, String apiKey) {
    this._model = model;
    this._messages = messages;
    this._temperature = temperature;
    this._topP = topP;
    this._topK = topK;
    this._maxTokens = maxTokens;
    this._apiUrl = apiUrl;
    this._apiKey = apiKey;
  }

  public static OpenAIChatRequest from(APIConfig apiConfig, LLMConfig llmConfig, String userInput) {
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
        apiConfig.APIConfig()
    );
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("model", _model);
    json.add("messages", _messages);
    json.addProperty("temperature", _temperature);
    json.addProperty("top_p", _topP);
    json.addProperty("n", _topK);
    json.addProperty("max_tokens", _maxTokens);
    return json;
  }

  public HttpRequest toHttpRequest() {
    return HttpRequest.newBuilder()
        .uri(URI.create(_apiUrl))
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + _apiKey)
        .POST(HttpRequest.BodyPublishers.ofString(toJson().toString()))
        .build();
  }
}
