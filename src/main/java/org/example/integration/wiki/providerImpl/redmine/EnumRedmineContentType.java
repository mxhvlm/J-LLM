package org.example.integration.wiki.providerImpl.redmine;

enum EnumRedmineContentType {
    JSON,
    XML;

    String getMimeType() {
        return switch (this) {
            case JSON -> "application/json";
            case XML -> "application/xml";
        };
    }
}
