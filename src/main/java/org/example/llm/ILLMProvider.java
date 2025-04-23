package org.example.llm;

public interface ILLMProvider {

    ILLMResponse getLLMResponse(ILLMConfig config, String input);
}
