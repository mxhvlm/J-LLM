package org.example.integration;

import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

public interface IApiRequest {
    <U> IApiResponse<U> getResponse(HttpClient httpClient, Class<U> clazz);
    URI getUri();
}
