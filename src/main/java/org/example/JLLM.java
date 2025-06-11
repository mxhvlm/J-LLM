package org.example;

import org.example.dbOutput.*;
import org.example.llm.APIConfig;
import org.example.llm.ILLMProvider;
import org.example.llm.LLMConfig;
import org.example.llm.providerImpl.OpenAIProvider;
import org.example.pipeline.Pipeline;
import org.example.pipeline.llm.ExplanationSummaryPreprocessedPipeline;
import org.example.pipeline.meta.ForumThreadsPipelinePreprocessed;
import org.example.pipeline.meta.CreateIndeciesAndConstraintsStep;
import org.example.pipeline.meta.PurgeDatabaseStep;
import org.example.pipeline.meta.SpoonExtractorStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JLLM implements Runnable {
    public static final Logger LOGGER = LoggerFactory.getLogger(JLLM.class);
    private final JLLMConfig _config;
    private final Pipeline<Neo4jService, Neo4jService> _metaPipeline;

    private APIConfig readOpenAIConfig() {
        String apiToken = System.getenv("OPENAI_API_KEY");
        String apiUrl = System.getenv("OPENAI_API_URL");

        if (apiToken == null || apiUrl == null) {
            LOGGER.error("OpenAI API key or URL not set in environment variables.");
            throw new IllegalStateException("OpenAI API key or URL not set in environment variables.");
        }
        return new APIConfig(apiUrl, apiToken);
    }

    public JLLM() {
        LOGGER.info("JLLM: Initializing...");
        _config = new JLLMConfig();

        _metaPipeline = Pipeline
                .start(new PurgeDatabaseStep())
                .then(new CreateIndeciesAndConstraintsStep())
                .then(new SpoonExtractorStep())
                .then(new ForumThreadsPipelinePreprocessed("1.19_candidate_posts_transformed.json"))
                .then(new ExplanationSummaryPreprocessedPipeline())
                .build();

//        _metaPipeline = Pipe[line
//            .start(new ExplanationSummaryPreprocessedPipeline())
//            .build();
//        _metaPipeline = Pipeline.start(new CreateEmbeddingStep("Explanation", "text", "explanationIndex"))
//            .then(new CreateEmbeddingStep("Summary", "text", "summaryIndex"))
//            .build();

        ILLMProvider llmProvider = new OpenAIProvider(readOpenAIConfig());
        LLMConfig explanationConfig = LLMConfig.Builder.defaultConfig()
            .withContextLength(16384)
            .withModel("gpt-3.5-turbo")
            .withTemperature(0.41)
            .withTopK(100)
            .withTopP(0.9)
            .build();

//        _metaPipeline = Pipeline
//                .start(new LLMExplanationPipelineStep(llmProvider, explanationConfig, 1))
//                .build();
//        _metaPipeline = Pipeline
//            .start(new ForumThreadsPipelinePreprocessed( "1.19_candidate_posts_transformed.json"))
//            .build();

        LOGGER.info("JLLM: Initialization complete.");
    }

    @Override
    public void run() {
        LOGGER.info("JLLM: Running import...");
        try (Neo4jService neo4jService = new Neo4jService(
                _config.getNeo4jUri(),
                _config.getNeo4jUser(),
                _config.getNeo4jPassword())) {

            _metaPipeline.run(neo4jService);

            System.out.println("Modules and packages imported successfully.");
        }
    }
}
