package org.example.pipeline.meta;

import com.google.gson.JsonParser;
import org.example.data.knowledge.ProcessedForumThread;
import org.example.dbOutput.Neo4jService;
import org.example.llm.ILLMProvider;
import org.example.llm.LLMConfig;
import org.example.pipeline.JsonExtractorStep;
import org.example.pipeline.Pipeline;
import org.example.pipeline.TransformResult;
import org.example.pipeline.llm.AbstractLLMBatchedPipelineStep;
import org.example.pipeline.llm.forumThreads.LoadStep;
import org.example.pipeline.llm.forumThreads.TransformStep;

import java.io.FileReader;
import java.io.IOException;

public class ForumThreadsPipeline extends AbstractLLMBatchedPipelineStep {
  private int _currCursor;
  private final String _filePath;

  public ForumThreadsPipeline(int batchSize, LLMConfig config,
      ILLMProvider llmProvider, String filePath) {
    super(batchSize, config, llmProvider);
    _filePath = filePath;
    _currCursor = 0;
  }

  @Override
  protected long getNumTotal(Neo4jService neo4jService) {
    try (FileReader reader = new FileReader(_filePath)) {
      return JsonParser.parseReader(reader).getAsJsonArray().size();
    } catch (IOException e) {
      throw new RuntimeException("Failed to read input file for counting JSON objects", e);
    }
  }

  @Override
  protected Pipeline<Integer, TransformResult> getPipeline(Neo4jService neo4jService) {
    return Pipeline
        .start(new JsonExtractorStep<>(ProcessedForumThread.class, _filePath, _currCursor, _batchSize))
        .then(new TransformStep(_llmProvider, _config, neo4jService))
        .then(new LoadStep(neo4jService))
        .then(input -> {
          _currCursor += _batchSize;
          return input;
        })
        .build();
  }
}
