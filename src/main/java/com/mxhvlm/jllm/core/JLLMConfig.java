package com.mxhvlm.jllm.core;

public class JLLMConfig {
    private final String _neo4jUri = "bolt://localhost:7687";
    private final String _neo4jUser = "neo4j";
    private final String _neo4jPassword = "password";
    private final String _inputPath = "analyzeIn/output/";

    public String getNeo4jUri() {
        return _neo4jUri;
    }

    public String getNeo4jUser() {
        return _neo4jUser;
    }

    public String getNeo4jPassword() {
        return _neo4jPassword;
    }

    public String getInputPath() {
        return _inputPath;
    }
}
