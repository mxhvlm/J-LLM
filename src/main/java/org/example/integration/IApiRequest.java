package org.example.integration;

import com.google.gson.JsonObject;

import java.net.http.HttpRequest;

public interface IApiRequest {
    JsonObject toJson();
    HttpRequest toHttpRequest();
}
