package org.example.integration.wiki.providerImpl;

import org.apache.commons.lang3.NotImplementedException;
import org.example.integration.wiki.IWikiPage;
import org.example.integration.wiki.IWikiProvider;

import java.util.List;

public class ConfluenceProvider implements IWikiProvider {
    @Override
    public List<IWikiPage> getAllPages() {
        throw new NotImplementedException();
    }
}
