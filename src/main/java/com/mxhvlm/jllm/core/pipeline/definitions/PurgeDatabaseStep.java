package com.mxhvlm.jllm.core.pipeline.definitions;

import com.mxhvlm.jllm.core.integration.api.neo4j.INeo4jProvider;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurgeDatabaseStep extends AbstractNeo4jMetaStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeDatabaseStep.class);

    @Override
    public INeo4jProvider process(INeo4jProvider input) {
        LOGGER.info("Purging database...");
        try (Session tempSession = input.getDriver().session()) {
            LOGGER.info("Purging contents...");

            tempSession.run("MATCH (n)\n" +
                    "CALL { WITH n\n" +
                    "DETACH DELETE n\n" +
                    "} IN TRANSACTIONS OF 10000 ROWS;");
        }
        try (Session tempSession = input.getDriver().session()) {
            try (Transaction tempTx = tempSession.beginTransaction()) {
                LOGGER.info("Purging indices...");
                tempTx.run("CALL apoc.schema.assert({},{})");
                tempTx.commit();
            }
        }
        LOGGER.info("Database purged.");

        return input;
    }
}
