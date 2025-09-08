package org.example.pipeline.llm.neo4j;

import org.example.interop.neo4j.Neo4jService;
import org.example.pipeline.IPipelineStep;
import org.neo4j.driver.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateEmbeddingStep implements IPipelineStep<Neo4jService, Neo4jService> {
  private static final Logger LOG = LoggerFactory.getLogger(CreateEmbeddingStep.class);
  private final String _nodeLabel;
  private final String _nodeProperty;
  private final String _indexName;

  public CreateEmbeddingStep(String nodeLabel, String nodeProperty, String indexName) {
    this._nodeLabel = nodeLabel;
    this._nodeProperty = nodeProperty;
    this._indexName = indexName;
  }

  private void generateEmbedding(Neo4jService neo4jService) {
    neo4jService.beginTransaction();
    neo4jService.runCypher("""
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
    neo4jService.commitTransactionIfPresent();
  }

  @Override
  public Neo4jService process(Neo4jService neo4jService) {
    LOG.info("Creating vector index for {} nodes...", _nodeLabel);
    generateEmbedding(neo4jService);
    LOG.info("Vector index for {} nodes created successfully.", _nodeLabel);
    return neo4jService;
  }
}
