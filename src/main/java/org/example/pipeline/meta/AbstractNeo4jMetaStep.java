package org.example.pipeline.meta;

import org.example.integration.api.neo4j.INeo4jProvider;
import org.example.integration.impl.neo4j.Neo4jProvider;
import org.example.pipeline.IPipelineStep;

public abstract class AbstractNeo4jMetaStep implements IPipelineStep<INeo4jProvider, INeo4jProvider> {

  public AbstractNeo4jMetaStep() {

  }
}
