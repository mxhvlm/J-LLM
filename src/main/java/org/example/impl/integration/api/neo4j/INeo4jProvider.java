package org.example.impl.integration.api.neo4j;

import org.example.impl.datamodel.impl.neo4j.Neo4JLink;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;

public interface INeo4jProvider {
    void beginTransaction();

    Result runCypher(String cypher);

    Result runCypher(String cypher, Value parameters);

    void commitTransactionIfPresent();

    Driver getDriver();

    void runInThreadTransactionThreadSafe(String cypher, Value parameters);

    void creatLinkNode(Neo4JLink linkObject);
}
