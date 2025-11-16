package com.mxhvlm.jllm.core.datamodel.impl.projectmanagement;

import com.mxhvlm.jllm.core.datamodel.api.projectmanagement.IField;

public class TicketField implements IField {
    private final String _name;
    private final String _value;

    public TicketField(String name, String value) {
        _name = name;
        _value = value;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getValue() {
        return _value;
    }
}
