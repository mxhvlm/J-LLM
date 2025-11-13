package org.example.integration.impl.redmine.wiki;

import org.example.integration.api.ITokenAuthConfig;
import org.example.integration.impl.ApiResponse;
import org.example.integration.api.EnumHttpMethod;
import org.example.integration.api.IApiResponse;
import org.example.integration.api.wiki.IWikiPage;
import org.example.integration.api.wiki.IWikiProvider;
import org.example.integration.impl.redmine.RedmineApiRequest;
import org.example.integration.impl.redmine.RedminePaginationDTO;
import org.example.integration.impl.shared.WikiPage;
import org.slf4j.Logger;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class RedmineProvider implements IWikiProvider {
    private final ITokenAuthConfig _apiConfig;
    private static final Logger _LOGGER = org.slf4j.LoggerFactory.getLogger(RedmineProvider.class);
    private final HttpClient _httpClient;

    public RedmineProvider(ITokenAuthConfig apiConfig) {
        _apiConfig = apiConfig;
        _httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    }

    @Override
    public IApiResponse<List<String>> getAllPageTitles(String projectName) {
        _LOGGER.info("Fetching all wiki pages for project: {}", projectName);
        return RedmineApiRequest.Builder.create()
                .withConfig(_apiConfig)
                .withMethod(EnumHttpMethod.GET)
                .withEndpoint("/projects/" + projectName + "/wiki/index")
                .withPagination()
                .build()
                .getResponse(_httpClient, RedminePageListDTO.class)
                .map(dto ->
                    ApiResponse.success(
                        dto.wiki_pages()
                            .stream()
                            .map(RedminePageListDTO.RedminePageListObjectDTO::title)
                            .toList()
                    ), ApiResponse::failure);
    }

    @Override
    public IApiResponse<Integer> getNumPages(String projectName) {
        _LOGGER.info("Fetching number of wiki pages for project: {}", projectName);
        return RedmineApiRequest.Builder.create()
                .withConfig(_apiConfig)
                .withMethod(EnumHttpMethod.GET)
                .withEndpoint("/projects/" + projectName + "/wiki/index")
                .build()
                .getResponse(_httpClient, RedminePaginationDTO.class)
                .map(dto -> ApiResponse.success(dto.total_count()), ApiResponse::failure);
    }

    @Override
    public IApiResponse<IWikiPage> getPageContent(String projectName, String pageTitle) {
        _LOGGER.info("Fetching wiki page '{}' for project: {}", pageTitle, projectName);

        return RedmineApiRequest.Builder.create()
                .withConfig(_apiConfig)
                .withMethod(EnumHttpMethod.GET)
                .withEndpoint("/projects/" + projectName + "/wiki/" + pageTitle)
                .build()
                .getResponse(_httpClient, RedminePageContentDTO.class)
                .map(dto -> {
                    _LOGGER.info("Received response for page '{}': {}", pageTitle, dto.text());

                    // Return a generic WikiPage object instead of the RedminePageContentDTO so we decouple the pipelines
                    // from the Redmine-specific implementation
                    return ApiResponse.success(
                            new WikiPage(
                                dto.title(),
                                dto.text(),
                                dto.author(),
                                dto.version(),
                                dto.createdOn(),
                                dto.updatedOn())
                    );
                }, ApiResponse::failure);
    }

    @Override
    public IApiResponse<List<IWikiPage>> getAllPages(String projectName) {
        _LOGGER.info("Fetching all wiki pages with content for project: {}", projectName);
        return getAllPageTitles(projectName).map(pageTitles -> {
            // GET all the individual wiki pages
            List<IApiResponse<IWikiPage>> pageResponses = pageTitles.stream()
                    .map(title -> getPageContent(projectName, title))
                    .toList();

            // Handle any failures while getting the page contents
            if (pageResponses.stream().anyMatch(IApiResponse::isFailure)) {
                String errorMessages = pageResponses.stream()
                        .filter(IApiResponse::isFailure)
                        .map(IApiResponse::getErrorMessage)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                        .orElse("Unknown error");
                _LOGGER.error("Errors occurred while fetching wiki pages: {}", errorMessages);
                return ApiResponse.failure("Failed to fetch some wiki pages: " + errorMessages);
            }

            // All page fetches were successful, return the list of WikiPage objects
            return ApiResponse.success(pageResponses
                    .stream()
                    .map(IApiResponse::getResponse)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList());
        }, ApiResponse::failure);
    }

    @Override
    public IApiResponse<List<String>> getPageTitlesPaginated(String projectName, int offset, int limit) {
        return RedmineApiRequest.Builder.create()
                .withPaginationOffset(offset)
                .withPaginationLimit(limit)
                .withConfig(_apiConfig)
                .withMethod(EnumHttpMethod.GET)
                .withEndpoint("/projects/" + projectName + "/wiki/index")
                .withPagination()
                .build()
                .getResponse(_httpClient, RedminePageListDTO.class)
                .map(dto ->
                    ApiResponse.success(dto.wiki_pages().stream().map(RedminePageListDTO.RedminePageListObjectDTO::title).toList()),
                        ApiResponse::failure);
    }

    @Override
    public IApiResponse<List<IWikiPage>> getPageContentPaginated(String projectName, int offset, int limit) {
        return getPageTitlesPaginated(projectName, offset, limit)
                .map(pageTitles -> {
                    List<IApiResponse<IWikiPage >> pageResponses = pageTitles.stream()
                            .map(title -> getPageContent(projectName, title))
                            .toList();

                    // Handle any failures while getting the page contents
                    if (pageResponses.stream().anyMatch(IApiResponse::isFailure)) {
                        String errorMessages = pageResponses.stream()
                                .filter(IApiResponse::isFailure)
                                .map(IApiResponse::getErrorMessage)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                                .orElse("Unknown error");
                        _LOGGER.error("Errors occurred while fetching wiki pages: {}", errorMessages);
                        return ApiResponse.failure("Failed to fetch some wiki pages: " + errorMessages);
                    }

                    // All page fetches were successful, return the list of WikiPage objects
                    return ApiResponse.success(pageResponses
                            .stream()
                            .map(IApiResponse::getResponse)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .toList());
                }, ApiResponse::failure);
    }
}