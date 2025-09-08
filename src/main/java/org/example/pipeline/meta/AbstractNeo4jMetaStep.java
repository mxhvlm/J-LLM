package org.example.pipeline.meta;

import org.example.interop.neo4j.Neo4jService;
import org.example.pipeline.IPipelineStep;

public abstract class AbstractNeo4jMetaStep implements IPipelineStep<Neo4jService, Neo4jService> {

  public AbstractNeo4jMetaStep() {

  }
}
