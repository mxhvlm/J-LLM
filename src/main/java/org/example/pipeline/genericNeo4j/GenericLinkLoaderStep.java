package org.example.pipeline.genericNeo4j;

import org.example.data.neo4j.Neo4JLink;
import org.example.dbOutput.Neo4jService;
import org.example.pipeline.AbstractNeo4jLoaderStep;
import org.example.pipeline.IPipelineStep;
import org.example.pipeline.TransformResult;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Stream;

public class GenericLinkLoaderStep extends AbstractNeo4jLoaderStep
    implements IPipelineStep<Stream<Neo4JLink>, TransformResult> {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(GenericLinkLoaderStep.class);

    public GenericLinkLoaderStep(Neo4jService neo4jService) {
        super(neo4jService);
    }

    @Override
    public TransformResult process(Stream<Neo4JLink> input) {
        List<Neo4JLink> links = input.toList();
        LOG.info("LinkExporter: Loading {} links...", links.size());

        _neo4jService.beginTransaction();
        links.forEach(_neo4jService::creatLinkNode);
        _neo4jService.commitTransactionIfPresent();

        return new TransformResult();
    }
}
