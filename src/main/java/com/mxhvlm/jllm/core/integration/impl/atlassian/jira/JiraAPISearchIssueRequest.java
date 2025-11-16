package com.mxhvlm.jllm.core.integration.impl.atlassian.jira;

import com.mxhvlm.jllm.core.integration.api.IApiRequest;
import com.mxhvlm.jllm.core.integration.api.IApiResponse;
import org.apache.commons.lang3.NotImplementedException;

import java.net.URI;
import java.net.http.HttpClient;

public class JiraAPISearchIssueRequest implements IApiRequest {
    private final String _jqlQuery;
    private final JiraApiConfig _apiConfig;
    private final String _endpoint = "/rest/api/2/search";

    private JiraAPISearchIssueRequest(JiraApiConfig apiConfig, String jqlQuery) {
        _apiConfig = apiConfig;
        _jqlQuery = jqlQuery;
    }

    public static JiraAPISearchIssueRequest from(JiraApiConfig apiConfig, String jqlQuery) {
        return new JiraAPISearchIssueRequest(apiConfig, jqlQuery);
    }

    @Override
    public <U> IApiResponse<U> getResponse(HttpClient httpClient, Class<U> clazz) {
        throw new NotImplementedException("Not implemented yet");
    }

    @Override
    public URI getUri() {
        throw new NotImplementedException("Not implemented yet");
    }
}
