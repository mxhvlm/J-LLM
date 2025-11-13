package org.example.integration.impl.redmine.wiki;

import java.util.Date;

record RedminePageContentDTO(String title, String parentTitle, String text, String version, String author, String comments, Date createdOn, Date updatedOn) {}
