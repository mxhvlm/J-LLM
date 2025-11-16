package com.mxhvlm.jllm.core.pipeline.step.llm.forumThreads;

import com.google.gson.Gson;
import com.mxhvlm.jllm.core.datamodel.api.knowledge.ProcessedForumThread;
import com.mxhvlm.jllm.core.datamodel.api.knowledge.SafeProcessedForumThread;
import com.mxhvlm.jllm.core.datamodel.impl.neo4j.Neo4JLink;
import com.mxhvlm.jllm.core.datamodel.impl.neo4j.Neo4jForumThread;
import com.mxhvlm.jllm.core.datamodel.impl.neo4j.Neo4jForumThreadTag;
import com.mxhvlm.jllm.core.datamodel.impl.neo4j.Neo4jType;
import com.mxhvlm.jllm.core.integration.api.llm.ILLMConfig;
import com.mxhvlm.jllm.core.integration.api.llm.ILLMProvider;
import com.mxhvlm.jllm.core.integration.api.neo4j.INeo4jProvider;
import com.mxhvlm.jllm.core.pipeline.step.llm.AbstractLLMTransformStep;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class TransformStep
        extends AbstractLLMTransformStep<
        ProcessedForumThread,
        TransformStep.IForumThreadOutput> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TransformStep.class);
    private final Gson _gson;
    private final INeo4jProvider _neo4JProvider;
    private final String _llmPrompt = """
            Extract the following details from the forum post in JSON format:
            - Problem statement: Clearly state the main problem or question asked.
            - Context: Provide the context or background information shared in the post.
            - Solved: Indicate if the issue has been resolved or not.
            - Solution: Detailed, long solution. Include all the solution steps or suggestions offered, if any.
            - One Sentence Summary: Provide a concise one-sentence summary of the post.
            - Mentioned minecraft or minecraft forge classes/types and methods: List any programming classes, types, or methods mentioned in the thread.
            - Version: If the Minecraft version is mentioned in one of the comments, add it here. If it concerns updating from another version, add it too.
            - Tags: Please come up with 5 tags that best describe the post.
            Forum Post:
            %s
            Output format:
            {{
                "problem_statement": "How to fix the lighting bug in Minecraft?",
                "context": "The user reported a lighting bug in their Minecraft world...",
                "solved": true,
                "solution": "One user suggested updating the graphics drivers...", # or null if not applicable
                "one_sentence_summary": "A user reported a lighting bug in Minecraft...",
                "mentioned_classes": ["net.minecraft.level.LevelRenderer", "net.minecraft.client.Minecraft"],
                "version": "1.19",
                "update_from": "1.18",
                "tags": ["lighting", "bug", "graphics", "update", "Minecraft"]
            }}
            Output JSON (nothing else):
            """;

    public TransformStep(ILLMProvider llmProvider, ILLMConfig config, INeo4jProvider neo4JProvider) {
        super(llmProvider, config);
        _gson = new Gson();
        _neo4JProvider = neo4JProvider;
    }

    private Collection<Neo4jType> lookupTypeByNameOrSimpleName(String typeName) {
        _neo4JProvider.beginTransaction();
        Collection<Neo4jType> types = _neo4JProvider.runCypher(
                        "MATCH (t:Type) WHERE t.name = $typeName OR t.simpleName = $typeName RETURN t",
                        Values.parameters("typeName", typeName))
                .stream()
                .map(record -> {
                    Node t = record.get("t").asNode();
                    return new Neo4jType(
                            t.get("name").asString(null),
                            t.get("simpleName").asString(null),
                            t.get("typeKind").asString(null),
                            t.get("modifiers").asString(null),
                            t.get("javadoc").asString(null)
                    );
                })
                .toList();
        _neo4JProvider.commitTransactionIfPresent();
        return types;
    }

    @Override
    public String getLLMQuery(ProcessedForumThread input) {
        return String.format(_llmPrompt, _gson.toJson(input));
    }

    @Override
    public ILLMConfig getLLMConfig(ProcessedForumThread input) {
        return _config;
    }

    @Override
    public Collection<IForumThreadOutput> processLLMResult(ProcessedForumThread input, String resp) {
        String stripped = resp.replaceAll("```json", "").replace("```", "");
        try {
            SafeProcessedForumThread processed = _gson.fromJson(resp, SafeProcessedForumThread.class);
            processed.fillDefaults();
            processed.tags = processed.tags.stream()
                    .map(String::toLowerCase)
                    .map(t -> t.replace(" ", "_"))
                    .toList();

            // Add forum tags
            List<ThreadTagNode> tags = processed.tags.stream()
                    .map(Neo4jForumThreadTag::new)
                    .map(ThreadTagNode::new)
                    .toList();

            // Add forum threads
            ThreadNode threadNode = new ThreadNode(
                    new Neo4jForumThread(input.title(),
                            input.thread_url(),
                            input.version(),
                            Integer.parseInt(input.replies()),
                            input.views(),
                            input.time(),
                            input.solved(),
                            processed.problem_statement,
                            processed.context,
                            processed.solution,
                            processed.one_sentence_summary));

            // Add tag links
            List<ThreadLink> links = new LinkedList<>(tags.stream()
                    .map(tag -> Neo4JLink.Builder.create()
                            .parentLabel("ForumThread")
                            .childLabel("ForumThreadTag")
                            .parentProp("threadUrl")
                            .childProp("name")
                            .parentValue(input.thread_url())
                            .childValue(tag.object().name())
                            .withLabel("HAS_TAG")
                            .build())
                    .map(ThreadLink::new)
                    .toList());

            // Add links to referenced classes
            LOGGER.info("Trying to link types {}", processed.mentioned_classes);
            for (String typeName : processed.mentioned_classes) {
                // Lookup the type by name or simple name
                Collection<Neo4jType> types = lookupTypeByNameOrSimpleName(typeName);
                if (types.isEmpty()) {
                    LOGGER.warn("Could not find type {}", typeName);
                    continue;
                }

                LOGGER.info("Found {} types for {}", types.size(), typeName);
                links.addAll(types.stream()
                        .map(type -> Neo4JLink.Builder.create()
                                .parentLabel("Type")
                                .childLabel("ForumThread")
                                .parentProp("name")
                                .childProp("threadUrl")
                                .parentValue(type.typeName())
                                .childValue(input.thread_url())
                                .withLabel("DISCUSSED_IN")
                                .build())
                        .map(ThreadLink::new)
                        .toList());
            }

            List<IForumThreadOutput> result = new LinkedList<>();
            result.add(threadNode);
            result.addAll(tags);
            result.addAll(links);

            return result;
        } catch (Exception e) {
            LOGGER.warn("Received invalid JSON from LLM: {}", resp);
            LOGGER.warn("Error: {}", e.getMessage());
            LOGGER.warn("Stack trace: ", e);
            return List.of();
        }
    }

    public sealed interface IForumThreadOutput
            permits ThreadLink, ThreadNode, ThreadTagNode, TransformStepPreprocessed.ThreadLink, TransformStepPreprocessed.ThreadNode, TransformStepPreprocessed.ThreadTagNode {
    }

    record ThreadNode(Neo4jForumThread object) implements IForumThreadOutput {
    }

    record ThreadTagNode(Neo4jForumThreadTag object) implements IForumThreadOutput {
    }

    record ThreadLink(Neo4JLink object) implements IForumThreadOutput {
    }
}