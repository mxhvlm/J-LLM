package org.example.integration.wiki;

import org.example.integration.IApiResponse;

import java.util.List;

public interface IWikiProvider {
    IApiResponse<List<String>> getAllPageTitles(String projectName);
    IApiResponse<WikiPage> getPageContent(String projectName, String pageTitle);
    IApiResponse<List<WikiPage>> getAllPages(String projectName);
}