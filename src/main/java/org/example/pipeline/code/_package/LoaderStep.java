package org.example.pipeline.code._package;

import org.example.datamodel.impl.neo4j.Neo4jPackage;
import org.example.integration.api.neo4j.INeo4jProvider;
import org.example.pipeline.AbstractNeo4jLoaderStep;
import org.example.pipeline.IPipelineStep;
import org.example.pipeline.TransformResult;
import org.neo4j.driver.Values;

import java.util.stream.Stream;

public class LoaderStep extends AbstractNeo4jLoaderStep
        implements IPipelineStep<Stream<TransformerStep.IPackageOutput>, TransformResult> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoaderStep.class);

    public LoaderStep(INeo4jProvider neo4JProvider) {
        super(neo4JProvider);
    }

    public void createPackageNode(Neo4jPackage object) {
        _neo4JProvider.runCypher(
                "CREATE (p:Package { name: $packageName, simpleName: $simpleName })",
                Values.parameters("packageName", object.packageName(), "simpleName", object.simpleName())
        );
    }

    @Override
    public TransformResult process(Stream<TransformerStep.IPackageOutput> input) {
        _neo4JProvider.beginTransaction();
        try {
            input.forEach(packageOutput -> {
                if (packageOutput instanceof TransformerStep.PackageNode pn) {
                    createPackageNode(pn.object());
                } else if (packageOutput instanceof TransformerStep.PackageLink pl) {
                    _neo4JProvider.creatLinkNode(pl.link());
                }
            });
        } finally {
            _neo4JProvider.commitTransactionIfPresent();
        }

        return new TransformResult();
    }
}
