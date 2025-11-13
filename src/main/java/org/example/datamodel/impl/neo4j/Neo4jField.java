package org.example.datamodel.impl.neo4j;

public record Neo4jField(String declaringTypeName, String fieldName, String modifiers, String fieldSourceSnippet) {}
