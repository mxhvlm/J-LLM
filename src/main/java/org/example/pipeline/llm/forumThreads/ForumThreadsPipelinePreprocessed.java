package org.example.pipeline.llm.forumThreads;

import com.google.gson.JsonParser;
import org.example.data.knowledge.ProcessedForumThread;
import org.example.dbOutput.Neo4jService;
import org.example.llm.ILLMProvider;
import org.example.llm.LLMConfig;
import org.example.pipeline.IPipelineStep;
import org.example.pipeline.JsonExtractorStep;
import org.example.pipeline.Pipeline;
import org.example.pipeline.TransformResult;
import org.example.pipeline.llm.AbstractLLMBatchedPipelineStep;
import org.example.pipeline.meta.AbstractNeo4jMetaStep;

import java.io.FileReader;
import java.io.IOException;

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
