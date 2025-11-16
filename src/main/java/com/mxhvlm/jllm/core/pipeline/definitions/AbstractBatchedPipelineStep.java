package com.mxhvlm.jllm.core.pipeline.definitions;

import com.mxhvlm.jllm.core.integration.api.neo4j.INeo4jProvider;
import com.mxhvlm.jllm.core.pipeline.Pipeline;
import com.mxhvlm.jllm.core.pipeline.TransformResult;
import org.slf4j.Logger;

public abstract class AbstractBatchedPipelineStep extends AbstractNeo4jMetaStep {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
            AbstractBatchedPipelineStep.class);
    protected final int _batchSize;

    public AbstractBatchedPipelineStep(int batchSize) {
        _batchSize = batchSize;
    }

    protected abstract long getNumTotal(INeo4jProvider neo4JProvider);

    @Override
    public INeo4jProvider process(INeo4jProvider neo4JProvider) {
        // Set up the pipeline
        Pipeline<Integer, TransformResult> pipeline = getPipeline(
                neo4JProvider);

        // Run the pipeline once for every batch until no more methods without an explanation are found
        long total = getNumTotal(neo4JProvider);
        long processed = 0;
        LOGGER.info("Total elements to process: {}", total);
        while (processed < total) {
            pipeline.run(_batchSize);
            processed += _batchSize;

            LOGGER.info("Processed {} of {} methods ({}%)", processed, total,
                    (double) processed / total * 100);
        }
        return neo4JProvider;
    }

    protected abstract Pipeline<Integer, TransformResult> getPipeline(INeo4jProvider neo4JProvider);
}
