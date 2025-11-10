package org.example.integration.neo4j;

import org.example.datamodel.neo4j.Neo4JLink;
import org.neo4j.driver.*;
import org.slf4j.Logger;
import spoon.reflect.reference.CtTypeReference;

import java.util.UUID;

public class Neo4jService implements AutoCloseable {
    private final Driver _driver;
    private final Session _session;
    private Transaction _transaction;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Neo4jService.class);

    public Neo4jService(String uri, String user, String password) {
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

    public void beginTransaction() {
        if (_session == null || !_session.isOpen()) {
            throw new IllegalStateException("Session is not open. Did you call beginThreadTransaction()?");
        }
        if (_transaction == null || !_transaction.isOpen()) {
            _transaction = _session.beginTransaction();
        }
    }
    public Result runCypher(String cypher) {
        return runCypher(cypher, Values.parameters());
    }

    public Result runCypher(String cypher, Value parameters) {
        if (parameters.isEmpty()) {
            return _transaction.run(cypher);
        } else {
            return _transaction.run(cypher, parameters);
        }
    }

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

    public void runInThreadTransactionThreadSafe(String cypher, Value parameters) {
        try (Session session = _driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(cypher, parameters);
                return null;
            });
        }
    }

//    private void runInThreadTransaction(String cypher) {
//        runInThreadTransaction(cypher, Values.parameters());
//    }

    public void createPackageNode(String packageName, String simpleName) {
        // If we know packages are always new, we can use CREATE to avoid MERGE overhead:
        runInThreadTransactionThreadSafe(
                "CREATE (p:Package { name: $packageName, simpleName: $simpleName })",
                Values.parameters("packageName", packageName, "simpleName", simpleName)
        );
    }

    public void createModuleNode(String moduleName) {
        // If we assume each moduleName is unique and new:
        runCypher("CREATE (m:Module { name: $moduleName })",
                Values.parameters("moduleName", moduleName));
    }

    public void linkPackageToParent(String parentPackageName, String childPackageName) {
        runCypher(
                "MATCH (parent:Package {name: $parentPackageName}), (child:Package {name: $childPackageName}) " +
                        "MERGE (parent)-[:CONTAINS_PACKAGE]->(child)",
                Values.parameters("parentPackageName", parentPackageName, "childPackageName", childPackageName)
        );
    }

    public void linkModuleToPackage(String moduleName, String packageName) {
        runCypher(
                "MATCH (m:Module {name: $moduleName}), (p:Package {name: $packageName}) " +
                        "MERGE (m)-[:CONTAINS_PACKAGE]->(p)",
                Values.parameters("moduleName", moduleName, "packageName", packageName));
    }

    public void creatLinkNode(Neo4JLink linkObject) {
        runCypher(
                "MATCH (parent:" + linkObject.node1Label() + " {" + linkObject.node1Prop() + ": $leftName}), (child:" + linkObject.node2Label() + " {" + linkObject.node2Prop() + ": $rightName}) " +
                        "MERGE (parent)-[:" + linkObject.label() + "]->(child)",
                Values.parameters("leftName", linkObject.node1(), "rightName", linkObject.node2())
        );
    }

    public void linkPackageToType(String packageName, String typeName) {
        runCypher(
                "MATCH (p:Package {name: $packageName}), (t:Type {name: $typeName}) " +
                        "MERGE (p)-[:CONTAINS_TYPE]->(t)",
                Values.parameters("packageName", packageName, "typeName", typeName)
        );
    }

    public void linkTypeToType(String parentTypeName, String childTypeName) {
        runCypher(
                "MATCH (parent:Type {name: $parentTypeName}), (child:Type {name: $childTypeName}) " +
                        "MERGE (parent)-[:CONTAINS_TYPE]->(child)",
                Values.parameters("parentTypeName", parentTypeName, "childTypeName", childTypeName)
        );
    }

    public void linkTypeExtends(String subTypeName, String superTypeName) {
        runCypher(
                "MATCH (sub:Type {name: $subTypeName}), (super:Type {name: $superTypeName}) " +
                        "MERGE (sub)-[:EXTENDS]->(super)",
                Values.parameters("subTypeName", subTypeName, "superTypeName", superTypeName)
        );
    }

    public void linkTypeImplements(String typeName, String interfaceName) {
        runCypher(
                "MATCH (t:Type {name: $typeName}), (i:Type {name: $interfaceName}) " +
                        "MERGE (t)-[:IMPLEMENTS]->(i)",
                Values.parameters("typeName", typeName, "interfaceName", interfaceName)
        );
    }

    public void createFieldNode(String declaringTypeName, String fieldName, String modifiers, String fieldSourceSnippet) {
        String fieldNodeId = declaringTypeName + "." + fieldName;
        runCypher(
                "MERGE (f:Field {id: $fieldId}) " +
                        "SET f.name = $fieldName, f.modifiers = $modifiers, f.sourceCodeSnippet = $sourceSnippet",
                Values.parameters("fieldId", fieldNodeId, "fieldName", fieldName,
                        "modifiers", modifiers, "sourceSnippet", fieldSourceSnippet)
        );
    }

    public void linkTypeHasField(String declaringTypeName, String fieldNodeId) {
        runCypher(
                "MATCH (t:Type {name: $typeName}), (f:Field {id: $fieldId}) " +
                        "MERGE (t)-[:HAS_FIELD]->(f)",
                Values.parameters("typeName", declaringTypeName, "fieldId", fieldNodeId)
        );
    }

    public void linkFieldOfType(String fieldNodeId, String fieldTypeName) {
        runCypher(
                "MATCH (f:Field {id: $fieldId}), (t:Type {name: $fieldTypeName}) " +
                        "MERGE (f)-[:OF_TYPE]->(t)",
                Values.parameters("fieldId", fieldNodeId, "fieldTypeName", fieldTypeName)
        );
    }

    public String createTypeArgumentNode(CtTypeReference<?> typeArgRef) {
        String argId = UUID.randomUUID().toString();
        boolean isWildcard = typeArgRef != null && typeArgRef.toString().contains("?");
        String variance = ""; // currently unused
        runCypher(
                "MERGE (arg:TypeArgument {id: $argId}) " +
                        "SET arg.isWildcard = $isWildcard, arg.variance = $variance",
                Values.parameters("argId", argId, "isWildcard", isWildcard, "variance", variance)
        );
        return argId;
    }

    public void linkFieldHasTypeArgument(String fieldNodeId, String argId) {
        runCypher(
                "MATCH (f:Field {id: $fieldId}), (arg:TypeArgument {id: $argId}) " +
                        "MERGE (f)-[:HAS_TYPE_ARGUMENT]->(arg)",
                Values.parameters("fieldId", fieldNodeId, "argId", argId)
        );
    }

    public void linkTypeArgumentOfType(String argId, String typeName) {
        runCypher(
                "MATCH (arg:TypeArgument {id: $argId}), (t:Type {name: $typeName}) " +
                        "MERGE (arg)-[:OF_TYPE]->(t)",
                Values.parameters("argId", argId, "typeName", typeName)
        );
    }

    public void createMethodNode(String methodName, String declaringTypeName, String methodSignature, String modifiers, String sourceCode, String javadoc) {
        String methodNodeId = declaringTypeName + "#" + methodSignature;
        try (Session session = _driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MERGE (m:Method {id: $methodId}) " +
                                "SET m.declaringType = $declaringType, m.name = $name, m.signature = $signature, " +
                                "    m.modifiers = $modifiers, m.sourceCode = $sourceCode, m.javadoc = $javadoc",
                        Values.parameters(
                                "methodId", methodNodeId,
                                "declaringType", declaringTypeName,
                                "name", methodName,
                                "signature", methodSignature,
                                "modifiers", modifiers,
                                "sourceCode", sourceCode,
                                "javadoc", javadoc
                        )
                );
                return null;
            });
        }
    }

    public void linkTypeHasMethod(String declaringTypeName, String methodNodeId) {
        runCypher(
                "MATCH (t:Type {name: $typeName}), (m:Method {id: $methodId}) " +
                        "MERGE (t)-[:HAS_METHOD]->(m)",
                Values.parameters("typeName", declaringTypeName, "methodId", methodNodeId)
        );
    }

    public void linkMethodReturnsType(String methodNodeId, String returnTypeName) {
        runCypher(
                "MATCH (m:Method {id: $methodId}), (t:Type {name: $returnTypeName}) " +
                        "MERGE (m)-[:RETURNS]->(t)",
                Values.parameters("methodId", methodNodeId, "returnTypeName", returnTypeName)
        );
    }

    public void linkMethodHasReturnTypeArgument(String methodNodeId, String argId) {
        runCypher(
                "MATCH (m:Method {id: $methodId}), (arg:TypeArgument {id: $argId}) " +
                        "MERGE (m)-[:HAS_RETURN_TYPE_ARGUMENT]->(arg)",
                Values.parameters("methodId", methodNodeId, "argId", argId)
        );
    }

    public void linkMethodDependsOnField(String methodNodeId, String fieldNodeId) {
        runCypher(
                "MATCH (m:Method {id: $methodId}), (f:Field {id: $fieldId}) " +
                        "MERGE (m)-[:DEPENDS_ON]->(f)",
                Values.parameters("methodId", methodNodeId, "fieldId", fieldNodeId)
        );
    }

    public String createParameterNode(String declaringTypeName, String methodSignature, String paramName) {
        String paramId = declaringTypeName + "#" + methodSignature + ":" + paramName;
        runCypher(
                "MERGE (p:Parameter {id: $paramId}) " +
                        "SET p.name = $paramName, " + // Ensure "name" is set
                        "    p.displayName = $paramName",  // Optionally add displayName for clarity
                Values.parameters("paramId", paramId, "paramName", paramName)
        );
        return paramId;
    }

    public void linkMethodHasParameter(String methodNodeId, String paramNodeId) {
        runCypher(
                "MATCH (m:Method {id: $methodId}), (p:Parameter {id: $paramId}) " +
                        "MERGE (m)-[:HAS_PARAMETER]->(p)",
                Values.parameters("methodId", methodNodeId, "paramId", paramNodeId)
        );
    }

    public void linkParameterOfType(String paramNodeId, String paramTypeName) {
        runCypher(
                "MATCH (p:Parameter {id: $paramId}), (t:Type {name: $paramTypeName}) " +
                        "MERGE (p)-[:OF_TYPE]->(t)",
                Values.parameters("paramId", paramNodeId, "paramTypeName", paramTypeName)
        );
    }

    public void linkParameterHasTypeArgument(String paramNodeId, String argId) {
        runCypher(
                "MATCH (p:Parameter {id: $paramId}), (arg:TypeArgument {id: $argId}) " +
                        "MERGE (p)-[:HAS_TYPE_ARGUMENT]->(arg)",
                Values.parameters("paramId", paramNodeId, "argId", argId)
        );
    }

    public void linkMethodCallsMethod(String callerMethodId, String calleeMethodId) {
        runCypher(
                "MATCH (caller:Method {id: $callerId}), (callee:Method {id: $calleeId}) " +
                        "MERGE (caller)-[:CALLS]->(callee)",
                Values.parameters("callerId", callerMethodId, "calleeId", calleeMethodId)
        );
    }

    @Override
    public void close() {
        _driver.close();
    }
}
