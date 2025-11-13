package com.mxhvlm.jllm.core.integration.impl;

import com.mxhvlm.jllm.core.integration.api.IApiResponse;

import java.util.Optional;
import java.util.function.Function;

public class ApiResponse<T> implements IApiResponse<T> {
    private final boolean _success;
    private final String _errorMessage;
    private final T _response;

    private ApiResponse(boolean success, String errorMessage, T response) {
        _success = success;
        _errorMessage = errorMessage;
        _response = response;
    }

    public static <U> IApiResponse<U> success(U response) {
        return new ApiResponse<>(true, null, response);
    }

    public static <U> IApiResponse<U> failure(String errorMessage) {
        return new ApiResponse<>(false, errorMessage, null);
    }

    @Override
    public boolean isSuccess() {
        return _success;
    }

    @Override
    public boolean isFailure() {
        return !_success;
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(_errorMessage);
    }

    @Override
    public Optional<T> getResponse() {
        return Optional.ofNullable(_response);
    }

    @Override
    public <U> U map(Function<T, U> successHandler, Function<String, U> errorHandler) {
        return isSuccess()
                ? successHandler.apply(_response)
                : errorHandler.apply(_errorMessage);
    }
}
