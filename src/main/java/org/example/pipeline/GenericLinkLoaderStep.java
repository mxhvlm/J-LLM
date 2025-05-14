package org.example.pipeline;

import org.example.data.Neo4JLinkObject;
import org.example.dbOutput.Neo4jService;

import java.util.List;

public class GenericLinkLoaderStep implements PipelineStep<List<Neo4JLinkObject>, TransformResult> {
    private final Neo4jService _neo4jService;

    public GenericLinkLoaderStep(Neo4jService neo4jService) {
        _neo4jService = neo4jService;
    }

    @Override
    public TransformResult process(List<Neo4JLinkObject> input) {
        input.forEach(_neo4jService::creatLinkNode);
        return new TransformResult();
    }
}
