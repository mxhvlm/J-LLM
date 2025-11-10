package org.example.pipeline.meta;

import org.example.datamodel.knowledge.ProcessedForumThread;
import org.example.integration.neo4j.Neo4jService;
import org.example.pipeline.IPipelineStep;
import org.example.pipeline.JsonExtractorStep;
import org.example.pipeline.Pipeline;
import org.example.pipeline.llm.forumThreads.LoadStepPreprocessed;
import org.example.pipeline.llm.forumThreads.TransformStepPreprocessed;

public class ForumThreadsPipelinePreprocessed implements IPipelineStep<Neo4jService, Neo4jService> {
  private final String _filePath;

  public ForumThreadsPipelinePreprocessed(String filePath) {
    _filePath = filePath;
  }

  @Override
  public Neo4jService process(Neo4jService neo4jService) {
    Pipeline
        .start(new JsonExtractorStep<>(ProcessedForumThread.class, _filePath))
        .then(new TransformStepPreprocessed(neo4jService))
        .then(new LoadStepPreprocessed(neo4jService))
        .build()
        .run(0);
    return neo4jService;
  }
}
