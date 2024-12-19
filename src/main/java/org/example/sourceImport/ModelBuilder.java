package org.example.sourceImport;

import org.example.CodeModel;
import org.slf4j.Logger;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.CtModel;

public class ModelBuilder {
    private final String _inputPath;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ModelBuilder.class);
    public ModelBuilder(String inputPath) {
        _inputPath = inputPath;
    }

    public CodeModel buildModel() {
        LOGGER.info("Building model from input path: " + _inputPath);
        LOGGER.info("Creating SpoonAPI instance...");
        SpoonAPI spoon = new Launcher();
//        spoon.getEnvironment().setLevel("TRACE");

        LOGGER.info("Adding input resource...");
        spoon.addInputResource(_inputPath);

        LOGGER.info("Building model. Please be patient, this can take a few minutes...");
        CodeModel codeModel = new CodeModel(spoon.buildModel(), _inputPath);
        LOGGER.info("Model built successfully.");
        return codeModel;
    }
}
