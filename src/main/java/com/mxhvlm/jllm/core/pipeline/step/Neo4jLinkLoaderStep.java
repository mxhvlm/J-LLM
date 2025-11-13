package com.mxhvlm.jllm.core.pipeline.step;

import com.mxhvlm.jllm.core.datamodel.impl.neo4j.Neo4JLink;
import com.mxhvlm.jllm.core.integration.api.neo4j.INeo4jProvider;
import com.mxhvlm.jllm.core.pipeline.IPipelineStep;
import com.mxhvlm.jllm.core.pipeline.TransformResult;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Stream;

public class Neo4jLinkLoaderStep extends AbstractNeo4jLoaderStep
        implements IPipelineStep<Stream<Neo4JLink>, TransformResult> {
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(Neo4jLinkLoaderStep.class);

    public Neo4jLinkLoaderStep(INeo4jProvider neo4JProvider) {
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
