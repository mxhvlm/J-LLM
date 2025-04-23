package org.example.pipeline.spoon.method;

import org.example.data.Neo4JLinkObject;
import org.example.data.Neo4jMethodObject;
import org.example.data.Neo4jParamObject;
import org.example.data.Neo4jTypeArgumentNode;

import java.util.List;

public class MethodTransformResult {

    private final List<Neo4jMethodObject> _methods;
    private final List<Neo4jParamObject> _params;
    private final List<Neo4JLinkObject> _links;
    private final List<Neo4jTypeArgumentNode> _typeArgs;

    public MethodTransformResult(List<Neo4jMethodObject> methods, List<Neo4jParamObject> params, List<Neo4JLinkObject> links, List<Neo4jTypeArgumentNode> typeArgs) {
        _methods = methods;
        _params = params;
        _links = links;
        _typeArgs = typeArgs;
    }

    public List<Neo4jMethodObject> getMethods() {
        return _methods;
    }

    public List<Neo4jParamObject> getParams() {
        return _params;
    }

    public List<Neo4JLinkObject> getLinks() {
        return _links;
    }

    public List<Neo4jTypeArgumentNode> getTypeArgs() {
        return _typeArgs;
    }
}
