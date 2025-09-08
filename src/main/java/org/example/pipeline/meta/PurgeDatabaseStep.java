package org.example.pipeline.meta;

import org.example.interop.neo4j.Neo4jService;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurgeDatabaseStep extends AbstractNeo4jMetaStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeDatabaseStep.class);
    @Override
    public Neo4jService process(Neo4jService input) {
        LOGGER.info("Purging database...");
        try (Session tempSession = input.getDriver().session()) {
            try (Transaction tempTx = tempSession.beginTransaction()) {
                LOGGER.info("Purging contents...");
                tempTx.run("MATCH (n) DETACH DELETE n");
                tempTx.commit();
            }
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
