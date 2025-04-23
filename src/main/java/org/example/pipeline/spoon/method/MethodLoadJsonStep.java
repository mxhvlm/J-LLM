package org.example.pipeline.spoon.method;

import org.apache.commons.lang3.tuple.Pair;
import org.example.data.Neo4JLinkObject;
import org.example.data.Neo4jMethodObject;
import org.example.pipeline.PipelineStep;

import java.util.List;

public class MethodLoadJsonStep implements PipelineStep<MethodTransformResult, MethodTransformResult> {

    @Override
    public MethodTransformResult process(MethodTransformResult input) {
        return input;
    }
}
