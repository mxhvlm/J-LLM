package org.example.pipeline.llm.wiki;

import org.example.integration.wiki.IWikiProvider;
import org.example.integration.wiki.WikiPage;
import org.example.pipeline.IPipelineStep;

import java.util.stream.Stream;

public class WikiExtractorStep implements IPipelineStep<Integer, Stream<String>> {
    private final String _space;
    private final IWikiProvider _wikiProvider;

    public WikiExtractorStep(String space, IWikiProvider wikiProvider){
        _space = space;
        _wikiProvider = wikiProvider;
    }

    @Override
    public Stream<String> process(Integer input) {
        return _wikiProvider.getAllPages(_space)
            .map(pages -> pages.stream().map(WikiPage::content),
                err -> {
                    throw new RuntimeException("Failed to fetch wiki pages: " + err);
                });
    }
}
