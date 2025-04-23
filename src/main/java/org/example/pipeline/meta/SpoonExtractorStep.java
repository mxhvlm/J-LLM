package org.example.pipeline.meta;

import org.example.CodeModel;
import org.example.JLLMConfig;
import org.example.dbOutput.Neo4jService;
import org.example.pipeline.GenericLinkLoaderStep;
import org.example.pipeline.Pipeline;
import org.example.pipeline.TransformResult;
import org.example.pipeline.spoon._package.PackageExtractorStep;
import org.example.pipeline.spoon._package.PackageLoaderStep;
import org.example.pipeline.spoon._package.PackageTransformerStep;
import org.example.pipeline.spoon.callGraph.CallGraphTransformerStep;
import org.example.pipeline.spoon.field.FieldExtractorStep;
import org.example.pipeline.spoon.field.FieldLoaderStep;
import org.example.pipeline.spoon.field.FieldTransformerStep;
import org.example.pipeline.spoon.method.MethodExtractorStep;
import org.example.pipeline.spoon.method.MethodLoadJsonStep;
import org.example.pipeline.spoon.method.MethodLoadStep;
import org.example.pipeline.spoon.method.MethodTransformerStep;
import org.example.pipeline.spoon.type.TypeExtractorStep;
import org.example.pipeline.spoon.type.TypeLoaderStep;
import org.example.pipeline.spoon.type.TypeTransformer;
import org.example.sourceImport.ModelBuilder;

public class SpoonExtractorStep extends AbstractNeo4jMetaStep {
    @Override
    public Neo4jService process(Neo4jService neo4jService) {
        var config = new JLLMConfig();
        var model = new ModelBuilder().addInputResource(config.getInputPath()).buildModel();
        model.printStatistics();

        Pipeline<CodeModel, TransformResult> packagePipeline = Pipeline
                .start(new PackageExtractorStep())
                .then(new PackageTransformerStep())
                .then(new PackageLoaderStep(neo4jService))
                .build();

        Pipeline<CodeModel, TransformResult> typePipeline = Pipeline
                .start(new TypeExtractorStep())
                .then(new TypeTransformer())
                .then(new TypeLoaderStep(neo4jService))
                .build();

        Pipeline<CodeModel, TransformResult> fieldPipeline = Pipeline
                .start(new TypeExtractorStep())
                .then(new FieldExtractorStep())
                .then(new FieldTransformerStep())
                .then(new FieldLoaderStep(neo4jService))
                .build();

        Pipeline<CodeModel, TransformResult> methodPipeline = Pipeline
                .start(new TypeExtractorStep())
                .then(new MethodExtractorStep())
                .then(new MethodTransformerStep())
                .then(new MethodLoadJsonStep())
                .then(new MethodLoadStep())
                .build();

        Pipeline<CodeModel, TransformResult> callGraphPipeline = Pipeline
                .start(new TypeExtractorStep())
                .then(new MethodExtractorStep())
                .then(new CallGraphTransformerStep())
                .then(new GenericLinkLoaderStep(neo4jService))
                .build();

        neo4jService.startSessionAndTransaction();
        packagePipeline.run(model);
        typePipeline.run(model);
        fieldPipeline.run(model);
        methodPipeline.run(model);
        callGraphPipeline.run(model);
        neo4jService.commitTransactionAndCloseSession();

        return neo4jService;
    }
}
