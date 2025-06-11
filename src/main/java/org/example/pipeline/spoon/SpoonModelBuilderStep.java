package org.example.pipeline.spoon;

import org.example.codeModel.CodeModel;
import org.example.pipeline.IPipelineStep;
import org.example.codeModel.ModelBuilder;

import java.util.List;

public class SpoonModelBuilderStep implements IPipelineStep<List<String>, CodeModel> {
    @Override
    public CodeModel process(List<String> input) {
        return new ModelBuilder().withInputResources(input).buildModel();
    }
}