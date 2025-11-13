package com.mxhvlm.jllm.core.integration.impl.openAI;

record OpenAIChatResponseDTO(String id, String object, long created, String model, UsageDTO usage,
                             ChoiceDTO[] choices) {
    public record UsageDTO(int prompt_tokens, int completion_tokens, int total_tokens) {
    }

    public record MessageDTO(String role, String content) {
    }

    public record ChoiceDTO(int index, MessageDTO message, String finish_reason) {
    }
}
