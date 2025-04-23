package org.example.pipeline.spoon.field;

import org.apache.commons.lang3.tuple.Pair;
import org.example.data.Neo4JLinkObject;
import org.example.data.Neo4jFieldObject;
import org.example.dbOutput.Neo4jService;
import org.example.pipeline.AbstractNeo4jLoaderStep;
import org.example.pipeline.PipelineStep;
import org.example.pipeline.TransformResult;
import org.neo4j.driver.Values;

import java.util.List;

public class FieldLoaderStep extends AbstractNeo4jLoaderStep
        implements PipelineStep<Pair<List<Neo4jFieldObject>, List<Neo4JLinkObject>>, TransformResult> {

    public FieldLoaderStep(Neo4jService neo4jService) {
        super(neo4jService);
    }

    public void createFieldNode(Neo4jFieldObject field) {
        String fieldNodeId = field.getDeclaringTypeName() + "." + field.getFieldName();
        _neo4jService.runCypher(
                "MERGE (f:Field {id: $fieldId}) " +
                        "SET f.name = $fieldName, f.modifiers = $modifiers, f.sourceCodeSnippet = $sourceSnippet",
                Values.parameters("fieldId", fieldNodeId, "fieldName", field.getFieldName(),
                        "modifiers", field.getModifiers(), "sourceSnippet", field.getFieldSourceSnippet())
        );
    }

    @Override
    public TransformResult process(Pair<List<Neo4jFieldObject>, List<Neo4JLinkObject>> input) {
        _neo4jService.startSessionAndTransaction();
        input.getLeft().forEach(this::createFieldNode);
        input.getRight().forEach(_neo4jService::creatLinkNode);
        _neo4jService.commitTransactionAndCloseSession();
        return new TransformResult();
    }
}
