package org.example.data;

public class Neo4jMethodObject {
    // String methodName, String declaringTypeName, String methodSignature, String modifiers, String sourceCode, String javadoc

    private final String _methodName;
    private final String _declaringTypeName;
    private final String _methodSignature;
    private final String _modifiers;
    private final String _sourceCode;
    private final String _javadoc;

    public Neo4jMethodObject(String methodName, String declaringTypeName, String methodSignature, String modifiers, String sourceCode, String javadoc) {
        _methodName = methodName;
        _declaringTypeName = declaringTypeName;
        _methodSignature = methodSignature;
        _modifiers = modifiers;
        _sourceCode = sourceCode;
        _javadoc = javadoc;
    }

    public String getMethodName() {
        return _methodName;
    }

    public String getDeclaringTypeName() {
        return _declaringTypeName;
    }

    public String getMethodSignature() {
        return _methodSignature;
    }

    public String getModifiers() {
        return _modifiers;
    }

    public String getSourceCode() {
        return _sourceCode;
    }

    public String getJavadoc() {
        return _javadoc;
    }
}
