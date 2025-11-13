package org.example.integration.wiki;

import org.example.integration.IApiResponse;

import java.util.List;

public interface IWikiProvider {
    IApiResponse<List<String>> getAllPageTitles(String projectName);
    IApiResponse<Integer> getNumPages(String projectName);
    IApiResponse<WikiPage> getPageContent(String projectName, String pageTitle);
    IApiResponse<List<WikiPage>> getAllPages(String projectName);
    IApiResponse<List<String>> getPageTitlesPaginated(String projectName, int offset, int limit);
    IApiResponse<List<WikiPage>> getPageContentPaginated(String projectName, int offset, int limit);
}