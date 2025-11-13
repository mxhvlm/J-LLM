package org.example.impl.datamodel.impl.wiki;

import org.example.impl.datamodel.api.wiki.IWikiPage;

import java.util.Date;

public record WikiPage(String title, String content, String author, String version, Date created,
                       Date updated) implements IWikiPage {
}
