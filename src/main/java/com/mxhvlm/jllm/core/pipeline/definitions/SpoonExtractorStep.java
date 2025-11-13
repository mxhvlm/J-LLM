package com.mxhvlm.jllm.core.pipeline.definitions;

import com.mxhvlm.jllm.core.JLLMConfig;
import com.mxhvlm.jllm.core.datamodel.impl.code.CodeModel;
import com.mxhvlm.jllm.core.datamodel.impl.code.spoon.SpoonModelBuilder;
import com.mxhvlm.jllm.core.integration.api.neo4j.INeo4jProvider;
import com.mxhvlm.jllm.core.pipeline.Pipeline;
import com.mxhvlm.jllm.core.pipeline.TransformResult;
import com.mxhvlm.jllm.core.pipeline.step.code._package.ExtractorStep;
import com.mxhvlm.jllm.core.pipeline.step.code._package.LoaderStep;
import com.mxhvlm.jllm.core.pipeline.step.code._package.TransformerStep;
import com.mxhvlm.jllm.core.pipeline.step.code.method.LoadStep;

public class SpoonExtractorStep extends AbstractNeo4jMetaStep {

    @Override
    public INeo4jProvider process(INeo4jProvider neo4JProvider) {
        var config = new JLLMConfig();
        var model = new SpoonModelBuilder().addInputResource(config.getInputPath()).buildModel();
        model.printStatistics();

        Pipeline<CodeModel, TransformResult> packagePipeline = Pipeline
                .start(new ExtractorStep())
                .then(new TransformerStep())
                .then(new LoaderStep(neo4JProvider))
                .build();

        Pipeline<CodeModel, TransformResult> typePipeline = Pipeline
                .start(new com.mxhvlm.jllm.core.pipeline.step.code.type.ExtractorStep())
                .then(new com.mxhvlm.jllm.core.pipeline.step.code.type.TransformerStep())
                .then(new com.mxhvlm.jllm.core.pipeline.step.code.type.LoaderStep(neo4JProvider))
                .build();


        Pipeline<CodeModel, TransformResult> fieldPipeline = Pipeline
                .start(new com.mxhvlm.jllm.core.pipeline.step.code.type.ExtractorStep())
                .then(new com.mxhvlm.jllm.core.pipeline.step.code.field.ExtractorStep())
                .then(new com.mxhvlm.jllm.core.pipeline.step.code.field.TransformerStep())
                .then(new com.mxhvlm.jllm.core.pipeline.step.code.field.LoaderStep(neo4JProvider))
                .build();

        Pipeline<CodeModel, TransformResult> methodPipeline = Pipeline
                .start(new com.mxhvlm.jllm.core.pipeline.step.code.type.ExtractorStep())
                .then(new com.mxhvlm.jllm.core.pipeline.step.code.method.ExtractorStep())
                .then(new com.mxhvlm.jllm.core.pipeline.step.code.method.TransformerStep())
                .then(new LoadStep(neo4JProvider))
                .build();
//
//        Pipeline<CodeModel, TransformResult> methodPipeline = Pipeline
//            .start(new org.example.impl.pipeline.spoon.type.ExtractorStep())
//            .then(new org.example.impl.pipeline.spoon.method.ExtractorStep())
//            .then(new org.example.impl.pipeline.spoon.method.TransformerStep())
//            .then(new LoadJsonStep())
//            .then(new LoadStep(neo4jService))
//            .build();

//      TODO: Re-enable using wrapped code model
//        Pipeline<CodeModel, TransformResult> staticCallGraphPipeline = Pipeline
//            .start(new StaticExtractorStep())
//            .then(new StaticTransformerStep())
//            .then(new LinkLoaderStep(neo4jService))
//            .build();

//        Config dynamicCallGraphConfig = new Config(List.of("net.minecraft", "net.minecraftforge", "com.mojang"));
//        Pipeline<CodeModel, TransformResult> dynamicRTACallGraphPipeline = Pipeline
//            .start(new StaticExtractorStep())
//            .then(new InstantiatedTypeExtractionStep(dynamicCallGraphConfig))
//            .then(new DynamicTransformerStep(dynamicCallGraphConfig))
//            .then(new LinkLoaderStep(neo4jService))
//            .build();

//        neo4jService.startSessionAndTransaction();
        packagePipeline.run(model);
        typePipeline.run(model);
        fieldPipeline.run(model);
        methodPipeline.run(model);
//        methodPipeline.run(model);
//        staticCallGraphPipeline.run(model);
//        dynamicRTACallGraphPipeline.run(model);
//        neo4jService.commitTransactionAndCloseSession();

        return neo4JProvider;
    }
}
