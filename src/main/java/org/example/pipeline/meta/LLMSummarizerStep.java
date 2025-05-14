package org.example.pipeline.meta;

import org.example.data.Neo4jMethodObject;
import org.example.dbOutput.Neo4jService;
import org.example.llm.ILLMConfig;
import org.example.llm.ILLMProvider;
import org.example.llm.providerImpl.KoboldCppProvider;
import org.example.pipeline.Pipeline;
import org.example.pipeline.llm.AbstractLLMTransformStep;
import org.example.pipeline.neo4j.MethodExtractorStep;
import org.example.pipeline.spoon.method.MethodLoadStep;
import org.example.pipeline.spoon.method.MethodTransformResult;

import java.util.List;

public class LLMSummarizerStep extends AbstractNeo4jMetaStep {
    @Override
    public Neo4jService process(Neo4jService input) {
        ILLMProvider llmProvider = new KoboldCppProvider();

        Pipeline<Neo4jService, ?> oneSentenceSummaryPipeline = Pipeline
                .start(new MethodExtractorStep())
                .then(new AbstractLLMTransformStep<List<Neo4jMethodObject>, MethodTransformResult>(llmProvider) {

                    @Override
                    public MethodTransformResult process(List<Neo4jMethodObject> input) {
                        return null;
                    }

                    @Override
                    public String getLLMQuery(List<Neo4jMethodObject> input) {
                        return "";
                    }

                    @Override
                    public ILLMConfig getLLMConfig(List<Neo4jMethodObject> input) {
                        return null;
                    }

                    @Override
                    public List<Neo4jMethodObject> saveLLMResult(List<Neo4jMethodObject> input, String resp) {
                        return List.of();
                    }
                })
                .then(new MethodLoadStep())
                .build();

        Pipeline<Neo4jService, ?> explanationPipeline = Pipeline
                .start(new MethodExtractorStep())
                .then(new AbstractLLMTransformStep<List<Neo4jMethodObject>, MethodTransformResult>(llmProvider) {
                    @Override
                    public MethodTransformResult process(List<Neo4jMethodObject> input) {
                        return null;
                    }

                    @Override
                    public String getLLMQuery(List<Neo4jMethodObject> input) {
                        return "";
                    }

                    @Override
                    public ILLMConfig getLLMConfig(List<Neo4jMethodObject> input) {
                        return null;
                    }

                    @Override
                    public List<Neo4jMethodObject> saveLLMResult(List<Neo4jMethodObject> input, String resp) {
                        return List.of();
                    }
                })
                .then(new MethodLoadStep())
                .build();

        oneSentenceSummaryPipeline.run(input);
        explanationPipeline.run(input);
        return input;
    }
}
