package org.example.llm;

public interface ILLMConfig {
    void setContextLength(int contextLength);
    void setTemperature(double temperature);
    void setTopK(int topK);
    void setTopP(double topP);
    void setMaxTokens(int maxTokens);

    String getUrl();
    String getApiKey();

    int getContextLength();
    double getTemperature();
    int getTopK();
    double getTopP();
    int getMaxTokens();
}
