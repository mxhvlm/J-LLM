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
import java.util.HashMap;
import java.util.Objects;

class RedmineApiRequest implements IApiRequest {
    private final RedmineApiConfig _config;
    private final String _endpoint;
    private final EnumRedmineContentType _contentType;
    private final JsonObject _body;
    private final EnumHttpMethod _method;
    private final boolean _isPaginated;
    private int _offset;
    private final int _limit;
    private final Gson _gson;
    private final HashMap<Object, Object> _params;

    private RedmineApiRequest(
            RedmineApiConfig config,
            String endpoint,
            EnumRedmineContentType contentType,
            JsonObject body,
            EnumHttpMethod method,
            boolean isPaginated,
            int offset,
            int limit,
            HashMap<Object, Object> params) {
        _config = config;
        _endpoint = endpoint;
        _contentType = contentType;
        _body = body;
        _method = method;
        _isPaginated = isPaginated;
        _params = params;
        _gson = new Gson();
        _offset = offset;
        _limit = limit;
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

                    RedminePaginationDTO responseDto = _gson.fromJson(response.body(), RedminePaginationDTO.class);

                    allItems.addAll(responseDto.items());

                    if (_offset + _limit >= responseDto.total_count()) { //TODO: Properly implement totalCount
                        return ApiResponse.success(_gson.fromJson(allItems, clazz)); // Deserialize to ensure type correctness
                    } else {
                        _offset += responseDto.limit();
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
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append(_config.baseUrl());
        if (!uriBuilder.toString().endsWith("/") && !_endpoint.startsWith("/")) {
            uriBuilder.append("/");
        }
        uriBuilder.append(_endpoint);

        if (_isPaginated || !_params.isEmpty()) {
            uriBuilder.append("?");
            if (_isPaginated) {
                uriBuilder.append("offset=").append(_offset).append("&limit=").append(_limit);
            }
            for (var entry : _params.entrySet()) {
                if (uriBuilder.charAt(uriBuilder.length() - 1) != '?') {
                    uriBuilder.append("&");
                }
                uriBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }

        return URI.create(uriBuilder.toString());
    }

    static class Builder {
        private RedmineApiConfig _config;
        private String _endpoint;
        private EnumRedmineContentType _contentType = EnumRedmineContentType.JSON;
        private JsonObject _body = new JsonObject();
        private EnumHttpMethod _method = EnumHttpMethod.GET;
        private boolean _isPaginated = false;
        private int _offset = 0;
        private int _limit = 100;
        private HashMap<Object, Object> _params = new HashMap<>();

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

        public Builder withPaginationOffset(int offset) {
            _offset = offset;
            return this;
        }

        public Builder withParams(HashMap<Object, Object> params) {
            _params.clear();
            _params.putAll(params);
            return this;
        }

        public Builder withPaginationLimit(int limit) {
            _limit = limit;
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

            return new RedmineApiRequest(_config, _endpoint, _contentType, _body, _method, _isPaginated, _offset, _limit, _params);
        }
    }
}
