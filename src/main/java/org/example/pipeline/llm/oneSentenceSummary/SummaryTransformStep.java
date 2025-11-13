package org.example.pipeline.llm.oneSentenceSummary;

import org.example.datamodel.impl.neo4j.Neo4JLink;
import org.example.datamodel.impl.neo4j.Neo4jMethodSummaryContextResult;
import org.example.datamodel.impl.neo4j.Neo4jSummary;
import org.example.integration.api.llm.ILLMProvider;
import org.example.integration.api.llm.LLMConfig;
import org.example.pipeline.llm.AbstractLLMTransformStep;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

class SummaryTransformStep
    extends AbstractLLMTransformStep<
      Neo4jMethodSummaryContextResult,
    SummaryTransformStep.ILLMSummaryStepOutput> {

  public SummaryTransformStep(ILLMProvider llmProvider, LLMConfig config) {
    super(llmProvider, config);
  }

  public sealed interface ILLMSummaryStepOutput
      permits SummaryLink, SummaryNode {}

  record SummaryNode(Neo4jSummary object) implements ILLMSummaryStepOutput {}
  record SummaryLink(Neo4JLink object) implements ILLMSummaryStepOutput {}

    @Override
    public String getLLMQuery(Neo4jMethodSummaryContextResult input) {
        return String.format("Context: Context for this method is: \"%s\\n\\nSource Code: %s\\nPlease explain what this method body does in one sentence. Only include the summary in your response, nothing else. DO NOT REPEAT the code. YOUR TASK IS TO WRITE THE ONE SENTENCE SUMMARY NOW. The method ",
            input.toStringWithoutSourceCode(), input.sourceCode());

    }

    @Override
    public LLMConfig getLLMConfig(Neo4jMethodSummaryContextResult input) {
        return _config;
    }

    @Override
    public Collection<ILLMSummaryStepOutput> processLLMResult(Neo4jMethodSummaryContextResult input, String resp) {
      Neo4jSummary summary = new Neo4jSummary(resp, UUID.randomUUID().toString(), new LinkedList<>());
      Neo4JLink link = Neo4JLink.Builder.create()
          .withLabel("HAS_ONE_SENTENCE_SUMMARY")
          .parentLabel("Method")
          .childLabel("Summary")
          .betweenProps("id")
          .parentValue(input.methodId())
          .childValue(summary.id())
          .build();

      return List.of(
          new SummaryNode(summary),
          new SummaryLink(link)
      );
    }
}
