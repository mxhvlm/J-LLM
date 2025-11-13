package org.example.integration.api.projectmanagement;

import org.example.datamodel.api.projectmanagement.ITicket;
import org.example.integration.api.IApiResponse;

import java.util.List;

public interface IProjectManagementProvider {
    /**
     * Gets all tickets within the API user's scope.
     * @return A list of all tickets.
     */
    IApiResponse<List<ITicket>> getAllTickets();

    /**
     * Gets all tickets for a specific project.
     * @param projectIdentifier The identifier of the project within the project management system.
     * @return A list of tickets for the specified project.
     */
    IApiResponse<List<ITicket>> getTicketsForProject(String projectIdentifier);

    /**
     * Gets tickets based on a query string
     * @param query The query string to filter tickets within the project management system.
     * @return A list of tickets matching the query.
     */
    IApiResponse<List<ITicket>> getTicketsByQuery(String query);
}
