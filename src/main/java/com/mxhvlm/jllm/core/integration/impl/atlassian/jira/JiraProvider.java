package com.mxhvlm.jllm.core.integration.impl.atlassian.jira;

import com.mxhvlm.jllm.core.datamodel.api.projectmanagement.ITicket;
import com.mxhvlm.jllm.core.integration.api.IApiResponse;
import com.mxhvlm.jllm.core.integration.api.projectmanagement.IProjectManagementProvider;
import org.apache.commons.lang3.NotImplementedException;

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
