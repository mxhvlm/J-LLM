package org.example.pipeline.code;

import org.example.datamodel.api.code.ICodeModel;
import org.example.datamodel.impl.code.spoon.SpoonModelBuilder;
import org.example.pipeline.IPipelineStep;

import java.util.List;

public class SpoonModelBuilderStep implements IPipelineStep<List<String>, ICodeModel> {
    @Override
    public ICodeModel process(List<String> input) {
        return new SpoonModelBuilder().withInputResources(input).buildModel();
    }
}