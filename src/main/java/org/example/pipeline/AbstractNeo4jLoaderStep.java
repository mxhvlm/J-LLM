package org.example.pipeline;

import org.example.dbOutput.Neo4jService;

public class AbstractNeo4jLoaderStep {

    protected final Neo4jService _neo4jService;

    public AbstractNeo4jLoaderStep(Neo4jService neo4jService) {
        _neo4jService = neo4jService;
    }
}
