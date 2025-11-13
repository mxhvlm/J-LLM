package org.example.impl.datamodel.api.knowledge;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a simplified version of the ProcessedForumThread class.
 * We need to use a class to gracefully handle the nullability of fields
 * as this is read from a llm.
 */
public class SafeProcessedForumThread {
    public String problem_statement;
    public String context;
    public boolean solved;
    public String solution;
    public String one_sentence_summary;
    public List<String> mentioned_classes;
    public String version;
    public List<String> tags;
    public String update_from;

    public void fillDefaults() {
        if (problem_statement == null) problem_statement = "";
        if (context == null) context = "";
        if (solution == null) solution = "";
        if (one_sentence_summary == null) one_sentence_summary = "";
        if (mentioned_classes == null) mentioned_classes = new ArrayList<>();
        if (version == null) version = "";
        if (tags == null) tags = new ArrayList<>();
        if (update_from == null) update_from = "";
    }
}
