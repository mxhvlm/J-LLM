package org.example.pipeline.spoon._package;

import org.example.data.Neo4jPackageObject;
import org.example.dbOutput.Neo4jService;
import org.example.pipeline.AbstractNeo4jLoaderStep;
import org.example.pipeline.PipelineStep;
import org.example.pipeline.TransformResult;
import org.neo4j.driver.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackageLoaderStep extends AbstractNeo4jLoaderStep
        implements PipelineStep<Stream<PackageTransformerStep.PackageOutput>, TransformResult> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PackageLoaderStep.class);

    public PackageLoaderStep(Neo4jService neo4jService) {
        super(neo4jService);
    }

    public void createPackageNode(Neo4jPackageObject object) {
        _neo4jService.runCypherThreadsafe(
                "CREATE (p:Package { name: $packageName, simpleName: $simpleName })",
                Values.parameters("packageName", object.packageName(), "simpleName", object.simpleName())
        );
    }

    @Override
    public TransformResult process(Stream<PackageTransformerStep.PackageOutput> input) {
        // Sort such that all nodes are before links
        input
            .sorted((o1, o2) -> {
                if (o1 instanceof PackageTransformerStep.PackageNode && o2 instanceof PackageTransformerStep.PackageLink) {
                    return -1;
                } else if (o1 instanceof PackageTransformerStep.PackageLink && o2 instanceof PackageTransformerStep.PackageNode) {
                    return 1;
                }
                return 0;
            })
            .collect(Collectors.groupingByConcurrent(
                    ignored -> Thread.currentThread(), // group by actual thread
                    Collectors.toList()
            ))
            .values()
            .parallelStream()
            .forEach(batch -> {
                try {
                    for (PackageTransformerStep.PackageOutput output : batch) {
                        if (output instanceof PackageTransformerStep.PackageNode pn) {
                            createPackageNode(pn.object());
                        } else if (output instanceof PackageTransformerStep.PackageLink pl) {
                            _neo4jService.creatLinkNode(pl.link());
                        }
                    }
                } finally {
                    _neo4jService.commitThreadTransactionIfPresent();
                }
            });

        return new TransformResult();
    }
}
