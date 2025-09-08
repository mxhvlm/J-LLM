package org.example.pipeline.code;

import org.example.datamodel.code.CodeModel;
import org.example.pipeline.IPipelineStep;
import org.example.datamodel.code.impl.spoon.SpoonModelBuilder;

import java.util.List;

public class SpoonModelBuilderStep implements IPipelineStep<List<String>, CodeModel> {
    @Override
    public CodeModel process(List<String> input) {
        return new SpoonModelBuilder().withInputResources(input).buildModel();
    }
}