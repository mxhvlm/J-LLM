package org.example.datamodel.api.knowledge;

import com.google.gson.annotations.SerializedName;

public record PreprocessedMethodAnalysis(
    String sourceCode,
    String methodId,
    String className,
    String explanation,
    @SerializedName("one_sentence_summary")
    String oneSentenceSummary
) {}
