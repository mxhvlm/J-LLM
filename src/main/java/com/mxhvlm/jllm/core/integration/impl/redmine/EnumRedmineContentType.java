package com.mxhvlm.jllm.core.integration.impl.redmine;

public enum EnumRedmineContentType {
    JSON,
    XML;

    String getMimeType() {
        return switch (this) {
            case JSON -> "application/json";
            case XML -> "application/xml";
        };
    }
}
