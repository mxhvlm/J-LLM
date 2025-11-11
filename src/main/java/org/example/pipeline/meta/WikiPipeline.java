package org.example.pipeline.meta;

import org.example.integration.llm.ILLMProvider;
import org.example.integration.llm.LLMConfig;
import org.example.integration.neo4j.Neo4jService;
import org.example.integration.wiki.IWikiProvider;
import org.example.integration.wiki.providerImpl.redmine.RedmineApiConfig;
import org.example.integration.wiki.providerImpl.redmine.RedmineProvider;
import org.example.pipeline.Pipeline;
import org.example.pipeline.TransformResult;
import org.example.pipeline.llm.AbstractLLMBatchedPipelineStep;
import org.example.pipeline.llm.wiki.TransformStep;
import org.example.pipeline.llm.wiki.WikiExtractorStep;
import org.slf4j.Logger;

import java.util.Arrays;

public class WikiPipeline extends AbstractLLMBatchedPipelineStep {
    private final Logger _LOGGER = org.slf4j.LoggerFactory.getLogger(WikiPipeline.class);
    private int _currCursor;
    private final String _space;
    private final IWikiProvider _wikiProvider;

    public WikiPipeline(int batchSize, LLMConfig config, ILLMProvider llmProvider, String space, IWikiProvider wikiProvider) {
        super(batchSize, config, llmProvider);
        _currCursor = 0;
        _space = space;
        _wikiProvider = wikiProvider;
    }

    @Override
    protected long getNumTotal(Neo4jService neo4jService) {
        return 1000; // TODO: IMPLEMENT PROPERLY!!!
    }

    @Override
    protected Pipeline<Integer, TransformResult> getPipeline(Neo4jService neo4jService) {
        return Pipeline
                .start(new WikiExtractorStep(_space, _wikiProvider))
//                .then(new TransformStep(_llmProvider, _llmConfig))
                .then(input -> {
                    String[] content = input.toArray(String[]::new);
                    _LOGGER.info("Processing wiki data: {}", Arrays.toString(content));
                    _currCursor += _batchSize;
                    return Arrays.stream(content);
                })
                .then(input -> new TransformResult())
                .build();
    }
}
