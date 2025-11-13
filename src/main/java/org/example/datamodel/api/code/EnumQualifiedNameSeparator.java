package org.example.datamodel.api.code;

public enum EnumQualifiedNameSeparator {
    METHOD,
    INNER_TYPE,
    PARAMETER,
    OTHER;

    public char toChar() {
        return switch (this) {
            case METHOD:
                yield '#';
            case INNER_TYPE:
                yield '$';
            case OTHER:
            default:
                yield '.';
        };
    }
}
