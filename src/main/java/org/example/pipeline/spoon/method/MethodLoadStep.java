package org.example.pipeline.spoon.method;

import org.example.pipeline.PipelineStep;
import org.example.pipeline.TransformResult;

public class MethodLoadStep implements PipelineStep<MethodTransformResult, TransformResult> {

    @Override
    public TransformResult process(MethodTransformResult input) {
        return new TransformResult();
    }
}
