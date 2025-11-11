package org.example.integration.wiki;

import java.util.Date;

public record WikiPage(String title, String content, String author, String version, Date created, Date updated) {
}
