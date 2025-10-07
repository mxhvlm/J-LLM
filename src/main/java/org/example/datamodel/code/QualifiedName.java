package org.example.datamodel.code;

import org.neo4j.driver.internal.shaded.reactor.util.annotation.Nullable;

import java.util.List;

public class QualifiedName {

    private final List<String> _parts;
    private final List<Character> _separators;
    private final String _precompName;

    public static QualifiedName of(String name, Character separator) {
        return new QualifiedName(null, name, separator);
    }

    public QualifiedName append(String name, Character separator) {
        return new QualifiedName(this, name, separator);
    }

    private QualifiedName(@Nullable QualifiedName parentName, String name, Character separator) {
        if (parentName == null) {
            _parts = List.of(name);
            _separators = List.of();
        } else {
            _parts = List.copyOf(parentName._parts);
            _parts.add(name);

            _separators = List.copyOf(parentName._separators);
            _separators.add(separator);
        }

        _precompName = buildQualifiedName();
    }

    private String buildQualifiedName() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < _parts.size(); i++) {
            if (i > 0) {
                sb.append(_separators.get(i - 1));
            }
            sb.append(_parts.get(i));
        }
        return sb.toString();
    }

    public String getSimpleName() {
        return _parts.get(_parts.size() - 1);
    }

    @Override
    public String toString() {
        return _precompName;
    }

    @Override
    public int hashCode() {
        return _precompName.hashCode();
    }

    public List<String> parts() {
        return _parts;
    }
}
