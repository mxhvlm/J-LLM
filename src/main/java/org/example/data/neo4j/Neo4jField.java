package org.example.data.neo4j;

public record Neo4jField(String declaringTypeName, String fieldName, String modifiers, String fieldSourceSnippet) {}
