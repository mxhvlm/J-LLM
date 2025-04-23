package org.example.pipeline.spoon._package;

import org.apache.commons.lang3.tuple.Pair;
import org.example.data.Neo4JLinkObject;
import org.example.data.Neo4jPackageObject;
import org.example.dbOutput.Neo4jService;
import org.example.pipeline.AbstractNeo4jLoaderStep;
import org.example.pipeline.PipelineStep;
import org.example.pipeline.TransformResult;
import org.neo4j.driver.Values;

import java.util.List;

public class PackageLoaderStep extends AbstractNeo4jLoaderStep
        implements PipelineStep<Pair<List<Neo4jPackageObject>, List<Neo4JLinkObject>>, TransformResult> {

    public PackageLoaderStep(Neo4jService neo4jService) {
        super(neo4jService);
    }

    public void createPackageNode(Neo4jPackageObject object) {
        // If we know packages are always new, we can use CREATE to avoid MERGE overhead:
        _neo4jService.runCypher(
                "CREATE (p:Package { name: $packageName, simpleName: $simpleName })",
                Values.parameters("packageName", object.getPackageName(), "simpleName", object.getSimpleName())
        );
    }

    @Override
    public TransformResult process(Pair<List<Neo4jPackageObject>, List<Neo4JLinkObject>> input) {
        _neo4jService.startSessionAndTransaction();
        input.getLeft().forEach(this::createPackageNode);
        input.getRight().forEach(_neo4jService::creatLinkNode);
        _neo4jService.commitTransactionAndCloseSession();
        return new TransformResult();
    }
}
