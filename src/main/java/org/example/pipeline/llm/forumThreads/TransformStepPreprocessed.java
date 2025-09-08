package org.example.pipeline.llm.forumThreads;

import com.google.gson.Gson;
import org.example.datamodel.knowledge.ProcessedForumThread;
import org.example.datamodel.neo4j.Neo4JLink;
import org.example.datamodel.neo4j.Neo4jForumThread;
import org.example.datamodel.neo4j.Neo4jForumThreadTag;
import org.example.datamodel.neo4j.Neo4jType;
import org.example.interop.neo4j.Neo4jService;
import org.example.pipeline.IPipelineStep;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class TransformStepPreprocessed
    implements IPipelineStep<
        Stream<ProcessedForumThread>,
        Stream<TransformStep.IForumThreadOutput>> {
  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TransformStepPreprocessed.class);
  private final Gson _gson;
  private final Neo4jService _neo4jService;

  public TransformStepPreprocessed(Neo4jService neo4jService) {
    _gson = new Gson();
    _neo4jService = neo4jService;
  }

  record ThreadNode(Neo4jForumThread object) implements TransformStep.IForumThreadOutput {}
  record ThreadTagNode(Neo4jForumThreadTag object) implements TransformStep.IForumThreadOutput {}
  record ThreadLink(Neo4JLink object) implements TransformStep.IForumThreadOutput {}

  private Collection<Neo4jType> lookupTypeByNameOrSimpleName(String typeName) {
    _neo4jService.beginTransaction();
    Collection<Neo4jType> types = _neo4jService.runCypher(
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
    _neo4jService.commitTransactionIfPresent();
    return types;
  }

  public Collection<TransformStep.IForumThreadOutput> processSingle(ProcessedForumThread input) {
    try {
      List<String> tagsNames = input.processed().tags().stream()
          .map(String::toLowerCase)
          .map(t -> t.replace(" ", "_"))
          .toList();

      // Add forum tags
      List<ThreadTagNode> tags = tagsNames.stream()
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
              input.processed().problem_statement(),
              input.processed().context(),
              input.processed().solution(),
              input.processed().one_sentence_summary()));

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
      LOGGER.info("Trying to link types {}", input.processed().mentioned_classes());
      for (String typeName : input.processed().mentioned_classes()) {
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

      List<TransformStep.IForumThreadOutput> result = new LinkedList<>();
      result.add(threadNode);
      result.addAll(tags);
      result.addAll(links);

      return result;
    } catch (Exception e) {
      LOGGER.warn("Error: {}", e.getMessage());
      LOGGER.warn("Stack trace: ", e);
      return List.of();
    }
  }

  @Override
  public Stream<TransformStep.IForumThreadOutput> process(Stream<ProcessedForumThread> input) {
    return input.map(this::processSingle).flatMap(Collection::stream).distinct();
  }
}