package com.mxhvlm.jllm.core.pipeline.definitions;

import com.mxhvlm.jllm.core.integration.api.neo4j.INeo4jProvider;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateIndeciesAndConstraintsStep extends AbstractNeo4jMetaStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateIndeciesAndConstraintsStep.class);

    /**
     * Create constraints and indexes to improve performance on MERGE.
     * Adjust these as needed for your schema.
     */
    @Override
    public INeo4jProvider process(INeo4jProvider input) {
        LOGGER.info("Creating constraints and indexes...");
        // Create a dedicated session and transaction for schema changes
        try (Session tempSession = input.getDriver().session()) {
            try (Transaction tempTx = tempSession.beginTransaction()) {
                // Create constraints for all major nodes
                tempTx.run("CREATE CONSTRAINT IF NOT EXISTS FOR (p:Package) REQUIRE p.name IS UNIQUE");
                tempTx.run("CREATE CONSTRAINT IF NOT EXISTS FOR (m:Module) REQUIRE m.name IS UNIQUE");
                tempTx.run("CREATE CONSTRAINT IF NOT EXISTS FOR (t:Type) REQUIRE t.name IS UNIQUE");
                tempTx.run("CREATE CONSTRAINT IF NOT EXISTS FOR (f:Field) REQUIRE f.id IS UNIQUE");
                tempTx.run("CREATE CONSTRAINT IF NOT EXISTS FOR (meth:Method) REQUIRE meth.id IS UNIQUE");
                tempTx.run("CREATE CONSTRAINT IF NOT EXISTS FOR (arg:TypeArgument) REQUIRE arg.id IS UNIQUE");
                tempTx.run("CREATE CONSTRAINT IF NOT EXISTS FOR (param:Parameter) REQUIRE param.id IS UNIQUE");
                tempTx.run("CREATE CONSTRAINT IF NOT EXISTS FOR (exp:Explanation) REQUIRE exp.id IS UNIQUE");
                tempTx.run("CREATE CONSTRAINT IF NOT EXISTS FOR (sum:Summary) REQUIRE sum.id IS UNIQUE");

                tempTx.commit();
                LOGGER.info("Constraints and indexes created.");
            }
        }
        return input;
    }
}
