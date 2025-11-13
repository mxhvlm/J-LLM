package org.example.impl.pipeline.code.type;

import org.example.impl.datamodel.impl.neo4j.Neo4JLink;
import org.example.impl.datamodel.impl.neo4j.Neo4jType;
import org.example.impl.integration.api.neo4j.INeo4jProvider;
import org.example.impl.pipeline.AbstractNeo4jLoaderStep;
import org.example.impl.pipeline.IPipelineStep;
import org.example.impl.pipeline.TransformResult;
import org.neo4j.driver.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LoaderStep extends AbstractNeo4jLoaderStep
        implements IPipelineStep<Stream<TransformerStep.ITypeOutput>, TransformResult> {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LoaderStep.class);

    public LoaderStep(INeo4jProvider neo4JProvider) {
        super(neo4JProvider);
    }

    private void createTypeNode(Neo4jType node) {
//             Using MERGE because we might reference the same type multiple times:
        _neo4JProvider.runCypher(
                "MERGE (t:Type { name: $typeName }) " +
                        "SET t.simpleName = $simpleName, " +
                        "    t.typeKind = $typeKind, " +
                        "    t.modifiers = $modifiers, " +
                        "    t.javadoc = $javadoc",
                Values.parameters("typeName", node.typeName(), "simpleName", node.simpleName(),
                        "typeKind", node.typeKind(), "modifiers", node.modifiers(), "javadoc", node.javadoc())
        );
    }

    private void createPrimitiveTypeNodes() {
        String[] primitiveTypes = {"int", "float", "double", "boolean", "char", "byte", "short", "long"};
        for (String primitiveType : primitiveTypes) {
            createTypeNode(new Neo4jType(primitiveType, primitiveType, "PRIMITIVE", "public", ""));
        }
    }

    @Override
    public TransformResult process(Stream<TransformerStep.ITypeOutput> input) {
//        _neo4jService.startSessionAndTransaction();
        LOGGER.info("TypeExporter: Loading types...");

        _neo4JProvider.beginTransaction();

        createPrimitiveTypeNodes();
        List<TransformerStep.TypeNode> nodes = new ArrayList<>();
        List<Neo4JLink> links = new ArrayList<>();
        input.forEach(output -> {
            if (output instanceof TransformerStep.TypeNode tn) {
                nodes.add(tn);
            } else if (output instanceof TransformerStep.TypeLink tl) {
                links.add(tl.link());
            }
        });
        // Phase 1: Insert all types
        nodes.forEach(n -> createTypeNode(n.object()));

        // Phase 2: Insert all links (after all nodes exist)
        links.forEach(_neo4JProvider::creatLinkNode);

        _neo4JProvider.commitTransactionIfPresent();

        LOGGER.info("TypeExporter: Loaded " + nodes.size() + " types and " + links.size() + " links.");
        return new TransformResult();
    }
}
