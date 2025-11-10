package org.example.integration.projectmanagement.providerImpl;

import org.apache.commons.lang3.NotImplementedException;
import org.example.integration.projectmanagement.IProjectManagementProvider;
import org.example.integration.projectmanagement.ITicket;

import java.util.List;

public class JiraProvider implements IProjectManagementProvider {

    @Override
    public List<ITicket> getAllTickets() {
        throw new NotImplementedException();
    }

    @Override
    public List<ITicket> getTicketsForProject(String projectIdentifier) {
        throw new NotImplementedException();
    }

    @Override
    public List<ITicket> getTicketsByQuery(String query) {
        throw new NotImplementedException();
    }
}
