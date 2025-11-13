package org.example.impl.pipeline.meta;

import org.example.impl.integration.api.neo4j.INeo4jProvider;
import org.example.impl.pipeline.IPipelineStep;

public abstract class AbstractNeo4jMetaStep implements IPipelineStep<INeo4jProvider, INeo4jProvider> {

    public AbstractNeo4jMetaStep() {

    }
}
