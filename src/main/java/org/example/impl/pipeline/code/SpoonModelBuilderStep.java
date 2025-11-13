package org.example.impl.pipeline.code;

import org.example.impl.datamodel.api.code.ICodeModel;
import org.example.impl.datamodel.impl.code.spoon.SpoonModelBuilder;
import org.example.impl.pipeline.IPipelineStep;

import java.util.List;

public class SpoonModelBuilderStep implements IPipelineStep<List<String>, ICodeModel> {
    @Override
    public ICodeModel process(List<String> input) {
        return new SpoonModelBuilder().withInputResources(input).buildModel();
    }
}