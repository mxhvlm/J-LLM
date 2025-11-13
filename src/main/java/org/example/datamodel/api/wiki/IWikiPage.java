package org.example.datamodel.api.wiki;

import java.util.Date;

public interface IWikiPage {
    String title();

    String content();

    String author();

    String version();

    Date created();

    Date updated();
}
