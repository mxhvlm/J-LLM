package org.example.dbOutput;

import org.example.data.Neo4JLinkObject;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.TransientException;
import org.slf4j.Logger;
import spoon.reflect.reference.CtTypeReference;

import java.util.UUID;

public class Neo4jService implements AutoCloseable {
    private final Driver driver;
    private final ThreadLocal<Session> threadSession;
    private final ThreadLocal<Transaction> threadTransaction;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Neo4jService.class);

    public Neo4jService(String uri, String user, String password) {
        LOGGER.info("Connecting to Neo4j at " + uri + " with user " + user);
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        LOGGER.info("Verifying connectivity...");
        this.driver.verifyConnectivity();

        threadSession = ThreadLocal.withInitial(() -> driver.session());
        threadTransaction = ThreadLocal.withInitial(() -> threadSession.get().beginTransaction());

        LOGGER.info("Connection successful.");
    }

    public void runCypherThreadsafe(String cypher, Value parameters) {
        int attempts = 0;
        while (true) {
            try {
                Transaction tx = threadTransaction.get();
                tx.run(cypher, parameters);
                return;
            } catch (TransientException ex) {
                attempts++;
                if (attempts >= 5) {
                    LOGGER.error("Too many retries for cypher: {}", cypher);
                    throw ex;
                }
                LOGGER.warn("Deadlock detected, retrying transaction... (attempt {})", attempts);
                resetThreadTransaction(); // rollback and get a new one
            }
        }
    }

    private void resetThreadTransaction() {
        try {
            Transaction tx = threadTransaction.get();
            if (tx != null && tx.isOpen()) {
                tx.rollback();
            }
        } catch (Exception ignored) {}
        threadTransaction.remove();
        threadTransaction.set(driver.session().beginTransaction());
    }


    public void commitThreadTransactionIfPresent() {
        try {
            Transaction tx = threadTransaction.get();
            if (tx != null && tx.isOpen()) {
                tx.commit();
            }
            Session session = threadSession.get();
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            LOGGER.error("Commit failed", e);
        } finally {
            threadTransaction.remove();
            threadSession.remove();
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
        return driver;
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
        try (Session session = driver.session()) {
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
        runCypherThreadsafe("CREATE (m:Module { name: $moduleName })",
                Values.parameters("moduleName", moduleName));
    }

    public void linkPackageToParent(String parentPackageName, String childPackageName) {
        runCypherThreadsafe(
                "MATCH (parent:Package {name: $parentPackageName}), (child:Package {name: $childPackageName}) " +
                        "MERGE (parent)-[:CONTAINS_PACKAGE]->(child)",
                Values.parameters("parentPackageName", parentPackageName, "childPackageName", childPackageName)
        );
    }

    public void linkModuleToPackage(String moduleName, String packageName) {
        runCypherThreadsafe(
                "MATCH (m:Module {name: $moduleName}), (p:Package {name: $packageName}) " +
                        "MERGE (m)-[:CONTAINS_PACKAGE]->(p)",
                Values.parameters("moduleName", moduleName, "packageName", packageName));
    }

    // TODO: Add params to set the names of the props we want to link by
    // Parent and child labels
    // Parent and child props
    public void creatLinkNode(Neo4JLinkObject linkObject) {
        runCypherThreadsafe(
                "MATCH (parent:" + linkObject.node1Label() + " {" + linkObject.node1Prop() + ": $leftName}), (child:" + linkObject.node2Label() + " {" + linkObject.node2Prop() + ": $rightName}) " +
                        "MERGE (parent)-[:" + linkObject.label() + "]->(child)",
                Values.parameters("leftName", linkObject.node1(), "rightName", linkObject.node2())
        );
    }

    public void linkPackageToType(String packageName, String typeName) {
        runCypherThreadsafe(
                "MATCH (p:Package {name: $packageName}), (t:Type {name: $typeName}) " +
                        "MERGE (p)-[:CONTAINS_TYPE]->(t)",
                Values.parameters("packageName", packageName, "typeName", typeName)
        );
    }

    public void linkTypeToType(String parentTypeName, String childTypeName) {
        runCypherThreadsafe(
                "MATCH (parent:Type {name: $parentTypeName}), (child:Type {name: $childTypeName}) " +
                        "MERGE (parent)-[:CONTAINS_TYPE]->(child)",
                Values.parameters("parentTypeName", parentTypeName, "childTypeName", childTypeName)
        );
    }

    public void linkTypeExtends(String subTypeName, String superTypeName) {
        runCypherThreadsafe(
                "MATCH (sub:Type {name: $subTypeName}), (super:Type {name: $superTypeName}) " +
                        "MERGE (sub)-[:EXTENDS]->(super)",
                Values.parameters("subTypeName", subTypeName, "superTypeName", superTypeName)
        );
    }

    public void linkTypeImplements(String typeName, String interfaceName) {
        runCypherThreadsafe(
                "MATCH (t:Type {name: $typeName}), (i:Type {name: $interfaceName}) " +
                        "MERGE (t)-[:IMPLEMENTS]->(i)",
                Values.parameters("typeName", typeName, "interfaceName", interfaceName)
        );
    }

    public void createFieldNode(String declaringTypeName, String fieldName, String modifiers, String fieldSourceSnippet) {
        String fieldNodeId = declaringTypeName + "." + fieldName;
        runCypherThreadsafe(
                "MERGE (f:Field {id: $fieldId}) " +
                        "SET f.name = $fieldName, f.modifiers = $modifiers, f.sourceCodeSnippet = $sourceSnippet",
                Values.parameters("fieldId", fieldNodeId, "fieldName", fieldName,
                        "modifiers", modifiers, "sourceSnippet", fieldSourceSnippet)
        );
    }

    public void linkTypeHasField(String declaringTypeName, String fieldNodeId) {
        runCypherThreadsafe(
                "MATCH (t:Type {name: $typeName}), (f:Field {id: $fieldId}) " +
                        "MERGE (t)-[:HAS_FIELD]->(f)",
                Values.parameters("typeName", declaringTypeName, "fieldId", fieldNodeId)
        );
    }

    public void linkFieldOfType(String fieldNodeId, String fieldTypeName) {
        runCypherThreadsafe(
                "MATCH (f:Field {id: $fieldId}), (t:Type {name: $fieldTypeName}) " +
                        "MERGE (f)-[:OF_TYPE]->(t)",
                Values.parameters("fieldId", fieldNodeId, "fieldTypeName", fieldTypeName)
        );
    }

    public String createTypeArgumentNode(CtTypeReference<?> typeArgRef) {
        String argId = UUID.randomUUID().toString();
        boolean isWildcard = typeArgRef != null && typeArgRef.toString().contains("?");
        String variance = ""; // currently unused
        runCypherThreadsafe(
                "MERGE (arg:TypeArgument {id: $argId}) " +
                        "SET arg.isWildcard = $isWildcard, arg.variance = $variance",
                Values.parameters("argId", argId, "isWildcard", isWildcard, "variance", variance)
        );
        return argId;
    }

    public void linkFieldHasTypeArgument(String fieldNodeId, String argId) {
        runCypherThreadsafe(
                "MATCH (f:Field {id: $fieldId}), (arg:TypeArgument {id: $argId}) " +
                        "MERGE (f)-[:HAS_TYPE_ARGUMENT]->(arg)",
                Values.parameters("fieldId", fieldNodeId, "argId", argId)
        );
    }

    public void linkTypeArgumentOfType(String argId, String typeName) {
        runCypherThreadsafe(
                "MATCH (arg:TypeArgument {id: $argId}), (t:Type {name: $typeName}) " +
                        "MERGE (arg)-[:OF_TYPE]->(t)",
                Values.parameters("argId", argId, "typeName", typeName)
        );
    }

    public void createMethodNode(String methodName, String declaringTypeName, String methodSignature, String modifiers, String sourceCode, String javadoc) {
        String methodNodeId = declaringTypeName + "#" + methodSignature;
        try (Session session = driver.session()) {
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
        runCypherThreadsafe(
                "MATCH (t:Type {name: $typeName}), (m:Method {id: $methodId}) " +
                        "MERGE (t)-[:HAS_METHOD]->(m)",
                Values.parameters("typeName", declaringTypeName, "methodId", methodNodeId)
        );
    }

    public void linkMethodReturnsType(String methodNodeId, String returnTypeName) {
        runCypherThreadsafe(
                "MATCH (m:Method {id: $methodId}), (t:Type {name: $returnTypeName}) " +
                        "MERGE (m)-[:RETURNS]->(t)",
                Values.parameters("methodId", methodNodeId, "returnTypeName", returnTypeName)
        );
    }

    public void linkMethodHasReturnTypeArgument(String methodNodeId, String argId) {
        runCypherThreadsafe(
                "MATCH (m:Method {id: $methodId}), (arg:TypeArgument {id: $argId}) " +
                        "MERGE (m)-[:HAS_RETURN_TYPE_ARGUMENT]->(arg)",
                Values.parameters("methodId", methodNodeId, "argId", argId)
        );
    }

    public void linkMethodDependsOnField(String methodNodeId, String fieldNodeId) {
        runCypherThreadsafe(
                "MATCH (m:Method {id: $methodId}), (f:Field {id: $fieldId}) " +
                        "MERGE (m)-[:DEPENDS_ON]->(f)",
                Values.parameters("methodId", methodNodeId, "fieldId", fieldNodeId)
        );
    }

    public String createParameterNode(String declaringTypeName, String methodSignature, String paramName) {
        String paramId = declaringTypeName + "#" + methodSignature + ":" + paramName;
        runCypherThreadsafe(
                "MERGE (p:Parameter {id: $paramId}) " +
                        "SET p.name = $paramName, " + // Ensure "name" is set
                        "    p.displayName = $paramName",  // Optionally add displayName for clarity
                Values.parameters("paramId", paramId, "paramName", paramName)
        );
        return paramId;
    }

    public void linkMethodHasParameter(String methodNodeId, String paramNodeId) {
        runCypherThreadsafe(
                "MATCH (m:Method {id: $methodId}), (p:Parameter {id: $paramId}) " +
                        "MERGE (m)-[:HAS_PARAMETER]->(p)",
                Values.parameters("methodId", methodNodeId, "paramId", paramNodeId)
        );
    }

    public void linkParameterOfType(String paramNodeId, String paramTypeName) {
        runCypherThreadsafe(
                "MATCH (p:Parameter {id: $paramId}), (t:Type {name: $paramTypeName}) " +
                        "MERGE (p)-[:OF_TYPE]->(t)",
                Values.parameters("paramId", paramNodeId, "paramTypeName", paramTypeName)
        );
    }

    public void linkParameterHasTypeArgument(String paramNodeId, String argId) {
        runCypherThreadsafe(
                "MATCH (p:Parameter {id: $paramId}), (arg:TypeArgument {id: $argId}) " +
                        "MERGE (p)-[:HAS_TYPE_ARGUMENT]->(arg)",
                Values.parameters("paramId", paramNodeId, "argId", argId)
        );
    }

    public void linkMethodCallsMethod(String callerMethodId, String calleeMethodId) {
        runCypherThreadsafe(
                "MATCH (caller:Method {id: $callerId}), (callee:Method {id: $calleeId}) " +
                        "MERGE (caller)-[:CALLS]->(callee)",
                Values.parameters("callerId", callerMethodId, "calleeId", calleeMethodId)
        );
    }

    @Override
    public void close() {
        driver.close();
    }
}
