package org.example.integration.wiki.providerImpl.redmine;

import java.util.Collection;

record RedminePageListDTO(Collection<RedminePageListObjectDTO> wiki_pages) {
    record RedminePageListObjectDTO(String title, String version, String created_on, String updated_on) {
    }
}
