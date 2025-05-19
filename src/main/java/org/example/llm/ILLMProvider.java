package org.example.llm;

public interface ILLMProvider {

    ILLMResponse getLLMResponse(LLMConfig config, String input);
}
