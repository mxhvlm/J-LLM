package org.example.impl.pipeline.meta;

import org.example.impl.integration.api.llm.ILLMConfig;
import org.example.impl.integration.api.llm.ILLMProvider;
import org.example.impl.integration.api.neo4j.INeo4jProvider;
import org.example.impl.pipeline.Pipeline;
import org.example.impl.pipeline.TransformResult;
import org.example.impl.pipeline.llm.AbstractLLMBatchedPipelineStep;
import org.example.impl.pipeline.llm.explanation.ExplanationLoadStep;
import org.example.impl.pipeline.llm.explanation.ExplanationTransformStep;
import org.example.impl.pipeline.llm.neo4j.MethodContextExtractorStep;

public class LLMExplanationPipelineStep extends AbstractLLMBatchedPipelineStep {
    public LLMExplanationPipelineStep(ILLMProvider llmProvider, ILLMConfig config, int batchSize) {
        super(batchSize, config, llmProvider);
    }

    @Override
    protected long getNumTotal(INeo4jProvider neo4JProvider) {
        neo4JProvider.beginTransaction();
        long result = neo4JProvider
                .runCypher("MATCH (m:Method) RETURN count(m) AS count")
                .stream()
                .findFirst()
                .map(record -> record.get("count").asLong())
                .orElseThrow(() -> new IllegalStateException("Failed to get total methods to process"));
        neo4JProvider.commitTransactionIfPresent();
        return result;
    }

    @Override
    protected Pipeline<Integer, TransformResult> getPipeline(INeo4jProvider neo4JProvider) {
        return Pipeline
                .start(new MethodContextExtractorStep(neo4JProvider))
                .then(new ExplanationTransformStep(_llmProvider, _llmConfig))
                .then(new ExplanationLoadStep(neo4JProvider))
                .build();
    }
}
