package com.mxhvlm.jllm.core.pipeline.step.code;

import org.slf4j.Logger;

/**
 * @deprecated Will be deleted once we ported the callGraph steps to the code model wrapper
 */
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
