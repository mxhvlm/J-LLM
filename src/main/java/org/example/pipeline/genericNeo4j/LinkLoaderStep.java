package org.example.pipeline.genericNeo4j;

import org.example.datamodel.neo4j.Neo4JLink;
import org.example.integration.api.neo4j.INeo4jProvider;
import org.example.integration.impl.neo4j.Neo4jProvider;
import org.example.pipeline.AbstractNeo4jLoaderStep;
import org.example.pipeline.IPipelineStep;
import org.example.pipeline.TransformResult;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Stream;

public class LinkLoaderStep extends AbstractNeo4jLoaderStep
    implements IPipelineStep<Stream<Neo4JLink>, TransformResult> {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(LinkLoaderStep.class);

    public LinkLoaderStep(INeo4jProvider neo4JProvider) {
        super(neo4JProvider);
    }

    @Override
    public TransformResult process(Stream<Neo4JLink> input) {
        List<Neo4JLink> links = input.toList();
        LOG.info("LinkExporter: Loading {} links...", links.size());

        _neo4JProvider.beginTransaction();
        links.forEach(_neo4JProvider::creatLinkNode);
        _neo4JProvider.commitTransactionIfPresent();

        return new TransformResult();
    }
}
