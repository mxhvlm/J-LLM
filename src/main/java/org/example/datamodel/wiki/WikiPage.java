package org.example.datamodel.wiki;

import java.util.Date;

public record WikiPage(String title, String content, String author, String version, Date created, Date updated) implements IWikiPage {
}
