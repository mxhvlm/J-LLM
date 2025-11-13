package org.example.integration.impl;

import org.example.integration.api.ITokenAuthConfig;

public record TokenAuthConfig(String url, String token) implements ITokenAuthConfig { }
