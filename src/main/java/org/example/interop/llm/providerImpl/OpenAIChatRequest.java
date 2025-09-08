package org.example.interop.llm.providerImpl;

import com.google.gson.*;
import org.example.interop.llm.APIConfig;
import org.example.interop.llm.LLMConfig;

import java.net.URI;
import java.net.http.HttpRequest;

class OpenAIChatRequest {
  private final String model;
  private final JsonArray messages;
  private final double temperature;
  private final double topP;
  private final int topK;
  private final int maxTokens;
  private final String apiUrl;
  private final String apiKey;

  private OpenAIChatRequest(String model, JsonArray messages, double temperature, double topP,
      int topK, int maxTokens, String apiUrl, String apiKey) {
    this.model = model;
    this.messages = messages;
    this.temperature = temperature;
    this.topP = topP;
    this.topK = topK;
    this.maxTokens = maxTokens;
    this.apiUrl = apiUrl;
    this.apiKey = apiKey;
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
    json.addProperty("model", model);
    json.add("messages", messages);
    json.addProperty("temperature", temperature);
    json.addProperty("top_p", topP);
    json.addProperty("n", topK);
    json.addProperty("max_tokens", maxTokens);
    return json;
  }

  public HttpRequest toHttpRequest() {
    return HttpRequest.newBuilder()
        .uri(URI.create(apiUrl))
        .header("Content-Type", "application/json")
        .header("Authorization", "Bearer " + apiKey)
        .POST(HttpRequest.BodyPublishers.ofString(toJson().toString()))
        .build();
  }
}
