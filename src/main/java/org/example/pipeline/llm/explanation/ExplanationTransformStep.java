package org.example.pipeline.llm.explanation;

import org.example.data.neo4j.Neo4JLink;
import org.example.data.neo4j.Neo4jExplanation;
import org.example.data.neo4j.Neo4jMethodSummaryContextResult;
import org.example.llm.ILLMProvider;
import org.example.llm.LLMConfig;
import org.example.pipeline.llm.AbstractLLMTransformStep;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

class ExplanationTransformStep
    extends AbstractLLMTransformStep<
    Neo4jMethodSummaryContextResult,
      ExplanationTransformStep.ILLMExplanationStepOutput> {
  private final LLMConfig _config;
  public ExplanationTransformStep(ILLMProvider llmProvider, LLMConfig config) {
    super(llmProvider);
    _config = config;
  }

  public sealed interface ILLMExplanationStepOutput
      permits ExplanationTransformStep.ExplanationLink, ExplanationTransformStep.ExplanationNode {}

  record ExplanationNode(Neo4jExplanation object) implements ILLMExplanationStepOutput {}
  record ExplanationLink(Neo4JLink object) implements ILLMExplanationStepOutput {}

  /**
   * Gets the token limit to be passed to the LLM request based on the source code length
   * @param sourceCodeLength The length of the java source code
   * @return The maximum number of tokens the LLM can process
   */
  private int getMaxLLMResponseLength(int sourceCodeLength) {
    // TODO: Find curve that roughly fits the data
    // Something along the lines of ((sourceCodeLength - 40)^.9)
    if (sourceCodeLength < 40) return 0;
    else if (sourceCodeLength < 120) return 50;
    else if (sourceCodeLength < 220) return 150;
    else if (sourceCodeLength < 500) return 250;
    else return 312;
  }

  /**
   * Gets the max length of the LLM response in a human readable format
   * Required for building the LLM prompt.
   * The LLM performs slightly better when given a range of sentences compared to
   * number of chars or tokens as it has difficulties counting
   * @param sourceCodeLength The length of the java source code
   * @return A string containing a rough range of sentences
   */
  private String getMaxLLMLengthAsString(int sourceCodeLength) {
    if (sourceCodeLength < 120) return "1 sentence";
    else if (sourceCodeLength < 220) return "3-5 sentences";
    else if (sourceCodeLength < 500) return "around ten sentences";
    else return "ten to twenty sentences";
  }

    @Override
    public String getLLMQuery(Neo4jMethodSummaryContextResult input) {
        return String.format("Context: Context for this method is: \"%s\\nSource code: %s\\nPlease explain what this method body does in %s. Complex methods should have a more extensive / more detailed summary. Getters and the like only need one sentence. Only include the summary in your response, nothing else. DO NOT REPEAT the code. YOUR TASK IS TO WRITE THE SUMMARY / Explanation NOW. The method ",
            input.toStringWithoutSourceCode(), input.sourceCode(), getMaxLLMLengthAsString(input.sourceCode().length()));
    }

    @Override
    public LLMConfig getLLMConfig(Neo4jMethodSummaryContextResult input) {
        // Make the max tokens dynamic based on the source code length
        // so we get a summary adequate to the size of the method
        return LLMConfig.Builder.copyOf(_config)
            .withMaxTokens(getMaxLLMResponseLength(input.sourceCode().length()))
            .build();
    }

    @Override
    public Collection<ExplanationTransformStep.ILLMExplanationStepOutput> processLLMResult(Neo4jMethodSummaryContextResult input, String resp) {
      Neo4jExplanation explanation = new Neo4jExplanation(resp, UUID.randomUUID().toString(), new LinkedList<>());
      Neo4JLink link = Neo4JLink.Builder.create()
          .withLabel("HAS_EXPLANATION")
          .parentLabel("Method")
          .childLabel("Explanation")
          .betweenProps("id")
          .parentValue(input.methodId())
          .childValue(explanation.id())
          .build();

      return List.of(
          new ExplanationNode(explanation),
          new ExplanationLink(link)
      );
    }
}
