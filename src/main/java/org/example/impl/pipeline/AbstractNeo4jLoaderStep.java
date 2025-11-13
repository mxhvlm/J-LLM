package org.example.impl.pipeline;

import org.example.impl.integration.api.neo4j.INeo4jProvider;

public class AbstractNeo4jLoaderStep {

    protected final INeo4jProvider _neo4JProvider;

    public AbstractNeo4jLoaderStep(INeo4jProvider neo4JProvider) {
        _neo4JProvider = neo4JProvider;
    }
}
