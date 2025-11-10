package org.example.integration.projectmanagement;

public interface ITicket {
    String getName();
    String getDescription();
    IField getField(String fieldName);
}
