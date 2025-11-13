package org.example.impl.integration.impl;

import org.example.impl.integration.api.ITokenAuthConfig;

public record TokenAuthConfig(String url, String token) implements ITokenAuthConfig {
}
