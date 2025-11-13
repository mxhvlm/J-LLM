package org.example.impl.pipeline.meta;

import org.example.impl.datamodel.api.knowledge.ProcessedForumThread;
import org.example.impl.integration.api.neo4j.INeo4jProvider;
import org.example.impl.pipeline.IPipelineStep;
import org.example.impl.pipeline.JsonExtractorStep;
import org.example.impl.pipeline.Pipeline;
import org.example.impl.pipeline.llm.forumThreads.LoadStepPreprocessed;
import org.example.impl.pipeline.llm.forumThreads.TransformStepPreprocessed;

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
