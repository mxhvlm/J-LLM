package org.example.data.neo4j;

import java.util.List;

public record Neo4jSummary(String summaryText, String id, List<Float> embedding) { }
