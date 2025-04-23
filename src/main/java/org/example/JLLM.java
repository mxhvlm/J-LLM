package org.example;

import org.example.dbOutput.*;
import org.example.pipeline.Pipeline;
import org.example.pipeline.meta.CreateIndeciesAndConstraintsStep;
import org.example.pipeline.meta.PurgeDatabaseStep;
import org.example.pipeline.meta.SpoonExtractorStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JLLM implements Runnable {
    public static final Logger LOGGER = LoggerFactory.getLogger(JLLM.class);
    private final JLLMConfig _config;
    private final Pipeline<Neo4jService, Neo4jService> _metaPipeline;

    public JLLM() {
        LOGGER.info("JLLM: Initializing...");
        _config = new JLLMConfig();

        _metaPipeline = Pipeline
                .start(new PurgeDatabaseStep())
                .then(new CreateIndeciesAndConstraintsStep())
                .then(new SpoonExtractorStep())
                .build();
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
