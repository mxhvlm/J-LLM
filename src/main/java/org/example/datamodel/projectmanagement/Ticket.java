package org.example.datamodel.projectmanagement;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Ticket implements ITicket {

    private final String _name;
    private final String _description;

    private final Map<String, String> _fields;

    public Ticket(String name, String description, Map<String, String> otherFields) {
        _name = name;
        _description = description;
        _fields = otherFields;
        _fields.put("name", name);
        _fields.put("description", description);
    }

    public Ticket(String name, String description) {
        this(name, description, new HashMap<>());
    }

    /**
     * Constructor that accepts all fields in a map.
     * @param allFields A map containing all fields of the ticket, including "name" and "description".
     */
    public Ticket(Map<String, String> allFields) {
        _name = allFields.getOrDefault("name", "");
        _description = allFields.getOrDefault("description", "");
        _fields = allFields;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getDescription() {
        return _description;
    }

    @Override
    public Optional<IField> getField(String fieldName) {
        if (_fields.containsKey(fieldName)) {
            return Optional.of(new TicketField(fieldName, _fields.get(fieldName)));
        }
        return Optional.empty();
    }

    @Override
    public Map<String, String> getAllFields() {
        return _fields;
    }
}
