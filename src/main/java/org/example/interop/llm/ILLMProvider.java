package org.example.interop.llm;

public interface ILLMProvider {

    ILLMResponse getLLMResponse(LLMConfig config, String input);
}
