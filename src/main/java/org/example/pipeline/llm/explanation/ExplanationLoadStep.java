package org.example.pipeline.llm.explanation;

import org.example.data.neo4j.Neo4jExplanation;
import org.example.dbOutput.Neo4jService;
import org.example.pipeline.AbstractNeo4jLoaderStep;
import org.example.pipeline.IPipelineStep;
import org.example.pipeline.TransformResult;
import org.neo4j.driver.Values;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExplanationLoadStep extends AbstractNeo4jLoaderStep
    implements IPipelineStep<Stream<ExplanationTransformStep.ILLMExplanationStepOutput>, TransformResult> {
  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ExplanationLoadStep.class);
  public ExplanationLoadStep(Neo4jService neo4jService) {
    super(neo4jService);
  }

  private void createExplanationNode(Neo4jExplanation expNode) {
    String expNodeId = expNode.id();
    _neo4jService.runCypher("MERGE (e:Explanation {id: $expId}) " +
        "SET e.text = $text",
        Values.parameters(
            "expId", expNodeId,
            "text", expNode.explanationText()
        ));
  }

  @Override
  public TransformResult process(Stream<ExplanationTransformStep.ILLMExplanationStepOutput> input) {
    Map<Boolean, List<ExplanationTransformStep.ILLMExplanationStepOutput>> partitions = input.collect(
        Collectors.partitioningBy(i -> i instanceof ExplanationTransformStep.ExplanationLink));
    LOGGER.info("ExplanationLoader: Loading {} explanations...",
        partitions.get(false).size());

    _neo4jService.beginTransaction();
    partitions.get(false).forEach(expNode -> {
      createExplanationNode(((ExplanationTransformStep.ExplanationNode) expNode).object());
    });

    LOGGER.info("ExplanationLoader: Loading {} explanation links...",
        partitions.get(true).size());
    partitions.get(true).forEach(expLink -> {
      _neo4jService.creatLinkNode(((ExplanationTransformStep.ExplanationLink) expLink).object());
    });
    _neo4jService.commitTransactionIfPresent();
    return null;
  }
}
