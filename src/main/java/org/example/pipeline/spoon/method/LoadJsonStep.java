package org.example.pipeline.spoon.method;

import org.example.pipeline.IPipelineStep;

import java.util.stream.Stream;

public class LoadJsonStep implements IPipelineStep<
    Stream<TransformerStep.IMethodTransformerOutput>,
    Stream<TransformerStep.IMethodTransformerOutput>> {

    @Override
    public Stream<TransformerStep.IMethodTransformerOutput> process(
        Stream<TransformerStep.IMethodTransformerOutput> input) {
        return input;
    }
}
