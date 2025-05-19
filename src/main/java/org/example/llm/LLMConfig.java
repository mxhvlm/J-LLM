package org.example.llm;

public record LLMConfig(
    String model,
    int contextLength,
    double temperature,
    int topK,
    double topP,
    int maxTokens
) {
    @Override
    public String toString() {
        return "LLMConfig{" +
            "model='" + model + '\'' +
            ", contextLength=" + contextLength +
            ", temperature=" + temperature +
            ", topK=" + topK +
            ", topP=" + topP +
            ", maxTokens=" + maxTokens +
            '}';
    }

    public static class Builder {
        private String model;
        private int contextLength = 2048;
        private double temperature = 0.7;
        private int topK = 40;
        private double topP = 0.9;
        private int maxTokens = 512;

        public Builder() {}

        public Builder withModel(String model) {
            this.model = model;
            return this;
        }

        public Builder withContextLength(int contextLength) {
            this.contextLength = contextLength;
            return this;
        }

        public Builder withTemperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder withTopK(int topK) {
            this.topK = topK;
            return this;
        }

        public Builder withTopP(double topP) {
            this.topP = topP;
            return this;
        }

        public Builder withMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public LLMConfig build() {
            if (contextLength <= 0) throw new IllegalArgumentException("Context length must be greater than 0");
            if (temperature <= 0) throw new IllegalArgumentException("Temperature must be greater than 0");
            if (topK <= 0) throw new IllegalArgumentException("Top-K must be greater than 0");
            if (topP <= 0) throw new IllegalArgumentException("Top-P must be greater than 0");
            if (maxTokens <= 0) throw new IllegalArgumentException("Max tokens must be greater than 0");

            return new LLMConfig(model, contextLength, temperature, topK, topP, maxTokens);
        }

        public static Builder copyOf(LLMConfig config) {
            return new Builder()
                .withModel(config.model())
                .withContextLength(config.contextLength())
                .withTemperature(config.temperature())
                .withTopK(config.topK())
                .withTopP(config.topP())
                .withMaxTokens(config.maxTokens());
        }

        public static Builder defaultConfig() {
            return new Builder()
                .withModel("gpt-3.5-turbo")
                .withContextLength(2048)
                .withTemperature(0.7)
                .withTopK(40)
                .withTopP(0.9)
                .withMaxTokens(512);
        }
    }
}