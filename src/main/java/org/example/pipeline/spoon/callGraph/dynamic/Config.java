package org.example.pipeline.spoon.callGraph.dynamic;

import java.util.List;

// Scopes can be "net.minecraft", "net.minecraftforge", "com.mojang", etc.
public record Config(List<String> scope) {}