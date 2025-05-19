package org.example.pipeline.meta;

import org.example.CodeModel;
import org.example.JLLMConfig;
import org.example.dbOutput.Neo4jService;
import org.example.pipeline.genericNeo4j.GenericLinkLoaderStep;
import org.example.pipeline.Pipeline;
import org.example.pipeline.TransformResult;
import org.example.pipeline.spoon._package.ExtractorStep;
import org.example.pipeline.spoon._package.LoaderStep;
import org.example.pipeline.spoon._package.TransformerStep;
import org.example.pipeline.spoon.callGraph.StaticExtractorStep;
import org.example.pipeline.spoon.callGraph.StaticTransformerStep;
import org.example.pipeline.spoon.callGraph.dynamic.Config;
import org.example.pipeline.spoon.callGraph.dynamic.DynamicTransformerStep;
import org.example.pipeline.spoon.callGraph.dynamic.InstantiatedTypeExtractionStep;
import org.example.pipeline.spoon.method.LoadJsonStep;
import org.example.pipeline.spoon.method.LoadStep;
import org.example.pipeline.spoon.type.Transformer;
import org.example.sourceImport.ModelBuilder;

import java.util.List;

public class SpoonExtractorStep extends AbstractNeo4jMetaStep {

    @Override
    public Neo4jService process(Neo4jService neo4jService) {
        var config = new JLLMConfig();
        var model = new ModelBuilder().addInputResource(config.getInputPath()).buildModel();
        model.printStatistics();

        Pipeline<CodeModel, TransformResult> packagePipeline = Pipeline
            .start(new ExtractorStep())
            .then(new TransformerStep())
            .then(new LoaderStep(neo4jService))
            .build();

        Pipeline<CodeModel, TransformResult> typePipeline = Pipeline
            .start(new org.example.pipeline.spoon.type.ExtractorStep())
            .then(new Transformer())
            .then(new org.example.pipeline.spoon.type.LoaderStep(neo4jService))
            .build();

        Pipeline<CodeModel, TransformResult> fieldPipeline = Pipeline
            .start(new org.example.pipeline.spoon.type.ExtractorStep())
            .then(new org.example.pipeline.spoon.field.ExtractorStep())
            .then(new org.example.pipeline.spoon.field.TransformerStep())
            .then(new org.example.pipeline.spoon.field.LoaderStep(neo4jService))
            .build();

        Pipeline<CodeModel, TransformResult> methodPipeline = Pipeline
            .start(new org.example.pipeline.spoon.type.ExtractorStep())
            .then(new org.example.pipeline.spoon.method.ExtractorStep())
            .then(new org.example.pipeline.spoon.method.TransformerStep())
            .then(new LoadJsonStep())
            .then(new LoadStep(neo4jService))
            .build();
//
        Pipeline<CodeModel, TransformResult> staticCallGraphPipeline = Pipeline
            .start(new StaticExtractorStep())
            .then(new StaticTransformerStep())
            .then(new GenericLinkLoaderStep(neo4jService))
            .build();

        Config dynamicCallGraphConfig = new Config(List.of("net.minecraft", "net.minecraftforge", "com.mojang"));
        Pipeline<CodeModel, TransformResult> dynamicRTACallGraphPipeline = Pipeline
            .start(new StaticExtractorStep())
            .then(new InstantiatedTypeExtractionStep(dynamicCallGraphConfig))
            .then(new DynamicTransformerStep(dynamicCallGraphConfig))
            .then(new GenericLinkLoaderStep(neo4jService))
            .build();

//        neo4jService.startSessionAndTransaction();
        packagePipeline.run(model);
        typePipeline.run(model);
        fieldPipeline.run(model);
        methodPipeline.run(model);
        staticCallGraphPipeline.run(model);
        dynamicRTACallGraphPipeline.run(model);
//        neo4jService.commitTransactionAndCloseSession();

        return neo4jService;
    }
}
