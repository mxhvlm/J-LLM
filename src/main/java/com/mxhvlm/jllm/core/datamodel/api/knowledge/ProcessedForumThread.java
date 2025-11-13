package com.mxhvlm.jllm.core.datamodel.api.knowledge;

import java.util.List;

public record ProcessedForumThread(
        String title,
        String thread_url,
        String version,
        String replies,
        String views,
        List<String> tags,
        String time,
        Boolean solved,
        List<Comment> content,
        Processed processed
) {
    public record Comment(
            String author,
            String content_markdown
    ) {
    }

    public record Processed(
            String problem_statement,
            String context,
            Boolean solved,
            String solution,
            String one_sentence_summary,
            List<String> mentioned_classes,
            String version,
            List<String> tags,
            String update_from
    ) {
    }
}