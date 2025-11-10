package org.example.integration.llm;

public interface ILLMProvider {

    ILLMResponse getLLMResponse(LLMConfig config, String input);
}
