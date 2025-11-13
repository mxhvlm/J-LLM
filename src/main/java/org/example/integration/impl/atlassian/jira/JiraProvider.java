package org.example.integration.impl.atlassian.jira;

import org.apache.commons.lang3.NotImplementedException;
import org.example.integration.api.IApiResponse;
import org.example.integration.api.projectmanagement.IProjectManagementProvider;
import org.example.datamodel.api.projectmanagement.ITicket;

import java.util.List;

public class JiraProvider implements IProjectManagementProvider {

    @Override
    public IApiResponse<List<ITicket>> getAllTickets() {
        throw new NotImplementedException();
    }

    @Override
    public IApiResponse<List<ITicket>> getTicketsForProject(String projectIdentifier) {
        throw new NotImplementedException();
    }

    @Override
    public IApiResponse<List<ITicket>> getTicketsByQuery(String query) {
        throw new NotImplementedException();
    }
}
