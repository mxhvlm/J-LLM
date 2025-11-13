package com.mxhvlm.jllm.core.pipeline.step.code.callGraph.dynamic;

import java.util.List;

// Scopes can be "net.minecraft", "net.minecraftforge", "com.mojang", etc.
public record Config(List<String> scope) {
}