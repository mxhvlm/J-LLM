package org.example.impl.integration.impl.redmine;

import com.google.gson.JsonArray;

public record RedminePaginationDTO(JsonArray items, int total_count, int offset, int limit) {
}
