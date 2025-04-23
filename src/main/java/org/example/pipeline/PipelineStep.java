package org.example.pipeline;

/**
 * A pipeline step that transforms input of type I into output of type O.
 */
public interface PipelineStep<I, O> {
    O process(I input);
}
