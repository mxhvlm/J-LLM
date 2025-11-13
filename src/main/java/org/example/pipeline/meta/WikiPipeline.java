package org.example.pipeline.meta;

import org.example.integration.api.llm.ILLMProvider;
import org.example.integration.api.llm.LLMConfig;
import org.example.integration.api.neo4j.INeo4jProvider;
import org.example.integration.impl.neo4j.Neo4jProvider;
import org.example.integration.api.wiki.IWikiProvider;
import org.example.pipeline.Pipeline;
import org.example.pipeline.TransformResult;
import org.example.pipeline.llm.AbstractLLMBatchedPipelineStep;
import org.example.pipeline.llm.wiki.WikiExtractorStep;
import org.slf4j.Logger;

import java.util.Arrays;

public class WikiPipeline extends AbstractLLMBatchedPipelineStep {
    private final Logger _LOGGER = org.slf4j.LoggerFactory.getLogger(WikiPipeline.class);
    private int _currOffset;
    private final String _space;
    private final IWikiProvider _wikiProvider;

    public WikiPipeline(int batchSize, LLMConfig config, ILLMProvider llmProvider, String space, IWikiProvider wikiProvider) {
        super(batchSize, config, llmProvider);
        _currOffset = 0;
        _space = space;
        _wikiProvider = wikiProvider;
    }

    @Override
    protected long getNumTotal(INeo4jProvider neo4JProvider) {
        return _wikiProvider.getNumPages(_space)
            .map(count -> (long) count, err -> {
                _LOGGER.error("Failed to get number of wiki pages: {}", err);
                return 0L;
            });
    }

    @Override
    protected Pipeline<Integer, TransformResult> getPipeline(INeo4jProvider neo4JProvider) {
        return Pipeline
                .start(new WikiExtractorStep(_space, _wikiProvider, _currOffset))
//                .then(new TransformStep(_llmProvider, _llmConfig))
                .then(input -> {
                    String[] content = input.toArray(String[]::new);
                    _LOGGER.info("Processing wiki data: {}", Arrays.toString(content));
                    _currOffset += _batchSize;
                    return Arrays.stream(content);
                })
                .then(input -> new TransformResult())
                .build();
    }
}
