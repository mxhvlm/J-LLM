package org.example.datamodel.impl.code;

import org.example.datamodel.api.code.EnumQualifiedNameSeparator;
import org.example.datamodel.api.code.IQualifiedName;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class QualifiedName implements IQualifiedName {
    private final List<IQualifiedNamePart> _parts;
    private final String _cachedQualifiedName;
    private final String _cachedSimpleName;
    private final int _cachedHash; // cache since String#hashCode may be called often

    public QualifiedName(List<IQualifiedNamePart> parts) {
        _parts = Collections.unmodifiableList(parts);

        StringBuilder sb = new StringBuilder();
        for (IQualifiedNamePart part : _parts) {
            if (!sb.isEmpty()) {
                sb.append(part.separator().toChar());
            }
            sb.append(part.name());
        }
        _cachedQualifiedName = sb.toString();
        _cachedSimpleName = _parts.isEmpty() ? null : _parts.get(_parts.size() - 1).name();
        _cachedHash = _cachedQualifiedName.hashCode();
    }

    @Override
    public String toString() {
        return _cachedQualifiedName;
    }

    @Override
    public int hashCode() {
        return _cachedHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        QualifiedName other = (QualifiedName) obj;
        return _cachedQualifiedName.equals(other._cachedQualifiedName);
    }

    @Override
    public List<IQualifiedNamePart> getParts() {
        return _parts;
    }

    @Override
    public String getQualifiedName() {
        return _cachedQualifiedName;
    }

    @Override
    public String getSimpleName() {
        return _cachedSimpleName;
    }

    public record Part(String name, EnumQualifiedNameSeparator separator) implements IQualifiedNamePart {
        public Part {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(separator, "separator");
        }
    }
}
