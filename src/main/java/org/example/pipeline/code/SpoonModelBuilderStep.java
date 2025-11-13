package org.example.pipeline.code;

import org.example.datamodel.impl.code.CodeModel;
import org.example.pipeline.IPipelineStep;
import org.example.datamodel.impl.code.spoon.SpoonModelBuilder;

import java.util.List;

public class SpoonModelBuilderStep implements IPipelineStep<List<String>, CodeModel> {
    @Override
    public CodeModel process(List<String> input) {
        return new SpoonModelBuilder().withInputResources(input).buildModel();
    }
}