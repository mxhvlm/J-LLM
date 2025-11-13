package org.example.integration.impl.shared;

import org.example.integration.api.wiki.IWikiPage;

import java.util.Date;

public record WikiPage(String title, String content, String author, String version, Date created, Date updated) implements IWikiPage {
}
