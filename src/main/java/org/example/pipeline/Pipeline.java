package org.example.pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * A pipeline that can apply multiple steps in sequence:
 * each step's output type must match the next step's input type.
 *
 * @param <I> The initial input type for the first step
 * @param <O> The final output type after the last step
 */
public class Pipeline<I, O> {

    private final List<IPipelineStep<?, ?>> steps;

    private Pipeline(List<IPipelineStep<?, ?>> steps) {
        this.steps = steps;
    }

    /**
     * Creates a builder starting with the first step (from I -> O).
     */
    public static <I, O> Builder<I, O> start(IPipelineStep<I, O> firstPipelineStep) {
        return new Builder<>(firstPipelineStep);
    }

    /**
     * Runs the pipeline on the given input, sequentially passing
     * each step's output to the next step's input.
     */
    @SuppressWarnings("unchecked")
    public O run(I initialInput) {
        Object current = initialInput;
        for (IPipelineStep<?, ?> step : steps) {
            IPipelineStep<Object, Object> typedPipelineStep = (IPipelineStep<Object, Object>) step;
            current = typedPipelineStep.process(current);
        }
        return (O) current;
    }

    /**
     * The Builder holds a list of steps and provides a fluent API
     * to chain them. Each call to 'then' transforms the current
     * (input -> output) type to (output -> newOutput) type.
     */
    public static class Builder<I, O> {
        private final List<IPipelineStep<?, ?>> steps = new ArrayList<>();

        /**
         * The constructor accepts the first step.
         */
        private <A extends I, B extends O> Builder(IPipelineStep<A, B> firstPipelineStep) {
            steps.add(firstPipelineStep);
        }

        /**
         * Chain a new step that consumes the current output type O
         * and produces a new type N.
         *
         * @param nextPipelineStep The step from O -> N
         * @param <N>              The new output type
         * @return A new Builder from I -> N
         */
        @SuppressWarnings("unchecked")
        public <N> Builder<I, N> then(IPipelineStep<? super O, ? extends N> nextPipelineStep) {
            steps.add(nextPipelineStep);
            // We "become" a Builder<I, N> now
            return (Builder<I, N>) this;
        }

        /**
         * Finalizes the builder into a Pipeline<I, O>.
         */
        public Pipeline<I, O> build() {
            return new Pipeline<>(steps);
        }
    }
}

