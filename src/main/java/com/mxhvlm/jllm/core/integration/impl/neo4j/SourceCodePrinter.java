package com.mxhvlm.jllm.core.integration.impl.neo4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.reflect.declaration.CtMethod;

class SourceCodePrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceCodePrinter.class);

    public static String printMethodSource(CtMethod<?> ctMethod) {
        try {
//            // Get the environment from the factory
//            Environment env = ctMethod.getFactory().getEnvironment();
//
//            // Enable simple imports and set no classpath mode to avoid dependency issues
//            env.setAutoImports(true);
//            env.setNoClasspath(true); // Avoids issues with missing dependencies
//            env.setPrettyPrintingMode(Environment.PRETTY_PRINTING_MODE.AUTOIMPORT);
//            env.setIgnoreDuplicateDeclarations(true);
//
//            // Create the pretty printer using the environment
//            DefaultJavaPrettyPrinter printer = new DefaultJavaPrettyPrinter(env);
//
//            // Print the source for the declaring type of this method
//            return printer.printElement(ctMethod);
            return ctMethod.toString();
        } catch (Exception e) {
            LOGGER.error("Error printing method source: " + e.getMessage(), e);
            return "ERROR";
        }
    }
}
