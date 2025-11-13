package org.example;

import org.example.integration.api.ITokenAuthConfig;
import org.example.integration.api.neo4j.INeo4jProvider;
import org.example.integration.impl.TokenAuthConfig;
import org.example.integration.api.llm.ILLMProvider;
import org.example.integration.api.llm.LLMConfig;
import org.example.integration.impl.neo4j.Neo4jProvider;
import org.example.integration.impl.openAI.OpenAIProvider;
import org.example.integration.api.wiki.IWikiProvider;
import org.example.integration.impl.redmine.wiki.RedmineProvider;
import org.example.pipeline.Pipeline;
import org.example.pipeline.meta.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JLLM implements Runnable {
    public static final Logger LOGGER = LoggerFactory.getLogger(JLLM.class);
    private final JLLMConfig _config;
    private final Pipeline<INeo4jProvider, INeo4jProvider> _metaPipeline;

    private ITokenAuthConfig readOpenAIConfig() {
        String apiToken = System.getenv("OPENAI_API_KEY");
        String apiUrl = System.getenv("OPENAI_API_URL");

        if (apiToken == null || apiUrl == null) {
            LOGGER.error("OpenAI API key or URL not set in environment variables.");
            throw new IllegalStateException("OpenAI API key or URL not set in environment variables.");
        }
        return new TokenAuthConfig(apiUrl, apiToken);
    }

    private ITokenAuthConfig readReadmineConfig() {
        String apiToken = System.getenv("REDMINE_API_KEY");
        String apiUrl = System.getenv("REDMINE_API_URL");

        if (apiToken == null || apiUrl == null) {
            LOGGER.error("Redmine API key or URL not set in environment variables.");
            throw new IllegalStateException("Redmine API key or URL not set in environment variables.");
        }
        return new TokenAuthConfig(apiUrl, apiToken);
    }

    public JLLM() {
        LOGGER.info("JLLM: Initializing...");
        _config = new JLLMConfig();

        ILLMProvider llmProvider = new OpenAIProvider(readOpenAIConfig());
        LLMConfig explanationConfig = LLMConfig.Builder.defaultConfig()
                .withContextLength(16384)
                .withModel("gpt-3.5-turbo")
                .withTemperature(0.41)
                .withTopK(100)
                .withTopP(0.9)
                .build();

        IWikiProvider wikiProvider = new RedmineProvider(readReadmineConfig());

        _metaPipeline = Pipeline
                .start(new PurgeDatabaseStep())
                .then(new CreateIndeciesAndConstraintsStep())
//                .then(new SpoonExtractorStep())
//                .then(new ForumThreadsPipelinePreprocessed("1.19_candidate_posts_transformed.json"))
//                .then(new ExplanationSummaryPreprocessedPipeline())
                .then(new WikiPipeline(1000, explanationConfig, llmProvider, "defaultProject", wikiProvider))
                .build();

//        _metaPipeline = Pipe[line
//            .start(new ExplanationSummaryPreprocessedPipeline())
//            .build();
//        _metaPipeline = Pipeline.start(new CreateEmbeddingStep("Explanation", "text", "explanationIndex"))
//            .then(new CreateEmbeddingStep("Summary", "text", "summaryIndex"))
//            .build();


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
        INeo4jProvider neo4JProvider = new Neo4jProvider(
                _config.getNeo4jUri(),
                _config.getNeo4jUser(),
                _config.getNeo4jPassword());

            _metaPipeline.run(neo4JProvider);

            System.out.println("Modules and packages imported successfully. Exiting...");
    }
}
