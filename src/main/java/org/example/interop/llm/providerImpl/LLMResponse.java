package org.example.interop.llm.providerImpl;

import org.example.interop.llm.ILLMResponse;

import java.util.Optional;

class LLMResponse implements ILLMResponse {
    private final boolean _success;
    private final String _errorMessage;
    private final String _response;

    private LLMResponse(boolean success, String errorMessage, String response) {
        _success = success;
        _errorMessage = errorMessage;
        _response = response;
    }

    public static ILLMResponse success(String response) {
        return new LLMResponse(true, null, response);
    }

    public static ILLMResponse failure(String errorMessage) {
        return new LLMResponse(false, errorMessage, null);
    }

    @Override
    public boolean isSuccess() {
        return _success;
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(_errorMessage);
    }

    @Override
    public Optional<String> getResponse() {
        return Optional.ofNullable(_response);
    }
}
