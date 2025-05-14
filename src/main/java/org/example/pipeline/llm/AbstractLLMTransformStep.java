package org.example.pipeline.llm;

import org.example.llm.ILLMConfig;
import org.example.llm.ILLMProvider;
import org.example.llm.ILLMResponse;
import org.example.pipeline.PipelineStep;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractLLMTransformStep<T, U> implements PipelineStep<T, U> {
        private final ILLMProvider _llmProvider;

        private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractLLMTransformStep.class);

        public AbstractLLMTransformStep(ILLMProvider llmProvider) {
            _llmProvider = llmProvider;
        }

        public abstract String getLLMQuery(T input);
        public abstract ILLMConfig getLLMConfig(T input);
        public abstract T saveLLMResult(T input, String resp);

        private T processSingle(T input) {
            ILLMResponse resp = _llmProvider.getLLMResponse(getLLMConfig(input), getLLMQuery(input));
            if (resp.isSuccess()) {
                saveLLMResult(input, resp.getResponse().get());
            } else {
                LOGGER.error("LLM failed: " + resp.getErrorMessage());
                LOGGER.error("Query: " + getLLMQuery(input));
                LOGGER.error("Config: " + getLLMConfig(input));
            }
            return input;
        }
}
