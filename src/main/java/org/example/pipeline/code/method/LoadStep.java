package org.example.pipeline.code.method;

import org.example.datamodel.neo4j.Neo4jMethod;
import org.example.datamodel.neo4j.Neo4jParam;
import org.example.integration.neo4j.Neo4jService;
import org.example.pipeline.AbstractNeo4jLoaderStep;
import org.example.pipeline.IPipelineStep;
import org.example.pipeline.TransformResult;
import org.neo4j.driver.Values;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoadStep extends AbstractNeo4jLoaderStep
    implements IPipelineStep<Stream<TransformerStep.IMethodTransformerOutput>, TransformResult> {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LoadStep.class);
    public LoadStep(Neo4jService neo4jService) {
        super(neo4jService);
    }

    private void createMethodNode(Neo4jMethod method) {
        String methodNodeId = method.declaringTypeName() + "#" + method.methodSignature();

        _neo4jService.runCypher("MERGE (m:Method {id: $methodId}) " +
                "SET m.declaringType = $declaringType, m.name = $name, m.signature = $signature, " +
                "    m.modifiers = $modifiers, m.sourceCode = $sourceCode, m.javadoc = $javadoc",
            Values.parameters(
                "methodId", methodNodeId,
                "declaringType", method.declaringTypeName(),
                "name", method.methodName(),
                "signature", method.methodSignature(),
                "modifiers", method.modifiers(),
                "sourceCode", method.sourceCode(),
                "javadoc", method.javadoc()
            ));
    }

    private void createParameterNode(Neo4jParam param) {
        _neo4jService.runCypher(
            "MERGE (p:Parameter {id: $paramId}) " +
                "SET p.name = $paramName, " + // Ensure "name" is set
                "    p.displayName = $paramName",  // Optionally add displayName for clarity
            Values.parameters("paramId", param.id(), "paramName", param.id())
        );
    }

    @Override
    public TransformResult process(
        Stream<TransformerStep.IMethodTransformerOutput> input) {
        LOGGER.info("MethodExporter: Loading methods...");

        _neo4jService.beginTransaction();
        // TODO: instanceof check is expensive, consider using a more efficient way to partition
        Map<Boolean, List<TransformerStep.IMethodTransformerOutput>> partitions = input.collect(
                Collectors.partitioningBy(i -> i instanceof TransformerStep.MethodLinkNode));

        // First create all nodes
        LOGGER.info("MethodExporter: Creating method nodes...");

        // Debug method called ERROR that all the invalid links get linked to for easy investigation
        // TODO: Only add this if were in debug mode
//        createMethodNode(
//            new Neo4jMethodObject("ERROR", "ERROR", "ERROR", "ERROR", "ERROR", "ERROR"));

        partitions.get(false)
            .forEach(methodTransformerOutput -> {
                if (methodTransformerOutput instanceof TransformerStep.MethodNode methodNode) {
                    createMethodNode(methodNode.object());
                } else if (methodTransformerOutput instanceof TransformerStep.MethodParamObject methodParam) {
                    createParameterNode(methodParam.object());
                }
            });
        _neo4jService.commitTransactionIfPresent();

        // Then create all links
        LOGGER.info("MethodExporter: Creating method links...");

        _neo4jService.beginTransaction();
        partitions.get(true)
            .forEach(methodTransformerOutput -> {
                if (methodTransformerOutput instanceof TransformerStep.MethodLinkNode methodLink) {
                    _neo4jService.creatLinkNode(methodLink.link());
                }
            });
        _neo4jService.commitTransactionIfPresent();
        return new TransformResult();
    }
}
