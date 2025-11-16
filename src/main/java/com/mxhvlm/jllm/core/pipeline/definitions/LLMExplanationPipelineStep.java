package com.mxhvlm.jllm.core.pipeline.definitions;

import com.mxhvlm.jllm.core.integration.api.llm.ILLMConfig;
import com.mxhvlm.jllm.core.integration.api.llm.ILLMProvider;
import com.mxhvlm.jllm.core.integration.api.neo4j.INeo4jProvider;
import com.mxhvlm.jllm.core.pipeline.Pipeline;
import com.mxhvlm.jllm.core.pipeline.TransformResult;
import com.mxhvlm.jllm.core.pipeline.step.llm.AbstractLLMBatchedPipelineStep;
import com.mxhvlm.jllm.core.pipeline.step.llm.explanation.ExplanationLoadStep;
import com.mxhvlm.jllm.core.pipeline.step.llm.explanation.ExplanationTransformStep;
import com.mxhvlm.jllm.core.pipeline.step.llm.neo4j.MethodContextExtractorStep;

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
