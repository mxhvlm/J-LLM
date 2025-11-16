package com.mxhvlm.jllm.core.integration.impl.atlassian.confluence;

import com.mxhvlm.jllm.core.datamodel.api.wiki.IWikiPage;
import com.mxhvlm.jllm.core.integration.api.IApiResponse;
import com.mxhvlm.jllm.core.integration.api.wiki.IWikiProvider;
import org.apache.commons.lang3.NotImplementedException;

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
