package org.example.pipeline.llm.oneSentenceSummary;

import org.example.datamodel.impl.neo4j.Neo4jSummary;
import org.example.integration.api.neo4j.INeo4jProvider;
import org.example.pipeline.AbstractNeo4jLoaderStep;
import org.example.pipeline.IPipelineStep;
import org.example.pipeline.TransformResult;
import org.neo4j.driver.Values;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SummaryLoadStep extends AbstractNeo4jLoaderStep
    implements IPipelineStep<Stream<SummaryTransformStep.ILLMSummaryStepOutput>, TransformResult> {
  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SummaryLoadStep.class);
  public SummaryLoadStep(INeo4jProvider neo4JProvider) {
    super(neo4JProvider);
  }

  private void createSummaryNode(Neo4jSummary summaryNode) {
    String sumNodeId = summaryNode.id();
    _neo4JProvider.runCypher("MERGE (s:Summary {id: $sumId}) " +
        "SET s.text = $text",
        Values.parameters(
            "sumId", sumNodeId,
            "text", summaryNode.summaryText()
        ));
  }

  @Override
  public TransformResult process(Stream<SummaryTransformStep.ILLMSummaryStepOutput> input) {
    Map<Boolean, List<SummaryTransformStep.ILLMSummaryStepOutput>> partitions = input.collect(
        Collectors.partitioningBy(i -> i instanceof SummaryTransformStep.SummaryLink));
    LOGGER.info("SummaryLoader: Loading {} summaries...",
        partitions.get(false).size());

    _neo4JProvider.beginTransaction();
    partitions.get(false).forEach(sumNode -> {
      createSummaryNode(((SummaryTransformStep.SummaryNode) sumNode).object());
    });

    LOGGER.info("SummaryLoader: Loading {} summary links...",
        partitions.get(true).size());
    partitions.get(true).forEach(sumLink -> {
      _neo4JProvider.creatLinkNode(((SummaryTransformStep.SummaryLink) sumLink).object());
    });
    _neo4JProvider.commitTransactionIfPresent();
    return null;
  }
}
