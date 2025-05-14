package org.example.sourceImport;

import org.example.CodeModel;
import org.slf4j.Logger;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ModelBuilder {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ModelBuilder.class);
    private boolean _enableAutoImports = true;
    private String _modelName = "Model";

//    private static class ExtractorListProcessor<T extends CtElement> extends AbstractProcessor<T> {
//        private final List<T> _results;
//        public ExtractorListProcessor(List<T> resultList) {
//             _results = resultList;
//        }
//        @Override
//        public void process(T element) {
//            _results.add(element);
//        }
//    }

    public ModelBuilder() {
        _inputResourcePaths = new LinkedList<>();
    }

    private final List<String> _inputResourcePaths;

    public ModelBuilder addInputResource(String inputResourcePath) {
        _inputResourcePaths.add(inputResourcePath);
        return this;
    }

    public ModelBuilder withInputResources(List<String> inputResourcePaths) {
        _inputResourcePaths.addAll(inputResourcePaths);
        return this;
    }

    public ModelBuilder noAutoImports() {
        _enableAutoImports = false;
        return this;
    }

    public ModelBuilder withModelName(String modelName) {
        _modelName = modelName;
        return this;
    }

    public CodeModel buildModel() {
        LOGGER.info("Building model: " + _modelName);

        if (_inputResourcePaths.isEmpty()) {
            throw new IllegalStateException("No input resources added. Please call ModelBuilder.addInputResource() first.");
        }

        LOGGER.info("Creating SpoonAPI instance...");
        SpoonAPI spoon = new Launcher();
//        spoon.getEnvironment().setLevel("TRACE");

        LOGGER.info("Adding input resources...");
        _inputResourcePaths.forEach(spoon::addInputResource);
        spoon.getEnvironment().setAutoImports(_enableAutoImports);
        LOGGER.info("Building model. Please be patient, this can take a few minutes...");

        List<CtType<?>> allTypes = new ArrayList<>();
        List<CtMethod<?>> allMethods = new ArrayList<>();
        List<CtParameter<?>> allParameters = new ArrayList<>();
//        spoon.addProcessor(new ExtractorListProcessor<>(allTypes));
//        spoon.addProcessor(new ExtractorListProcessor<>(allMethods));
//        spoon.addProcessor(new ExtractorListProcessor<>(allParameters));
        CodeModel codeModel = new CodeModel(spoon.buildModel(), _modelName, allTypes, allMethods, allParameters);
        LOGGER.info("Model built successfully.");
        return codeModel;
    }
}