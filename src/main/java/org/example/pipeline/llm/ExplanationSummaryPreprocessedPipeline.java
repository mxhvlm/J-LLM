package org.example.pipeline.llm;

import org.example.datamodel.knowledge.PreprocessedMethodAnalysis;
import org.example.datamodel.neo4j.Neo4JLink;
import org.example.datamodel.neo4j.Neo4jExplanation;
import org.example.datamodel.neo4j.Neo4jSummary;
import org.example.interop.neo4j.Neo4jService;
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

public class ExplanationSummaryPreprocessedPipeline implements IPipelineStep<Neo4jService, Neo4jService> {
  public sealed interface IExplanationPreprocessedOutput
      permits ExplanationNode, Link, SummaryNode {}

  private static final Logger LOG = LoggerFactory.getLogger(ExplanationSummaryPreprocessedPipeline.class);

  record ExplanationNode(Neo4jExplanation object) implements IExplanationPreprocessedOutput {}
  record SummaryNode(Neo4jSummary object) implements IExplanationPreprocessedOutput {}
  record Link(Neo4JLink object) implements IExplanationPreprocessedOutput {}

  private void createExplanationNode(Neo4jService neo4jService, Neo4jExplanation expNode) {
    String expNodeId = expNode.id();
    neo4jService.runCypher("MERGE (e:Explanation {id: $expId}) " +
            "SET e.text = $text",
        Values.parameters(
            "expId", expNodeId,
            "text", expNode.explanationText()
        ));
  }

  private void createSummaryNode(Neo4jService neo4jService, Neo4jSummary summaryNode) {
    String sumNodeId = summaryNode.id();
    neo4jService.runCypher("MERGE (s:Summary {id: $sumId}) " +
            "SET s.text = $text",
        Values.parameters(
            "sumId", sumNodeId,
            "text", summaryNode.summaryText()
        ));
  }

  @Override
  public Neo4jService process(Neo4jService neo4jService) {
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
        })
        .then(input12 -> {
          Map<Boolean, List<IExplanationPreprocessedOutput>> partitions = input12.collect(
              Collectors.partitioningBy(i -> i instanceof Link));

          LOG.info("ExplanationSummaryPreprocessedPipeline: Loading {} nodes...", partitions.get(false).size());

          for (int i = 0; i < partitions.get(false).size(); i++) {
            if (i % 1000 == 0) {
              neo4jService.commitTransactionIfPresent();
              neo4jService.beginTransaction();
              LOG.info("ExplanationSummaryPreprocessedPipeline: Loaded {} nodes...", i);
            }
            IExplanationPreprocessedOutput node = partitions.get(false).get(i);
            if (node instanceof ExplanationNode expNode) {
              createExplanationNode(neo4jService, expNode.object());
            } else if (node instanceof SummaryNode sumNode) {
              createSummaryNode(neo4jService, sumNode.object());
            }
          }
          neo4jService.beginTransaction();
          LOG.info("ExplanationSummaryPreprocessedPipeline: Loading {} links...", partitions.get(true).size());
          for (int i = 0; i < partitions.get(true).size(); i++) {
            if (i % 1000 == 0) {
              neo4jService.commitTransactionIfPresent();
              neo4jService.beginTransaction();
              LOG.info("ExplanationSummaryPreprocessedPipeline: Loaded {} links...", i);
            }
            if (partitions.get(true).get(i) instanceof Link expLink) {
              neo4jService.creatLinkNode(expLink.object());
            }
          }
          neo4jService.commitTransactionIfPresent();

          return new TransformResult();
        }).build().run(0);
    return neo4jService;
  }
}
