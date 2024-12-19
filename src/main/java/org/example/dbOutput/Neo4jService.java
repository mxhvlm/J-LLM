package org.example.dbOutput;

import org.neo4j.driver.*;
import org.slf4j.Logger;
import spoon.reflect.reference.CtTypeReference;

import java.util.UUID;


public class Neo4jService implements AutoCloseable {
    private final Driver driver;

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Neo4jService.class);

    public Neo4jService(String uri, String user, String password) {
        LOGGER.info("Connecting to Neo4j at " + uri + " with user " + user);
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));

        LOGGER.info("Verifying connectivity...");
        this.driver.verifyConnectivity();
        LOGGER.info("Connection successful.");
    }

    public void purgeDatabase() {
        LOGGER.info("Purging database...");
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (n) DETACH DELETE n");
                return null;
            });
        }
        LOGGER.info("Database purged.");
    }


    public void createPackageNode(String packageName) {
        LOGGER.info("Creating package node: " + packageName);
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MERGE (p:Package { name: $packageName })",
                        Values.parameters("packageName", packageName));
                return null;
            });
        }
    }

    public void createModuleNode(String moduleName) {
        LOGGER.info("Creating module node: " + moduleName);
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MERGE (m:Module { name: $moduleName })",
                        Values.parameters("moduleName", moduleName));
                return null;
            });
        }
    }

    public void linkPackageToParent(String parentPackageName, String childPackageName) {
        LOGGER.info("Linking package " + childPackageName + " to parent " + parentPackageName);
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MERGE (parent:Package {name: $parentPackageName}) " +
                                "MERGE (child:Package {name: $childPackageName}) " +
                                "MERGE (parent)-[:CONTAINS_PACKAGE]->(child)",
                        Values.parameters("parentPackageName", parentPackageName, "childPackageName", childPackageName)
                );
                return null;
            });
        }
    }

    public void linkModuleToPackage(String moduleName, String packageName) {
        LOGGER.info("Linking module " + moduleName + " to package " + packageName);
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (m:Module {name: $moduleName}), (p:Package {name: $packageName}) " +
                                "MERGE (m)-[:CONTAINS_PACKAGE]->(p)",
                        Values.parameters("moduleName", moduleName, "packageName", packageName));
                return null;
            });
        }
    }

    public void createTypeNode(String typeName, String simpleName, String typeKind, String modifiers, String javadoc) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MERGE (t:Type { name: $typeName }) " +
                                "SET t.simpleName = $simpleName, " +
                                "    t.typeKind = $typeKind, " +
                                "    t.modifiers = $modifiers, " +
                                "    t.javadoc = $javadoc",
                        Values.parameters("typeName", typeName, "simpleName", simpleName,
                                "typeKind", typeKind, "modifiers", modifiers, "javadoc", javadoc)
                );
                return null;
            });
        }
    }

    public void linkPackageToType(String packageName, String typeName) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (p:Package {name: $packageName}), (t:Type {name: $typeName}) " +
                                "MERGE (p)-[:CONTAINS_TYPE]->(t)",
                        Values.parameters("packageName", packageName, "typeName", typeName)
                );
                return null;
            });
        }
    }

    public void linkTypeToType(String parentTypeName, String childTypeName) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (parent:Type {name: $parentTypeName}), (child:Type {name: $childTypeName}) " +
                                "MERGE (parent)-[:CONTAINS_TYPE]->(child)",
                        Values.parameters("parentTypeName", parentTypeName, "childTypeName", childTypeName)
                );
                return null;
            });
        }
    }

    public void linkTypeExtends(String subTypeName, String superTypeName) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (sub:Type {name: $subTypeName}), (super:Type {name: $superTypeName}) " +
                                "MERGE (sub)-[:EXTENDS]->(super)",
                        Values.parameters("subTypeName", subTypeName, "superTypeName", superTypeName)
                );
                return null;
            });
        }
    }

    public void linkTypeImplements(String typeName, String interfaceName) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (t:Type {name: $typeName}), (i:Type {name: $interfaceName}) " +
                                "MERGE (t)-[:IMPLEMENTS]->(i)",
                        Values.parameters("typeName", typeName, "interfaceName", interfaceName)
                );
                return null;
            });
        }
    }

    // ------------------
    // Field Methods
    // ------------------
    public void createFieldNode(String declaringTypeName, String fieldName, String modifiers, String fieldSourceSnippet) {
        String fieldNodeId = declaringTypeName + "." + fieldName;
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MERGE (f:Field {id: $fieldId}) " +
                                "SET f.name = $fieldName, f.modifiers = $modifiers, f.sourceCodeSnippet = $sourceSnippet",
                        Values.parameters("fieldId", fieldNodeId, "fieldName", fieldName,
                                "modifiers", modifiers, "sourceSnippet", fieldSourceSnippet)
                );
                return null;
            });
        }
    }

    public void linkTypeHasField(String declaringTypeName, String fieldNodeId) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (t:Type {name: $typeName}), (f:Field {id: $fieldId}) " +
                                "MERGE (t)-[:HAS_FIELD]->(f)",
                        Values.parameters("typeName", declaringTypeName, "fieldId", fieldNodeId)
                );
                return null;
            });
        }
    }

    public void linkFieldOfType(String fieldNodeId, String fieldTypeName) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (f:Field {id: $fieldId}), (t:Type {name: $fieldTypeName}) " +
                                "MERGE (f)-[:OF_TYPE]->(t)",
                        Values.parameters("fieldId", fieldNodeId, "fieldTypeName", fieldTypeName)
                );
                return null;
            });
        }
    }

    public String createTypeArgumentNode(CtTypeReference<?> typeArgRef) {
        // Create a unique id for the type argument
        String argId = UUID.randomUUID().toString();
        String variance = ""; // If needed handle ? extends, ? super
        boolean isWildcard = typeArgRef != null && typeArgRef.toString().contains("?");

        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MERGE (arg:TypeArgument {id: $argId}) " +
                                "SET arg.isWildcard = $isWildcard, arg.variance = $variance",
                        Values.parameters("argId", argId, "isWildcard", isWildcard, "variance", variance)
                );
                return null;
            });
        }
        return argId;
    }

    public void linkFieldHasTypeArgument(String fieldNodeId, String argId) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (f:Field {id: $fieldId}), (arg:TypeArgument {id: $argId}) " +
                                "MERGE (f)-[:HAS_TYPE_ARGUMENT]->(arg)",
                        Values.parameters("fieldId", fieldNodeId, "argId", argId)
                );
                return null;
            });
        }
    }

    public void linkTypeArgumentOfType(String argId, String typeName) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (arg:TypeArgument {id: $argId}), (t:Type {name: $typeName}) " +
                                "MERGE (arg)-[:OF_TYPE]->(t)",
                        Values.parameters("argId", argId, "typeName", typeName)
                );
                return null;
            });
        }
    }

    // ------------------
    // Method Methods
    // ------------------
    public void createMethodNode(String declaringTypeName, String methodSignature, String modifiers, String sourceCode, String javadoc) {
        String methodNodeId = declaringTypeName + "#" + methodSignature;
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MERGE (m:Method {id: $methodId}) " +
                                "SET m.declaringType = $declaringType, m.signature = $signature, " +
                                "    m.modifiers = $modifiers, m.sourceCode = $sourceCode, m.javadoc = $javadoc",
                        Values.parameters(
                                "methodId", methodNodeId,
                                "declaringType", declaringTypeName,
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
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (t:Type {name: $typeName}), (m:Method {id: $methodId}) " +
                                "MERGE (t)-[:HAS_METHOD]->(m)",
                        Values.parameters("typeName", declaringTypeName, "methodId", methodNodeId)
                );
                return null;
            });
        }
    }

    public void linkMethodReturnsType(String methodNodeId, String returnTypeName) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (m:Method {id: $methodId}), (t:Type {name: $returnTypeName}) " +
                                "MERGE (m)-[:RETURNS]->(t)",
                        Values.parameters("methodId", methodNodeId, "returnTypeName", returnTypeName)
                );
                return null;
            });
        }
    }

    public void linkMethodHasReturnTypeArgument(String methodNodeId, String argId) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (m:Method {id: $methodId}), (arg:TypeArgument {id: $argId}) " +
                                "MERGE (m)-[:HAS_RETURN_TYPE_ARGUMENT]->(arg)",
                        Values.parameters("methodId", methodNodeId, "argId", argId)
                );
                return null;
            });
        }
    }

    public String createParameterNode(String declaringTypeName, String methodSignature, String paramName) {
        String paramId = declaringTypeName + "#" + methodSignature + ":" + paramName;
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MERGE (p:Parameter {id: $paramId}) " +
                                "SET p.name = $paramName",
                        Values.parameters("paramId", paramId, "paramName", paramName)
                );
                return null;
            });
        }
        return paramId;
    }

    public void linkMethodHasParameter(String methodNodeId, String paramNodeId) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (m:Method {id: $methodId}), (p:Parameter {id: $paramId}) " +
                                "MERGE (m)-[:HAS_PARAMETER]->(p)",
                        Values.parameters("methodId", methodNodeId, "paramId", paramNodeId)
                );
                return null;
            });
        }
    }

    public void linkParameterOfType(String paramNodeId, String paramTypeName) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (p:Parameter {id: $paramId}), (t:Type {name: $paramTypeName}) " +
                                "MERGE (p)-[:OF_TYPE]->(t)",
                        Values.parameters("paramId", paramNodeId, "paramTypeName", paramTypeName)
                );
                return null;
            });
        }
    }

    public void linkParameterHasTypeArgument(String paramNodeId, String argId) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (p:Parameter {id: $paramId}), (arg:TypeArgument {id: $argId}) " +
                                "MERGE (p)-[:HAS_TYPE_ARGUMENT]->(arg)",
                        Values.parameters("paramId", paramNodeId, "argId", argId)
                );
                return null;
            });
        }
    }

    // ------------------
    // Call Graph Methods
    // ------------------
    public void linkMethodCallsMethod(String callerMethodId, String calleeMethodId) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run(
                        "MATCH (caller:Method {id: $callerId}), (callee:Method {id: $calleeId}) " +
                                "MERGE (caller)-[:CALLS]->(callee)",
                        Values.parameters("callerId", callerMethodId, "calleeId", calleeMethodId)
                );
                return null;
            });
        }
    }

    @Override
    public void close() {
        driver.close();
    }
}
