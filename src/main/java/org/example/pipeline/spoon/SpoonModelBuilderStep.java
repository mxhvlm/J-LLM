package org.example.pipeline.spoon;

import org.example.CodeModel;
import org.example.pipeline.IPipelineStep;
import org.example.sourceImport.ModelBuilder;

import java.util.List;

public class SpoonModelBuilderStep implements IPipelineStep<List<String>, CodeModel> {
    @Override
    public CodeModel process(List<String> input) {
        return new ModelBuilder().withInputResources(input).buildModel();
    }
}