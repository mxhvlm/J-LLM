package org.example.impl.integration.api.llm;

public interface ILLMConfig {
    String model();

    int contextLength();

    double temperature();

    int topK();

    double topP();

    int maxTokens();
}
