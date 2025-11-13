package org.example.pipeline.llm.wiki;

import org.example.integration.api.wiki.IWikiPage;
import org.example.integration.api.wiki.IWikiProvider;
import org.example.pipeline.IPipelineStep;

import java.util.stream.Stream;

public class WikiExtractorStep implements IPipelineStep<Integer, Stream<String>> {
    private final String _space;
    private final IWikiProvider _wikiProvider;
    private final int _offset;

    public WikiExtractorStep(String space, IWikiProvider wikiProvider, int offset){
        _space = space;
        _wikiProvider = wikiProvider;
        _offset = offset;
    }

    @Override
    public Stream<String> process(Integer batchSize) {
        return _wikiProvider.getPageContentPaginated(_space, _offset, batchSize)
            .map(pages -> pages.stream().map(IWikiPage::content),
                err -> {
                    throw new RuntimeException("Failed to fetch wiki pages: " + err);
                });
    }
}
