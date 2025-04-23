package org.example.data;

public class Neo4jTypeObject {
    private final String _typeName;
    private final String _simpleName;
    private final String _typeKind;
    private final String _modifiers;
    private final String _javadoc;

    public Neo4jTypeObject(String typeName, String simpleName, String typeKind, String modifiers, String javadoc) {
        _typeName = typeName;
        _simpleName = simpleName;
        _typeKind = typeKind;
        _modifiers = modifiers;
        _javadoc = javadoc;
    }

    public String getTypeName() {
        return _typeName;
    }

    public String getSimpleName() {
        return _simpleName;
    }

    public String getTypeKind() {
        return _typeKind;
    }

    public String getModifiers() {
        return _modifiers;
    }

    public String getJavadoc() {
        return _javadoc;
    }
}
