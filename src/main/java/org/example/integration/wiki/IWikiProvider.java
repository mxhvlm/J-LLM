package org.example.integration.wiki;

import java.util.List;

public interface IWikiProvider {
    List<IWikiPage> getAllPages();
}