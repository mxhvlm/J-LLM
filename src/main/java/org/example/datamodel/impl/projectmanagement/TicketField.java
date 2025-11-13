package org.example.datamodel.impl.projectmanagement;

import org.example.datamodel.api.projectmanagement.IField;

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
