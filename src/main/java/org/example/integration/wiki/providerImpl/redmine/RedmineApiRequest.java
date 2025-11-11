package org.example.integration.wiki.providerImpl.redmine;

import com.google.gson.*;
import org.example.integration.ApiResponse;
import org.example.integration.EnumHttpMethod;
import org.example.integration.IApiRequest;
import org.example.integration.IApiResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

class RedmineApiRequest implements IApiRequest {
    private final RedmineApiConfig _config;
    private final String _endpoint;
    private final EnumRedmineContentType _contentType;
    private final JsonObject _body;
    private final EnumHttpMethod _method;
    private final boolean _isPaginated;
    private int _offset;
    private final int _limit = 100;
    private final Gson _gson;

    private RedmineApiRequest(RedmineApiConfig config, String endpoint, EnumRedmineContentType contentType, JsonObject body, EnumHttpMethod method, boolean isPaginated) {
        _config = config;
        _endpoint = endpoint;
        _contentType = contentType;
        _body = body;
        _method = method;
        _isPaginated = isPaginated;
        _gson = new Gson();
    }

    private JsonObject toJson() {
        return _body;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    private HttpRequest toHttpRequest() {
        return HttpRequest.newBuilder()
                .uri(getUri())
                .header("Content-Type", _contentType.getMimeType())
                .header("X-Redmine-API-Key", _config.apiKey())
                .method(_method.name(), HttpRequest.BodyPublishers.ofString(toString()))
                .build();
    }

    @Override
    public <U> IApiResponse<U> getResponse(HttpClient httpClient, Class<U> clazz) {
        if (_isPaginated) {
            try {
                JsonArray allItems = new JsonArray();
                while (true) {
                    HttpResponse<String> response = httpClient.send(toHttpRequest(), HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() != 200) {
                        return ApiResponse.failure(String.format("Request failed with status code %d: %s", response.statusCode(), response.body()));
                    }

                    /*
                    Response looking like this:
                        GET /issues.json
                        { "issues":[...], "total_count":2595, "limit":25, "offset":0 }
                    Due to the array being named after the endpoint, we cannot generically extract it by name.
                    Instead we simply get the first array in the response.
                     */
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    int totalCount = json.get("total_count").getAsInt();
                    JsonArray items = json.entrySet().stream()
                            .filter(e -> e.getValue().isJsonArray())
                            .map(e -> e.getValue().getAsJsonArray())
                            .findFirst()
                            .orElse(new JsonArray());

                    allItems.addAll(items);

                    if (_offset + _limit >= totalCount) {
                        return ApiResponse.success(_gson.fromJson(allItems, clazz)); // Deserialize to ensure type correctness
                    } else {
                        _offset += _limit;
                    }
                }
            } catch (JsonSyntaxException ex) {
                return ApiResponse.failure("Failed to parse JSON response: " + ex.getMessage());
            } catch (Exception ex) {
                return ApiResponse.failure("Exception during paginated API request: " + ex.getMessage());
            }
        } else {
            try {
                HttpResponse<String> response = httpClient.send(toHttpRequest(), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    return ApiResponse.failure(String.format("Request failed with status code %d: %s", response.statusCode(), response.body()));
                }
                return ApiResponse.success(_gson.fromJson(response.body(), clazz));
            } catch (JsonSyntaxException ex) {
                return ApiResponse.failure("Failed to parse JSON response: " + ex.getMessage());
            } catch (Exception ex) {
                return ApiResponse.failure("Exception during API request: " + ex.getMessage());
            }
        }
    }

    @Override
    public URI getUri() {
        return _isPaginated
                ? URI.create(_config.baseUrl() + _endpoint + "?offset=" + _offset + "&limit=" + _limit)
                : URI.create(_config.baseUrl() + _endpoint);
    }

    static class Builder {
        private RedmineApiConfig _config;
        private String _endpoint;
        private EnumRedmineContentType _contentType = EnumRedmineContentType.JSON;
        private JsonObject _body = new JsonObject();
        private EnumHttpMethod _method = EnumHttpMethod.GET;
        private boolean _isPaginated = false;

        public static Builder create() {
            return new Builder();
        }

        public Builder withConfig(RedmineApiConfig config) {
            _config = config;
            return this;
        }

        public Builder withEndpoint(String endpoint) {
            _endpoint = "/" + endpoint;
            return this;
        }

        public Builder withContentType(EnumRedmineContentType contentType) {
            _contentType = contentType;
            return this;
        }

        public Builder withPagination() {
            _isPaginated = true;
            return this;
        }

        public Builder withBody(JsonObject body) {
            _body = body;
            return this;
        }

        public Builder withMethod(EnumHttpMethod method) {
            _method = method;
            return this;
        }

        public RedmineApiRequest build() {
            // Validate
            Objects.requireNonNull(_config, "RedmineApiConfig is required");
            Objects.requireNonNull(_endpoint, "Endpoint is required");
            Objects.requireNonNull(_method, "HttpMethod is required");

            return new RedmineApiRequest(_config, _endpoint, _contentType, _body, _method, _isPaginated);
        }
    }
}
