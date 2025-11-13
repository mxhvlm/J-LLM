package org.example.integration.impl.redmine.projectmanagement;

import org.example.integration.api.EnumHttpMethod;
import org.example.integration.api.IApiResponse;
import org.example.integration.api.ITokenAuthConfig;
import org.example.integration.api.projectmanagement.IProjectManagementProvider;
import org.example.datamodel.projectmanagement.ITicket;
import org.example.integration.impl.ApiResponse;
import org.example.integration.impl.redmine.RedmineApiRequest;
import org.example.datamodel.projectmanagement.Ticket;
import org.slf4j.Logger;

import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RedmineProvider implements IProjectManagementProvider {
    private final ITokenAuthConfig _apiConfig;
    private static final Logger _LOGGER = org.slf4j.LoggerFactory.getLogger(RedmineProvider.class);
    private final HttpClient _httpClient;

    public RedmineProvider(ITokenAuthConfig apiConfig) {
        _apiConfig = apiConfig;
        _httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    }

    @Override
    public IApiResponse<List<ITicket>> getAllTickets() {
        _LOGGER.info("Fetching all tickets from Redmine...");
        return RedmineApiRequest.Builder.create()
                .withConfig(_apiConfig)
                .withMethod(EnumHttpMethod.GET)
                .withEndpoint("/issues")
                .withPagination()
                .build()
                .getResponse(_httpClient, RedmineIssueListDTO.class)
                .map(dto-> ApiResponse.success(dto.issues().stream()
                        .map(issue -> new Ticket(issue.getAllFields()))
                        .collect(Collectors.toUnmodifiableList())),
                    ApiResponse::failure);
    }

    @Override
    public IApiResponse<List<ITicket>> getTicketsForProject(String projectIdentifier) {
        _LOGGER.info("Fetching tickets for project: {}", projectIdentifier);
        return RedmineApiRequest.Builder.create()
                .withConfig(_apiConfig)
                .withMethod(EnumHttpMethod.GET)
                .withEndpoint("/issues")
                .withQueryParam("project_id", projectIdentifier)
                .withPagination()
                .build()
                .getResponse(_httpClient, RedmineIssueListDTO.class)
                .map(dto-> ApiResponse.success(dto.issues().stream()
                        .map(issue -> new Ticket(issue.getAllFields()))
                        .collect(Collectors.toUnmodifiableList())),
                    ApiResponse::failure);
    }

    /**
     * Fetch tickets based on a query string.
     * @param query The query string to filter tickets within the project management system.
     *              Within Redmine, there is no jql-like query language, instead filtering is done through query params.
     *              Example: GET /issues.xml
     *                  GET /issues.xml?issue_id=1
     *                  GET /issues.xml?issue_id=1,2
     *                  GET /issues.xml?project_id=2
     *                  GET /issues.xml?project_id=2&tracker_id=1
     *                  GET /issues.xml?assigned_to_id=6
     *                  GET /issues.xml?assigned_to_id=me
     *                  GET /issues.xml?status_id=closed
     *                  GET /issues.xml?status_id=*
     *                  GET /issues.xml?cf_1=abcdef
     *                  GET /issues.xml?sort=category:desc,updated_on
     *              Therefore, this method expects a String of query parameters to be appended to the /issues endpoint.
     * @return A response containing a list of tickets matching the query.
     */
    @Override
    public IApiResponse<List<ITicket>> getTicketsByQuery(String query) {
        _LOGGER.info("Fetching tickets by query: {}", query);

        // Convert the query string into individual query parameters
        Map<String, String> queryParams = parseQuery(query);

        return RedmineApiRequest.Builder.create()
                .withConfig(_apiConfig)
                .withMethod(EnumHttpMethod.GET)
                .withEndpoint("/issues")
                .withQueryParams(queryParams)
                .withPagination()
                .build()
                .getResponse(_httpClient, RedmineIssueListDTO.class)
                .map(dto-> ApiResponse.success(dto.issues().stream()
                        .map(issue -> new Ticket(issue.getAllFields()))
                        .collect(Collectors.toUnmodifiableList())),
                    ApiResponse::failure);
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();

        if (query == null || query.isEmpty())
            return map;

        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2); // limit=2 handles value containing '='
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length > 1
                    ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8)
                    : "";
            map.put(key, value);
        }

        return map;
    }
}
