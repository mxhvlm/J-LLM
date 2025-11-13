package org.example.impl.datamodel.api.knowledge;

import java.util.List;

public record UnprocessedForumThread(
        String title,
        String thread_url,
        String version,
        String replies,
        String views,
        List<String> tags,
        String time,
        boolean solved,
        List<Comment> content
) {
    public record Comment(
            String author,
            String content_markdown
    ) {
    }
}
