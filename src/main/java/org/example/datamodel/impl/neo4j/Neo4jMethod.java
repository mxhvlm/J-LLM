package org.example.datamodel.impl.neo4j;

public record Neo4jMethod(String methodName, String declaringTypeName, String methodSignature, String modifiers, String sourceCode, String javadoc) {}
