package com.mxhvlm.jllm.core.pipeline.step.llm.neo4j;

import com.mxhvlm.jllm.core.integration.api.neo4j.INeo4jProvider;
import com.mxhvlm.jllm.core.pipeline.IPipelineStep;
import org.neo4j.driver.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateEmbeddingStep implements IPipelineStep<INeo4jProvider, INeo4jProvider> {
    private static final Logger LOG = LoggerFactory.getLogger(CreateEmbeddingStep.class);
    private final String _nodeLabel;
    private final String _nodeProperty;
    private final String _indexName;

    public CreateEmbeddingStep(String nodeLabel, String nodeProperty, String indexName) {
        this._nodeLabel = nodeLabel;
        this._nodeProperty = nodeProperty;
        this._indexName = indexName;
    }

    private void generateEmbedding(INeo4jProvider neo4JProvider) {
        neo4JProvider.beginTransaction();
        neo4JProvider.runCypher("""
                        CREATE VECTOR INDEX $indexName IF NOT EXISTS
                        FOR (d:$label)
                        ON (d.$property)
                        OPTIONS {
                          indexConfig: {
                            `vector.dimensions`: 1536,
                            `vector.similarity_function`: 'cosine'
                          }
                        };
                        """,
                Values.parameters(
                        "indexName", _indexName,
                        "label", _nodeLabel,
                        "property", _nodeProperty
                ));
        neo4JProvider.commitTransactionIfPresent();
    }

    @Override
    public INeo4jProvider process(INeo4jProvider neo4JProvider) {
        LOG.info("Creating vector index for {} nodes...", _nodeLabel);
        generateEmbedding(neo4JProvider);
        LOG.info("Vector index for {} nodes created successfully.", _nodeLabel);
        return neo4JProvider;
    }
}
