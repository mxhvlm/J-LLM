package org.example.impl.integration.api.wiki;

import org.example.impl.datamodel.api.wiki.IWikiPage;
import org.example.impl.integration.api.IApiResponse;

import java.util.List;

public interface IWikiProvider {
    IApiResponse<List<String>> getAllPageTitles(String projectName);

    IApiResponse<Integer> getNumPages(String projectName);

    IApiResponse<IWikiPage> getPageContent(String projectName, String pageTitle);

    IApiResponse<List<IWikiPage>> getAllPages(String projectName);

    IApiResponse<List<String>> getPageTitlesPaginated(String projectName, int offset, int limit);

    IApiResponse<List<IWikiPage>> getPageContentPaginated(String projectName, int offset, int limit);
}