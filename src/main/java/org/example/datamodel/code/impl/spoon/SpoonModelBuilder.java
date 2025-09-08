package org.example.datamodel.code.impl.spoon;

import org.apache.commons.lang3.NotImplementedException;
import org.example.datamodel.code.CodeModel;
import org.example.datamodel.code.IModelBuilder;
import org.example.code.wrapper.*;
import org.example.datamodel.code.wrapper.*;
import org.slf4j.Logger;
import spoon.Launcher;
import spoon.SpoonAPI;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
        return spoon.getModel()
            .getAllPackages()
            .stream()
            .filter(ctPackage -> !ctPackage.isUnnamedPackage())
            .map(WrappedCtPackage::new)
            .collect(Collectors.toUnmodifiableList());
    }

    private List<IField> getFields(SpoonAPI spoon) {
        return getTypes(spoon)
                .stream()
                .flatMap(type -> type.getFields().stream())
                .toList();
    }

    private List<IType> getTypes(SpoonAPI spoon) {
        return spoon.getModel()
            .getAllTypes()
            .stream()
            .map(t -> new WrappedCtType(t, null)) // TODO: Think about parent types
            .collect(Collectors.toUnmodifiableList());
    }

    private List<IMethod> getMethods(SpoonAPI spoon) {
        return getTypes(spoon)
            .stream()
            .flatMap(type -> type.getMethods().stream())
            .toList();
    }

    private List<IParameter> getParameters(SpoonAPI spoon) {
        return getMethods(spoon)
            .stream()
            .flatMap(method -> method.getParameters().stream())
            .toList();
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

        codeModel.printStatistics();
        codeModel.getTypes()
                .stream()
                .map(IType::getQualifiedName)
                .forEach(typeName -> LOGGER.info("Found type: " + typeName));

        LOGGER.info("Model built successfully.");
        return codeModel;
    }
}