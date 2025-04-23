package org.example.llm;

public class LLMConfig implements ILLMConfig {

    private final String _url;
    private final String _token;

    private int _contextLength;
    private double _temperature;
    private int _topK;
    private double _topP;
    private int _maxTokens;

    private LLMConfig(String url, String token, int contextLength, double temperature, int topK, int topP, int maxTokens) {
        _url = url;
        _token = token;
        _contextLength = contextLength;
        _temperature = temperature;
        _topK = topK;
        _topP = topP;
        _maxTokens = maxTokens;
    }

    public String getUrl() {
        return _url;
    }

    public String getApiKey() {
        return _token;
    }

    public int getContextLength() {
        return _contextLength;
    }

    public double getTemperature() {
        return _temperature;
    }

    public int getTopK() {
        return _topK;
    }

    public double getTopP() {
        return _topP;
    }

    public int getMaxTokens() {
        return _maxTokens;
    }

    public void setContextLength(int contextLength) {
        _contextLength = contextLength;
    }

    public void setTemperature(double temperature) {
        _temperature = temperature;
    }

    public void setTopK(int topK) {
        _topK = topK;
    }

    @Override
    public void setTopP(double topP) {
        _topP = topP;
    }

    public void setTopP(int topP) {
        _topP = topP;
    }

    public void setMaxTokens(int maxTokens) {
        _maxTokens = maxTokens;
    }

    @Override
    public String toString() {
        return "LLMConfig{" +
                "_url='" + _url + '\'' +
                ", _token='" + _token.substring(0, 4) + '\'' +
                ", _contextLength=" + _contextLength +
                ", _temperature=" + _temperature +
                ", _topK=" + _topK +
                ", _topP=" + _topP +
                ", _maxTokens=" + _maxTokens +
                '}';
    }

    public static class Builder {
        private String _url;
        private String _token;
        private int _contextLength;
        private double _temperature;
        private int _topK;
        private int _topP;
        private int _maxTokens;

        public static Builder create() {
            return new Builder();
        }

        public Builder withUrl(String url) {
            _url = url;
            return this;
        }

        public Builder withToken(String token) {
            _token = token;
            return this;
        }

        public Builder withContextLength(int contextLength) {
            _contextLength = contextLength;
            return this;
        }

        public Builder withTemperature(double temperature) {
            _temperature = temperature;
            return this;
        }

        public Builder withTopK(int topK) {
            _topK = topK;
            return this;
        }

        public Builder withTopP(int topP) {
            _topP = topP;
            return this;
        }

        public Builder withMaxTokens(int maxTokens) {
            _maxTokens = maxTokens;
            return this;
        }

        public LLMConfig build() {
            if (_url == null || _token == null) {
                throw new IllegalStateException("URL and token must be set");
            }
            if (_contextLength <= 0) {
                throw new IllegalStateException("Context length must be set and greater than 0");
            } else if (_temperature <= 0) {
                throw new IllegalStateException("Temperature must be set and greater than 0");
            } else if (_topK <= 0) {
                throw new IllegalStateException("Top-K must be set and greater than 0");
            } else if (_topP <= 0) {
                throw new IllegalStateException("Top-P must be set and greater than 0");
            } else if (_maxTokens <= 0) {
                throw new IllegalStateException("Max tokens must be set and greater than 0");
            }

            return new LLMConfig(_url, _token, _contextLength, _temperature, _topK, _topP, _maxTokens);
        }
    }
}
