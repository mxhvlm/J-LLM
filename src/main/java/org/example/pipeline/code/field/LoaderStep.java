package org.example.pipeline.code.field;

import org.example.datamodel.neo4j.Neo4jField;
import org.example.integration.neo4j.Neo4jService;
import org.example.pipeline.AbstractNeo4jLoaderStep;
import org.example.pipeline.IPipelineStep;
import org.example.pipeline.TransformResult;
import org.neo4j.driver.Values;

import java.util.stream.Stream;

public class LoaderStep extends AbstractNeo4jLoaderStep
        implements IPipelineStep<Stream<TransformerStep.IFieldTransformerOutput>, TransformResult> {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoaderStep.class);
    public LoaderStep(Neo4jService neo4jService) {
        super(neo4jService);
    }

    public void createFieldNode(Neo4jField field) {
        String fieldNodeId = field.declaringTypeName() + "." + field.fieldName();
        _neo4jService.runCypher(
                "MERGE (f:Field {id: $fieldId}) " +
                        "SET f.name = $fieldName, f.modifiers = $modifiers, f.sourceCodeSnippet = $sourceSnippet",
                Values.parameters("fieldId", fieldNodeId, "fieldName", field.fieldName(),
                        "modifiers", field.modifiers(), "sourceSnippet", field.fieldSourceSnippet())
        );
    }

    @Override
    public TransformResult process(Stream<TransformerStep.IFieldTransformerOutput> input) {
        LOG.info("FieldExporter: Loading fields...");
        _neo4jService.beginTransaction();
        input.forEach(i -> {
            if (i instanceof TransformerStep.FieldNode fieldNode) {
                createFieldNode(fieldNode.object());
            } else if (i instanceof TransformerStep.FieldLink fieldLink) {
                _neo4jService.creatLinkNode(fieldLink.link());
            }
        });
        _neo4jService.commitTransactionIfPresent();
        return new TransformResult();
    }
}
