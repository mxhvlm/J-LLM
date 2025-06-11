package org.example.pipeline.spoon;

import org.example.codeModel.CodeModel;
import org.example.pipeline.IPipelineStep;
import org.example.codeModel.impl.spoon.SpoonModelBuilder;

import java.util.List;

public class SpoonModelBuilderStep implements IPipelineStep<List<String>, CodeModel> {
    @Override
    public CodeModel process(List<String> input) {
        return new SpoonModelBuilder().withInputResources(input).buildModel();
    }
}