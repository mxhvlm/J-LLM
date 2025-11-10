package org.example.integration.projectmanagement.providerImpl;

import org.example.integration.IApiRequest;

public class NextPaginatedRequest<T extends IApiRequest> {
    private final T _initialRequest;
    public NextPaginatedRequest(T apiRequest) {
        _initialRequest = apiRequest;
    }
}
