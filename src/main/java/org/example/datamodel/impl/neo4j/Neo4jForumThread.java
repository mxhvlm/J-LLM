package org.example.datamodel.impl.neo4j;

public record Neo4jForumThread(
    String title,
    String threadUrl,
    String version,
    int replies,
    String views,
    String time,
    boolean solved,
    String problemStatement,
    String context,
    String solution,
    String oneSentenceSummary
) {
}
