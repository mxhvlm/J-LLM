package org.example.data;

public class Neo4jPackageObject {
    private final String _packageName;
    private final String _simpleName;

    public Neo4jPackageObject(String packageName, String simpleName) {
        _packageName = packageName;
        _simpleName = simpleName;
    }

    public String getPackageName() {
        return _packageName;
    }

    public String getSimpleName() {
        return _simpleName;
    }
}
