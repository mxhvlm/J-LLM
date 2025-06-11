package org.example.codeModel.impl.spoon;

import org.apache.commons.lang3.NotImplementedException;
import org.example.codeModel.CodeModel;
import org.example.codeModel.IModelBuilder;
import org.example.codeModel.wrapper.*;
import org.slf4j.Logger;
import spoon.Launcher;
import spoon.SpoonAPI;

import java.util.LinkedList;
import java.util.List;

public class SpoonModelBuilder implements IModelBuilder {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SpoonModelBuilder.class);
    private boolean _enableAutoImports = true;
    private String _modelName = "Model";

    public SpoonModelBuilder() {
        _inputResourcePaths = new LinkedList<>();
    }

    private final List<String> _inputResourcePaths;

    public IModelBuilder addInputResource(String inputResourcePath) {
        _inputResourcePaths.add(inputResourcePath);
        return this;
    }

    public IModelBuilder withInputResources(List<String> inputResourcePaths) {
        _inputResourcePaths.addAll(inputResourcePaths);
        return this;
    }

    public IModelBuilder noAutoImports() {
        _enableAutoImports = false;
        return this;
    }

    public IModelBuilder withModelName(String modelName) {
        _modelName = modelName;
        return this;
    }

    private List<IPackage> getPackages(SpoonAPI spoon) {
        throw new NotImplementedException("getPackages() is not implemented yet. Please implement this method to retrieve packages from the Spoon model.");
    }

    private List<IField> getFields(SpoonAPI spoon) {
        throw new NotImplementedException("getFields() is not implemented yet. Please implement this method to retrieve fields from the Spoon model.");
    }

    private List<IType> getTypes(SpoonAPI spoon) {
        throw new NotImplementedException("getTypes() is not implemented yet. Please implement this method to retrieve types from the Spoon model.");
    }

    private List<IMethod> getMethods(SpoonAPI spoon) {
        throw new NotImplementedException("getMethods() is not implemented yet. Please implement this method to retrieve methods from the Spoon model.");
    }

    private List<IParameter> getParameters(SpoonAPI spoon) {
        throw new NotImplementedException("getParameters() is not implemented yet. Please implement this method to retrieve parameters from the Spoon model.");
    }

    private List<IType> getInstantiatedTypes(SpoonAPI spoon) {
        throw new NotImplementedException("getInstantiatedTypes() is not implemented yet. Please implement this method to retrieve instantiated types from the Spoon model.");
    }

    @Override
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
        spoon.buildModel();

        CodeModel codeModel = new CodeModel(_modelName, getPackages(spoon),
            getTypes(spoon), getFields(spoon), getMethods(spoon), getParameters(spoon));

        LOGGER.info("Model built successfully.");
        return codeModel;
    }
}