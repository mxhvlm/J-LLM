package org.example.integration.wiki.providerImpl.confluence;

import org.apache.commons.lang3.NotImplementedException;
import org.example.integration.IApiResponse;
import org.example.integration.wiki.IWikiProvider;
import org.example.integration.wiki.WikiPage;

import java.util.List;

public class ConfluenceProvider implements IWikiProvider {

    @Override
    public IApiResponse<List<String>> getAllPageTitles(String projectName) {
        throw new NotImplementedException();
    }

    @Override
    public IApiResponse<WikiPage> getPageContent(String projectName, String pageTitle) {
        throw new NotImplementedException();
    }

    @Override
    public IApiResponse<List<WikiPage>> getAllPages(String projectName) {
        throw new NotImplementedException();
    }
}
