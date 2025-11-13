package org.example.integration.impl.neo4j;

import org.example.datamodel.impl.neo4j.Neo4JLink;
import org.example.integration.api.neo4j.INeo4jProvider;
import org.neo4j.driver.*;
import org.slf4j.Logger;

public class Neo4jProvider implements AutoCloseable, INeo4jProvider {
    private final Driver _driver;
    private final Session _session;
    private Transaction _transaction;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Neo4jProvider.class);

    public Neo4jProvider(String uri, String user, String password) {
        LOGGER.info("Connecting to Neo4j at " + uri + " with user " + user);
        this._driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        verifyConnectivity();
        _session = _driver.session();
        LOGGER.info("Connection successful.");
        verifyAPOCPluginAvailability();
    }

    private void verifyConnectivity() throws RuntimeException {
        LOGGER.info("Verifying connectivity...");
        try {
            this._driver.verifyConnectivity();
        } catch (Exception ex) {
            LOGGER.error("An error occurred while connecting to the Neo4j DB. Its probably not running or the connection IP and / or port are wrong: ", ex);
            throw new RuntimeException("Could not connect to Neo4j database", ex);
        }
    }

    private void verifyAPOCPluginAvailability() throws RuntimeException {
        LOGGER.info("Verifying availability of APOC plugin...");
        try {
            _session.run("CALL apoc.help('apoc')").consume();
            LOGGER.info("APOC plugin is available.");
        } catch (Exception e) {
            LOGGER.error("This tool requires the APOC plugin to be installed on the instance. Currently, the APOC plugin wasn't found. Please install / enable it before continuing.");
            throw new RuntimeException("APOC plugin is not available. Please ensure it is installed and configured correctly.", e);
        }
    }

    @Override
    public void beginTransaction() {
        if (_session == null || !_session.isOpen()) {
            throw new IllegalStateException("Session is not open. Did you call beginThreadTransaction()?");
        }
        if (_transaction == null || !_transaction.isOpen()) {
            _transaction = _session.beginTransaction();
        }
    }
    @Override
    public Result runCypher(String cypher) {
        return runCypher(cypher, Values.parameters());
    }

    @Override
    public Result runCypher(String cypher, Value parameters) {
        if (parameters.isEmpty()) {
            return _transaction.run(cypher);
        } else {
            return _transaction.run(cypher, parameters);
        }
    }

    @Override
    public void commitTransactionIfPresent() {
        try {
            if (_transaction != null && _transaction.isOpen()) {
                _transaction.commit();
            }
        } catch (Exception e) {
            LOGGER.error("Commit failed", e);
        }
    }


//    /**
//     * Start a session and transaction for the entire export run.
//     */
//    public void startSessionAndTransaction() {
//        this.session = driver.session();
//        this.transaction = session.beginTransaction();
//        LOGGER.info("Session and Transaction started.");
//    }
//
//    public void commitTransactionAndCloseSession() {
//        LOGGER.info("Committing Transaction and closing Session...");
//        if (this.transaction != null && this.transaction.isOpen()) {
//            this.transaction.commit();
//        }
//        if (this.session != null) {
//            this.session.close();
//        }
//        LOGGER.info("Transaction committed and Session closed.");
//    }

    @Override
    public Driver getDriver() {
        return _driver;
    }

    // Instead of opening and closing sessions/transactions in every method,
    // we now have a generic runInThreadTransaction method that uses the long-lived transaction.
//    public void runInThreadTransaction(String cypher, Value parameters) {
//        if (transaction == null || !transaction.isOpen()) {
//            throw new IllegalStateException("No open transaction. Did you call startSessionAndTransaction()?");
//        }
//        transaction.run(cypher, parameters);
//    }

    @Override
    public void runInThreadTransactionThreadSafe(String cypher, Value parameters) {
        try (Session session = _driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(cypher, parameters);
                return null;
            });
        }
    }

    @Override
    public void creatLinkNode(Neo4JLink linkObject) {
        runCypher(
                "MATCH (parent:" + linkObject.node1Label() + " {" + linkObject.node1Prop() + ": $leftName}), (child:" + linkObject.node2Label() + " {" + linkObject.node2Prop() + ": $rightName}) " +
                        "MERGE (parent)-[:" + linkObject.label() + "]->(child)",
                Values.parameters("leftName", linkObject.node1(), "rightName", linkObject.node2())
        );
    }

    @Override
    public void close() {
        _driver.close();
    }
}
