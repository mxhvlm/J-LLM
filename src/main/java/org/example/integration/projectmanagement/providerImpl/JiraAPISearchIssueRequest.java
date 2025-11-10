package org.example.integration.projectmanagement.providerImpl;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.NotImplementedException;
import org.example.integration.IApiRequest;

import java.net.http.HttpRequest;

public class JiraAPISearchIssueRequest implements IApiRequest {
    private final String _jqlQuery;
    private final JiraApiConfig _apiConfig;

    private JiraAPISearchIssueRequest(JiraApiConfig apiConfig, String jqlQuery) {
        _apiConfig  = apiConfig;
        _jqlQuery = jqlQuery;
    }

    public static JiraAPISearchIssueRequest from(JiraApiConfig apiConfig, String jqlQuery) {
        return new JiraAPISearchIssueRequest(apiConfig, jqlQuery);
    }

    @Override
    public JsonObject toJson() {
        throw new NotImplementedException();
    }

    public HttpRequest toHttpRequest() {
        throw new NotImplementedException();
    }
}
