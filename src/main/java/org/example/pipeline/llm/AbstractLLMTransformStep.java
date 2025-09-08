package org.example.pipeline.llm;

import org.example.interop.llm.ILLMProvider;
import org.example.interop.llm.ILLMResponse;
import org.example.interop.llm.LLMConfig;
import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractLLMTransformStep<T, U> implements IPipelineStep<Stream<T>, Stream<U>> {
    private final ILLMProvider _llmProvider;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractLLMTransformStep.class);

    public AbstractLLMTransformStep(ILLMProvider llmProvider) {
        _llmProvider = llmProvider;
    }

    /**
     * Hook for subclasses to define the LLM query.
     * @param input The generic input object
     * @return The LLM query string
     */
    public abstract String getLLMQuery(T input);

    /**
     * Hook for subclasses to define the LLM configuration.
     * Gets called for each input object to make the configuration dynamic,
     * for example to set the max response token count based on the input size.
     * @param input The generic input object
     * @return The LLM configuration
     */
    public abstract LLMConfig getLLMConfig(T input);

    /**
     * Hook for subclasses to process the LLM response and build the output object.
     * Multiple output objects can be created from a single LLM response.
     * Example: Build a new node and a link based on a LLM response
     * @param input The generic input object
     * @param resp The LLM response string
     * @return The collection of output objects
     */
    public abstract Collection<U> processLLMResult(T input, String resp);

    private Collection<U> processSingle(T input) {
        ILLMResponse resp = _llmProvider.getLLMResponse(getLLMConfig(input), getLLMQuery(input));
        if (resp.isSuccess()) {
            LOGGER.info("LLM request succeeded: " + resp.getResponse().get());
            return processLLMResult(input, resp.getResponse().get());
        } else {
            LOGGER.error("LLM request failed: " + resp.getErrorMessage());
            LOGGER.error("Query: " + getLLMQuery(input));
            LOGGER.error("Config: " + getLLMConfig(input));
            return null;
        }
    }

    @Override
    public Stream<U> process(Stream<T> input) {
      return input
          .map(this::processSingle)
          .filter(Objects::nonNull)
          .flatMap(Collection::stream)
          .filter(Objects::nonNull);
    }
}
