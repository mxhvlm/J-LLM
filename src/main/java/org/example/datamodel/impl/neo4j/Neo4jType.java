package org.example.datamodel.impl.neo4j;

public record Neo4jType(String typeName, String simpleName, String typeKind, String modifiers, String javadoc) {
}