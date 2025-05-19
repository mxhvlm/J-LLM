package org.example.pipeline.llm.explanation;

import org.example.dbOutput.Neo4jService;
import org.example.llm.ILLMProvider;
import org.example.llm.LLMConfig;
import org.example.pipeline.TransformResult;
import org.example.pipeline.llm.AbstractLLMBatchedPipelineStep;
import org.example.pipeline.llm.MethodContextExtractorStep;
import org.example.pipeline.Pipeline;

public class LLMExplanationPipelineStep extends AbstractLLMBatchedPipelineStep {
  public LLMExplanationPipelineStep(ILLMProvider llmProvider, LLMConfig config, int batchSize) {
    super(batchSize, config, llmProvider);
  }

  @Override
  protected long getNumTotal(Neo4jService neo4jService) {
    neo4jService.beginTransaction();
    long result = neo4jService
        .runCypher("MATCH (m:Method) RETURN count(m) AS count")
        .stream()
        .findFirst()
        .map(record -> record.get("count").asLong())
        .orElseThrow(() -> new IllegalStateException("Failed to get total methods to process"));
    neo4jService.commitTransactionIfPresent();
    return result;
  }

  @Override
  protected Pipeline<Integer, TransformResult> getPipeline(Neo4jService neo4jService) {
    return  Pipeline
        .start(new MethodContextExtractorStep(neo4jService))
        .then(new ExplanationTransformStep(_llmProvider, _config))
        .then(new ExplanationLoadStep(neo4jService))
        .build();
  }
}
