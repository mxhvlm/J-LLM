package com.mxhvlm.jllm.core.datamodel.api.projectmanagement;

import java.util.Map;
import java.util.Optional;

public interface ITicket {
    String getName();

    String getDescription();

    Optional<IField> getField(String fieldName);

    Map<String, String> getAllFields();
}
