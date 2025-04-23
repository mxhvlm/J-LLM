package org.example.pipeline.spoon.type;

import org.apache.commons.lang3.tuple.Pair;
import org.example.data.Neo4JLinkObject;
import org.example.data.Neo4jTypeObject;
import org.example.dbOutput.Neo4jService;
import org.example.pipeline.AbstractNeo4jLoaderStep;
import org.example.pipeline.PipelineStep;
import org.example.pipeline.TransformResult;
import org.neo4j.driver.Values;

import java.util.List;

public class TypeLoaderStep extends AbstractNeo4jLoaderStep
        implements PipelineStep<Pair<List<Neo4jTypeObject>, List<Neo4JLinkObject>>, TransformResult> {

    public TypeLoaderStep(Neo4jService neo4jService) {
        super(neo4jService);
    }

    private void createTypeNode(Neo4jTypeObject node) {
        // Using MERGE because we might reference the same type multiple times:
        _neo4jService.runCypher(
                "MERGE (t:Type { name: $typeName }) " +
                        "SET t.simpleName = $simpleName, " +
                        "    t.typeKind = $typeKind, " +
                        "    t.modifiers = $modifiers, " +
                        "    t.javadoc = $javadoc",
                Values.parameters("typeName", node.getTypeName(), "simpleName", node.getSimpleName(),
                        "typeKind", node.getTypeKind(), "modifiers", node.getModifiers(), "javadoc", node.getJavadoc())
        );
    }

    private void createPrimitiveTypeNodes() {
        String[] primitiveTypes = {"int", "float", "double", "boolean", "char", "byte", "short", "long"};
        for (String primitiveType : primitiveTypes) {
            createTypeNode(new Neo4jTypeObject(primitiveType, primitiveType, "PRIMITIVE", "public", ""));
        }
    }

    @Override
    public TransformResult process(Pair<List<Neo4jTypeObject>, List<Neo4JLinkObject>> input) {
        _neo4jService.startSessionAndTransaction();

        createPrimitiveTypeNodes();
        input.getLeft().forEach(this::createTypeNode);
        input.getRight().forEach(_neo4jService::creatLinkNode);
        _neo4jService.commitTransactionAndCloseSession();

        return new TransformResult();
    }
}
