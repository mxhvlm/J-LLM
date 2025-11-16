package com.mxhvlm.jllm.core.pipeline.definitions;

import com.mxhvlm.jllm.core.integration.api.neo4j.INeo4jProvider;
import com.mxhvlm.jllm.core.pipeline.IPipelineStep;

public abstract class AbstractNeo4jMetaStep implements IPipelineStep<INeo4jProvider, INeo4jProvider> {

    public AbstractNeo4jMetaStep() {

    }
}
