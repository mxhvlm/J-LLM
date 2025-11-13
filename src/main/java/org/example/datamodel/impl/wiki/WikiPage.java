package org.example.datamodel.impl.wiki;

import org.example.datamodel.api.wiki.IWikiPage;

import java.util.Date;

public record WikiPage(String title, String content, String author, String version, Date created,
                       Date updated) implements IWikiPage {
}
