package org.example.impl.pipeline.llm.forumThreads;

import org.example.impl.datamodel.impl.neo4j.Neo4jForumThread;
import org.example.impl.datamodel.impl.neo4j.Neo4jForumThreadTag;
import org.example.impl.integration.api.neo4j.INeo4jProvider;
import org.example.impl.pipeline.AbstractNeo4jLoaderStep;
import org.example.impl.pipeline.IPipelineStep;
import org.example.impl.pipeline.TransformResult;
import org.neo4j.driver.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoadStepPreprocessed extends AbstractNeo4jLoaderStep
        implements IPipelineStep<Stream<TransformStep.IForumThreadOutput>, TransformResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadStepPreprocessed.class);

    public LoadStepPreprocessed(INeo4jProvider neo4JProvider) {
        super(neo4JProvider);
    }

    private void createForumThreadNode(Neo4jForumThread thread) {
        _neo4JProvider.runCypher("""
                        CREATE (t:ForumThread {
                            title: $title,
                            threadUrl: $threadUrl,
                            version: $version,
                            replies: $replies,
                            views: $views,
                            time: $time,
                            solved: $solved,
                            problemStatement: $problemStatement,
                            context: $context,
                            solution: $solution,
                            oneSentenceSummary: $oneSentenceSummary
                        })
                        """,
                Values.parameters(
                        "title", thread.title(),
                        "threadUrl", thread.threadUrl(),
                        "version", thread.version(),
                        "replies", thread.replies(),
                        "views", thread.views(),
                        "time", thread.time(),
                        "solved", thread.solved(),
                        "problemStatement", thread.problemStatement(),
                        "context", thread.context(),
                        "solution", thread.solution(),
                        "oneSentenceSummary", thread.oneSentenceSummary()
                )
        );
    }

    private void createForumThreadTagNode(Neo4jForumThreadTag tag) {
        _neo4JProvider.runCypher("""
                        MERGE (tag:ForumThreadTag {name: $name})
                        """,
                Values.parameters("name", tag.name())
        );
    }

    @Override
    public TransformResult process(Stream<TransformStep.IForumThreadOutput> input) {
        Map<Boolean, List<TransformStep.IForumThreadOutput>> partitions = input.collect(
                Collectors.partitioningBy(i -> i instanceof TransformStepPreprocessed.ThreadLink));

        LOGGER.info("ForumThreadExporter: Loading forum threads...");
        _neo4JProvider.beginTransaction();
        LOGGER.info("ForumThreadExporter: Exporting {} nodes", partitions.get(false).size());
        partitions.get(false).forEach(node -> {
            if (node instanceof TransformStepPreprocessed.ThreadNode threadNode) {
                createForumThreadNode(threadNode.object());
            } else if (node instanceof TransformStepPreprocessed.ThreadTagNode tagNode) {
                createForumThreadTagNode(tagNode.object());
            }
        });
        _neo4JProvider.commitTransactionIfPresent();

        LOGGER.info("ForumThreadExporter: Exporting {} links", partitions.get(true).size());
        _neo4JProvider.beginTransaction();
        partitions.get(true).forEach(link -> _neo4JProvider.creatLinkNode(((TransformStepPreprocessed.ThreadLink) link).object()));
        _neo4JProvider.commitTransactionIfPresent();

        return new TransformResult();
    }
}
