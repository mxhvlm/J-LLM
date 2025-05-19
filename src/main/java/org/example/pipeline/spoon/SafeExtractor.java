package org.example.pipeline.spoon;

import org.slf4j.Logger;

public class SafeExtractor {

    public static <T> T safelyExtract(SafeExtractor.Extractor<T> extractor, T fallback, Logger logger) {
        try {
            return extractor.extract();
        } catch (Exception e) {
            logger.error("Error during extraction: " + e.getMessage());
            return fallback;
        }
    }

    @FunctionalInterface
    public interface Extractor<T> {
        T extract() throws Exception;
    }
}
