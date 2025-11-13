package com.mxhvlm.jllm.core.pipeline.definitions;

import com.google.gson.JsonParser;
import com.mxhvlm.jllm.core.datamodel.api.knowledge.ProcessedForumThread;
import com.mxhvlm.jllm.core.integration.api.llm.ILLMConfig;
import com.mxhvlm.jllm.core.integration.api.llm.ILLMProvider;
import com.mxhvlm.jllm.core.integration.api.neo4j.INeo4jProvider;
import com.mxhvlm.jllm.core.pipeline.Pipeline;
import com.mxhvlm.jllm.core.pipeline.TransformResult;
import com.mxhvlm.jllm.core.pipeline.step.JsonExtractorStep;
import com.mxhvlm.jllm.core.pipeline.step.llm.AbstractLLMBatchedPipelineStep;
import com.mxhvlm.jllm.core.pipeline.step.llm.forumThreads.LoadStep;
import com.mxhvlm.jllm.core.pipeline.step.llm.forumThreads.TransformStep;

import java.io.FileReader;
import java.io.IOException;

public class ForumThreadsPipeline extends AbstractLLMBatchedPipelineStep {
    private final String _filePath;
    private int _currCursor;

    public ForumThreadsPipeline(int batchSize, ILLMConfig config,
                                ILLMProvider llmProvider, String filePath) {
        super(batchSize, config, llmProvider);
        _filePath = filePath;
        _currCursor = 0;
    }

    @Override
    protected long getNumTotal(INeo4jProvider neo4JProvider) {
        try (FileReader reader = new FileReader(_filePath)) {
            return JsonParser.parseReader(reader).getAsJsonArray().size();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read input file for counting JSON objects", e);
        }
    }

    @Override
    protected Pipeline<Integer, TransformResult> getPipeline(INeo4jProvider neo4JProvider) {
        return Pipeline
                .start(new JsonExtractorStep<>(ProcessedForumThread.class, _filePath, _currCursor, _batchSize))
                .then(new TransformStep(_llmProvider, _llmConfig, neo4JProvider))
                .then(new LoadStep(neo4JProvider))
                .then(input -> {
                    _currCursor += _batchSize;
                    return input;
                })
                .build();
    }
}
