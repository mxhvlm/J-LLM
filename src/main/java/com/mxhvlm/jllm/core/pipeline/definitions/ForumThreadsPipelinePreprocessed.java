package com.mxhvlm.jllm.core.pipeline.definitions;

import com.mxhvlm.jllm.core.datamodel.api.knowledge.ProcessedForumThread;
import com.mxhvlm.jllm.core.integration.api.neo4j.INeo4jProvider;
import com.mxhvlm.jllm.core.pipeline.IPipelineStep;
import com.mxhvlm.jllm.core.pipeline.Pipeline;
import com.mxhvlm.jllm.core.pipeline.step.JsonExtractorStep;
import com.mxhvlm.jllm.core.pipeline.step.llm.forumThreads.LoadStepPreprocessed;
import com.mxhvlm.jllm.core.pipeline.step.llm.forumThreads.TransformStepPreprocessed;

public class ForumThreadsPipelinePreprocessed implements IPipelineStep<INeo4jProvider, INeo4jProvider> {
    private final String _filePath;

    public ForumThreadsPipelinePreprocessed(String filePath) {
        _filePath = filePath;
    }

    @Override
    public INeo4jProvider process(INeo4jProvider neo4JProvider) {
        Pipeline
                .start(new JsonExtractorStep<>(ProcessedForumThread.class, _filePath))
                .then(new TransformStepPreprocessed(neo4JProvider))
                .then(new LoadStepPreprocessed(neo4JProvider))
                .build()
                .run(0);
        return neo4JProvider;
    }
}
