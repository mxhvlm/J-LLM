package org.example.integration.projectmanagement;

import java.util.List;

public interface IProjectManagementProvider {
    /**
     * Gets all tickets within the API user's scope.
     * @return A list of all tickets.
     */
    List<ITicket> getAllTickets();

    /**
     * Gets all tickets for a specific project.
     * @param projectIdentifier The identifier of the project within the project management system.
     * @return A list of tickets for the specified project.
     */
    List<ITicket> getTicketsForProject(String projectIdentifier);

    /**
     * Gets tickets based on a query string
     * @param query The query string to filter tickets within the project management system.
     * @return A list of tickets matching the query.
     */
    List<ITicket> getTicketsByQuery(String query);
}
