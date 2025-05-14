package org.example.data;

public class Neo4jFieldObject {
    // declaringTypeName, fieldName, modifiers, fieldSourceSnippet

    private final String _declaringTypeName;
    private final String _fieldName;
    private final String _modifiers;
    private final String _fieldSourceSnippet;

    public Neo4jFieldObject(String declaringTypeName, String fieldName, String modifiers, String fieldSourceSnippet) {
        _declaringTypeName = declaringTypeName;
        _fieldName = fieldName;
        _modifiers = modifiers;
        _fieldSourceSnippet = fieldSourceSnippet;
    }

    public String getDeclaringTypeName() {
        return _declaringTypeName;
    }

    public String getFieldName() {
        return _fieldName;
    }

    public String getModifiers() {
        return _modifiers;
    }

    public String getFieldSourceSnippet() {
        return _fieldSourceSnippet;
    }
}
