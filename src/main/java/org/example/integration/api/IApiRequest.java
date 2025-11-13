package org.example.integration.api;

import java.net.URI;
import java.net.http.HttpClient;

public interface IApiRequest {
    <U> IApiResponse<U> getResponse(HttpClient httpClient, Class<U> clazz);

    URI getUri();
}
