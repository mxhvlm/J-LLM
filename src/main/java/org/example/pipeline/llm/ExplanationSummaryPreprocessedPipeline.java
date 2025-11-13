package org.example.pipeline.llm;

import org.example.datamodel.api.knowledge.PreprocessedMethodAnalysis;
import org.example.datamodel.impl.neo4j.Neo4JLink;
import org.example.datamodel.impl.neo4j.Neo4jExplanation;
import org.example.datamodel.impl.neo4j.Neo4jSummary;
import org.example.integration.api.neo4j.INeo4jProvider;
import org.example.pipeline.IPipelineStep;
import org.example.pipeline.JsonExtractorStep;
import org.example.pipeline.Pipeline;
import org.example.pipeline.TransformResult;
import org.neo4j.driver.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ExplanationSummaryPreprocessedPipeline implements IPipelineStep<INeo4jProvider, INeo4jProvider> {
  public sealed interface IExplanationPreprocessedOutput
      permits ExplanationNode, Link, SummaryNode {}

  private static final Logger LOG = LoggerFactory.getLogger(ExplanationSummaryPreprocessedPipeline.class);

  record ExplanationNode(Neo4jExplanation object) implements IExplanationPreprocessedOutput {}
  record SummaryNode(Neo4jSummary object) implements IExplanationPreprocessedOutput {}
  record Link(Neo4JLink object) implements IExplanationPreprocessedOutput {}

  private void createExplanationNode(INeo4jProvider neo4JProvider, Neo4jExplanation expNode) {
    String expNodeId = expNode.id();
    neo4JProvider.runCypher("MERGE (e:Explanation {id: $expId}) " +
            "SET e.text = $text",
        Values.parameters(
            "expId", expNodeId,
            "text", expNode.explanationText()
        ));
  }

  private void createSummaryNode(INeo4jProvider neo4JProvider, Neo4jSummary summaryNode) {
    String sumNodeId = summaryNode.id();
    neo4JProvider.runCypher("MERGE (s:Summary {id: $sumId}) " +
            "SET s.text = $text",
        Values.parameters(
            "sumId", sumNodeId,
            "text", summaryNode.summaryText()
        ));
  }

  @Override
  public INeo4jProvider process(INeo4jProvider neo4JProvider) {
    Pipeline
        .start(new JsonExtractorStep<>(PreprocessedMethodAnalysis.class, "methods_out_both.json"))
        .then(analysisStream -> {
          List<PreprocessedMethodAnalysis> analyses = analysisStream.toList();
          List<IExplanationPreprocessedOutput> ret = new LinkedList<>();

          analyses.forEach(a -> {
            Neo4jExplanation exp = new Neo4jExplanation(a.explanation(), UUID.randomUUID().toString(), List.of());
            Neo4jSummary sum = new Neo4jSummary(a.oneSentenceSummary(), UUID.randomUUID().toString(), List.of());
            ret.add(new ExplanationNode(exp));
            ret.add(new SummaryNode(sum));
            ret.add(new Link(Neo4JLink.Builder.create()
                .withLabel("HAS_EXPLANATION")
                .parentLabel("Method")
                .childLabel("Explanation")
                .betweenProps("id")
                .parentValue(a.methodId())
                .childValue(exp.id())
                .build()));
            ret.add(new Link(Neo4JLink.Builder.create()
                .withLabel("HAS_ONE_SENTENCE_SUMMARY")
                .parentLabel("Method")
                .childLabel("Summary")
                .betweenProps("id")
                .parentValue(a.methodId())
                .childValue(sum.id())
                .build()));
          });

          return ret.stream();
        }).then(input12 -> {
          Map<Boolean, List<IExplanationPreprocessedOutput>> partitions = input12.collect(
              Collectors.partitioningBy(i -> i instanceof Link));

          LOG.info("ExplanationSummaryPreprocessedPipeline: Loading {} nodes...", partitions.get(false).size());

          for (int i = 0; i < partitions.get(false).size(); i++) {
            if (i % 1000 == 0) {
              neo4JProvider.commitTransactionIfPresent();
              neo4JProvider.beginTransaction();
              LOG.info("ExplanationSummaryPreprocessedPipeline: Loaded {} nodes...", i);
            }
            IExplanationPreprocessedOutput node = partitions.get(false).get(i);
            if (node instanceof ExplanationNode expNode) {
              createExplanationNode(neo4JProvider, expNode.object());
            } else if (node instanceof SummaryNode sumNode) {
              createSummaryNode(neo4JProvider, sumNode.object());
            }
          }
          neo4JProvider.beginTransaction();
          LOG.info("ExplanationSummaryPreprocessedPipeline: Loading {} links...", partitions.get(true).size());
          for (int i = 0; i < partitions.get(true).size(); i++) {
            if (i % 1000 == 0) {
              neo4JProvider.commitTransactionIfPresent();
              neo4JProvider.beginTransaction();
              LOG.info("ExplanationSummaryPreprocessedPipeline: Loaded {} links...", i);
            }
            if (partitions.get(true).get(i) instanceof Link expLink) {
              neo4JProvider.creatLinkNode(expLink.object());
            }
          }
          neo4JProvider.commitTransactionIfPresent();

          return new TransformResult();
        }).build().run(0);
    return neo4JProvider;
  }
}
