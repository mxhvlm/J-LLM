package org.example.impl.integration.impl.atlassian.confluence;

import org.apache.commons.lang3.NotImplementedException;
import org.example.impl.datamodel.api.wiki.IWikiPage;
import org.example.impl.integration.api.IApiResponse;
import org.example.impl.integration.api.wiki.IWikiProvider;

import java.util.List;

public class ConfluenceProvider implements IWikiProvider {

    @Override
    public IApiResponse<List<String>> getAllPageTitles(String projectName) {
        throw new NotImplementedException();
    }

    @Override
    public IApiResponse<Integer> getNumPages(String projectName) {
        return null;
    }

    @Override
    public IApiResponse<IWikiPage> getPageContent(String projectName, String pageTitle) {
        throw new NotImplementedException();
    }

    @Override
    public IApiResponse<List<IWikiPage>> getAllPages(String projectName) {
        throw new NotImplementedException();
    }

    @Override
    public IApiResponse<List<String>> getPageTitlesPaginated(String projectName, int offset, int limit) {
        return null;
    }

    @Override
    public IApiResponse<List<IWikiPage>> getPageContentPaginated(String projectName, int offset, int limit) {
        return null;
    }
}
