package org.example.impl.datamodel.impl.neo4j;

import java.util.List;

public record Neo4jExplanation(String explanationText, String id, List<Float> embedding) {
}
