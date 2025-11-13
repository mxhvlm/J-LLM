package org.example.integration.impl.redmine.projectmanagement;

import org.apache.commons.lang3.NotImplementedException;
import org.example.datamodel.api.projectmanagement.IField;
import org.example.datamodel.api.projectmanagement.ITicket;
import org.example.datamodel.impl.projectmanagement.TicketField;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

public record RedmineIssueDTO(
        Integer id,
        RedmineIssueProjectDTO project,
        RedmineIssueTrackerDTO tracker,
        RedmineIssueStatusDTO status,
        RedmineIssuePriorityDTO priority,
        RedmineIssueAuthorDTO author,
        RedmineIssueCategoryDTO category,
        RedmineFixedVersionDTO fixed_version,
        String subject,
        String description,
        String start_date,
        String due_date,
        Double done_ratio,
        Boolean is_private,
        Date created_on,
        Date updated_on,
        Date closed_on
) implements ITicket {
    @Override
    public String getName() {
        return subject;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Optional<IField> getField(String fieldName) {
        return switch (fieldName) {
            case "id" -> Optional.of(new TicketField("id", id().toString()));
            case "project" -> Optional.of(new TicketField("project", project().name()));
            case "tracker" -> Optional.of(new TicketField("tracker", tracker().name()));
            case "status" -> Optional.of(new TicketField("status", status().name()));
            case "priority" -> Optional.of(new TicketField("priority", priority().name()));
            case "author" -> Optional.of(new TicketField("author", author().name()));
            case "category" -> Optional.of(new TicketField("category", category() != null ? category().name() : ""));
            case "fixed_version" ->
                    Optional.of(new TicketField("fixed_version", fixed_version() != null ? fixed_version().name() : ""));
            case "start_date" -> Optional.of(new TicketField("start_date", start_date));
            case "due_date" -> Optional.of(new TicketField("due_date", due_date));
            case "done_ratio" ->
                    Optional.of(new TicketField("done_ratio", done_ratio != null ? done_ratio.toString() : ""));
            case "is_private" ->
                    Optional.of(new TicketField("is_private", is_private != null ? is_private.toString() : ""));
            case "created_on" ->
                    Optional.of(new TicketField("created_on", created_on != null ? created_on.toString() : ""));
            case "updated_on" ->
                    Optional.of(new TicketField("updated_on", updated_on != null ? updated_on.toString() : ""));
            case "closed_on" ->
                    Optional.of(new TicketField("closed_on", closed_on != null ? closed_on.toString() : ""));
            default -> Optional.empty();
        };
    }

    @Override
    public Map<String, String> getAllFields() {
        throw new NotImplementedException("Not implemented yet");
    }

    public record RedmineIssueProjectDTO(Integer id, String name) {
    }

    public record RedmineIssueTrackerDTO(Integer id, String name) {
    }

    public record RedmineIssueStatusDTO(Integer id, String name) {
    }

    public record RedmineIssuePriorityDTO(Integer id, String name) {
    }

    public record RedmineIssueAuthorDTO(Integer id, String name) {
    }

    public record RedmineIssueCategoryDTO(Integer id, String name) {
    }

    public record RedmineFixedVersionDTO(Integer id, String name) {
    }
}
