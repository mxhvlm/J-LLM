package com.mxhvlm.jllm.core.datamodel.impl.wiki;

import com.mxhvlm.jllm.core.datamodel.api.wiki.IWikiPage;

import java.util.Date;

public record WikiPage(String title, String content, String author, String version, Date created,
                       Date updated) implements IWikiPage {
}
