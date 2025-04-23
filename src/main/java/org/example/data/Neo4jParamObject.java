package org.example.data;

public class Neo4jParamObject {

    private final String _id;
    private final String _name;

    public Neo4jParamObject(String id, String name) {
        _id = id;
        _name = name;
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }
}
