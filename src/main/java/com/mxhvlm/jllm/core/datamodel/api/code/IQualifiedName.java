package com.mxhvlm.jllm.core.datamodel.api.code;

import java.util.List;

public interface IQualifiedName {
    List<IQualifiedNamePart> getParts();

    String getQualifiedName();

    String getSimpleName();

    interface IQualifiedNamePart {
        String name();

        EnumQualifiedNameSeparator separator();
    }
}
