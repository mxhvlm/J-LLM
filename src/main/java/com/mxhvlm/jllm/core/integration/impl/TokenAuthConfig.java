package com.mxhvlm.jllm.core.integration.impl;

import com.mxhvlm.jllm.core.integration.api.ITokenAuthConfig;

public record TokenAuthConfig(String url, String token) implements ITokenAuthConfig {
}
