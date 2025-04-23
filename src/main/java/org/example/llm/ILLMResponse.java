package org.example.llm;

import java.util.Optional;

public interface ILLMResponse {

    boolean isSuccess();
    Optional<String> getErrorMessage();
    Optional<String> getResponse();
 }
