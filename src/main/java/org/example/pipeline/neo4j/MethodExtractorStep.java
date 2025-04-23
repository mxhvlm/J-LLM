package org.example.pipeline.neo4j;

import org.apache.commons.lang3.NotImplementedException;
import org.example.data.Neo4jMethodObject;
import org.example.dbOutput.Neo4jService;
import org.example.pipeline.PipelineStep;

import java.util.List;

public class MethodExtractorStep implements PipelineStep<Neo4jService, List<Neo4jMethodObject>> {
    @Override
    public List<Neo4jMethodObject> process(Neo4jService input) {
        throw new NotImplementedException("MethodExtractorStep is not implemented yet");
    }
}
