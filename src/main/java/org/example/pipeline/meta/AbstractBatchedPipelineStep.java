package org.example.pipeline.meta;

import org.example.dbOutput.Neo4jService;
import org.example.pipeline.Pipeline;
import org.example.pipeline.TransformResult;
import org.slf4j.Logger;

public abstract class AbstractBatchedPipelineStep extends AbstractNeo4jMetaStep {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(
        AbstractBatchedPipelineStep.class);
    protected final int _batchSize;

    public AbstractBatchedPipelineStep( int batchSize) {
        _batchSize = batchSize;
    }

    protected abstract long getNumTotal(Neo4jService neo4jService);

    @Override
    public Neo4jService process(Neo4jService neo4jService) {
        // Set up the pipeline
        Pipeline<Integer, TransformResult> pipeline = getPipeline(
            neo4jService);

        // Run the pipeline once for every batch until no more methods without an explanation are found
        long total = getNumTotal(neo4jService);
        long processed = 0;
        LOGGER.info("Total elements to process: {}", total);
        while (processed < total) {
            pipeline.run(_batchSize);
            processed += _batchSize;

            LOGGER.info("Processed {} of {} methods ({}%)", processed, total,
                (double) processed / total * 100);
        }
        return neo4jService;
    }

    protected abstract Pipeline<Integer, TransformResult> getPipeline(Neo4jService neo4jService);
}
