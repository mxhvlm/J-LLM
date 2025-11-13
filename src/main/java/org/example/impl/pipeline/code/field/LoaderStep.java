package org.example.impl.pipeline.code.field;

import org.example.impl.datamodel.impl.neo4j.Neo4jField;
import org.example.impl.integration.api.neo4j.INeo4jProvider;
import org.example.impl.pipeline.AbstractNeo4jLoaderStep;
import org.example.impl.pipeline.IPipelineStep;
import org.example.impl.pipeline.TransformResult;
import org.neo4j.driver.Values;

import java.util.stream.Stream;

public class LoaderStep extends AbstractNeo4jLoaderStep
        implements IPipelineStep<Stream<TransformerStep.IFieldTransformerOutput>, TransformResult> {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoaderStep.class);

    public LoaderStep(INeo4jProvider neo4JProvider) {
        super(neo4JProvider);
    }

    public void createFieldNode(Neo4jField field) {
        String fieldNodeId = field.declaringTypeName() + "." + field.fieldName();
        _neo4JProvider.runCypher(
                "MERGE (f:Field {id: $fieldId}) " +
                        "SET f.name = $fieldName, f.modifiers = $modifiers, f.sourceCodeSnippet = $sourceSnippet",
                Values.parameters("fieldId", fieldNodeId, "fieldName", field.fieldName(),
                        "modifiers", field.modifiers(), "sourceSnippet", field.fieldSourceSnippet())
        );
    }

    @Override
    public TransformResult process(Stream<TransformerStep.IFieldTransformerOutput> input) {
        LOG.info("FieldExporter: Loading fields...");
        _neo4JProvider.beginTransaction();
        input.forEach(i -> {
            if (i instanceof TransformerStep.FieldNode fieldNode) {
                createFieldNode(fieldNode.object());
            } else if (i instanceof TransformerStep.FieldLink fieldLink) {
                _neo4JProvider.creatLinkNode(fieldLink.link());
            }
        });
        _neo4JProvider.commitTransactionIfPresent();
        return new TransformResult();
    }
}
