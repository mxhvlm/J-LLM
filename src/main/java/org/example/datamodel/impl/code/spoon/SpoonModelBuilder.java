package org.example.datamodel.impl.code.spoon;

import org.apache.commons.lang3.NotImplementedException;
import org.example.datamodel.api.code.IModelBuilder;
import org.example.datamodel.api.code.wrapper.*;
import org.example.datamodel.impl.code.CodeModel;
import org.example.datamodel.impl.code.wrapper.CodeObjectRegistry;
import org.slf4j.Logger;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.declaration.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SpoonModelBuilder implements IModelBuilder {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SpoonModelBuilder.class);

    private final ICodeObjectRegistry _objectRegistry;
    private final List<String> _inputResourcePaths;
    private boolean _enableAutoImports = true;
    private String _modelName = "Model";

    public SpoonModelBuilder() {
        _inputResourcePaths = new LinkedList<>();
        _objectRegistry = new CodeObjectRegistry();

        _objectRegistry.createRegister(IPackage.class, CtPackage.class);
        _objectRegistry.createRegister(IType.class, CtType.class);
        _objectRegistry.createRegister(IField.class, CtField.class);
        _objectRegistry.createRegister(IMethod.class, CtMethod.class);
        _objectRegistry.createRegister(IParameter.class, CtParameter.class);
    }

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
                .map(p -> _objectRegistry.getRegister(IPackage.class).getOrCreate(QualifiedNameFactory.fromCtElement(p), () -> new WrappedCtPackage(p)))
                .toList();
    }

    private List<IType> getInstantiatedTypes(SpoonAPI spoon) {
        throw new NotImplementedException("getInstantiatedTypes() is not implemented yet. Please implement this method to retrieve instantiated types from the Spoon model.");
    }

    private <T extends INamedElement> List<T> filterScope(List<T> objects, List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return objects;
        }

        return objects.stream()
                .filter(obj -> scopes.stream().anyMatch(scope -> obj.getName().getQualifiedName().startsWith(scope)))
                .collect(Collectors.toList());
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
        LOGGER.info("Model built. Extracting elements...");

        // All package objects are created in the register

        LOGGER.info("Initially resolving packages...");
        // Start the iterative resolution process by resolving the packages
        getPackages(spoon)
                .forEach(p -> p.resolve(_objectRegistry));

        LOGGER.info("Iterative dependency resolution starting...");
        int iteration = 0;
        // TODO: If we keep iterating, we're adding more and more types, etc. from the java standard library.
        while (_objectRegistry.getAllObjects().anyMatch(e -> !e.isResolved())) {
            LOGGER.info("Resolution iteration: {}", iteration);

            LOGGER.info("Resolving packages...");
            _objectRegistry.getRegister(IPackage.class).getAll().stream().
                    filter(p -> !p.isResolved())
                    .forEach(p -> p.resolve(_objectRegistry));

            LOGGER.info("Resolving types...");
            _objectRegistry
                    .getRegister(IType.class)
                    .getAll()
                    .stream()
                    .filter(t -> !t.isResolved())
                    .forEach(t -> t.resolve(_objectRegistry));

            LOGGER.info("Resolving fields...");
            _objectRegistry
                    .getRegister(IField.class)
                    .getAll()
                    .stream()
                    .filter(f -> !f.isResolved())
                    .forEach(f -> f.resolve(_objectRegistry));

            LOGGER.info("Resolving methods...");
            // First resolve all the method objects
            _objectRegistry.
                    getRegister(IMethod.class)
                    .getAll()
                    .stream()
                    .filter(m -> !m.isResolved())
                    .forEach(m -> m.resolve(_objectRegistry));

            // once that's done, resolve the calls between the methods created in the last step
            _objectRegistry
                    .getRegister(IMethod.class)
                    .getAll()
                    .stream()
                    .filter(m -> !m.isResolved())
                    .forEach(m -> m.resolveStaticRefs(_objectRegistry));

            LOGGER.info("Resolving parameters...");
            _objectRegistry.
                    getRegister(IParameter.class)
                    .getAll()
                    .stream()
                    .filter(p -> !p.isResolved())
                    .forEach(p -> p.resolve(_objectRegistry));

            LOGGER.info("Iteration {} complete. Unresolved objects remaining: {}",
                    iteration,
                    _objectRegistry.getAllObjects().filter(e -> !e.isResolved()).count());
            LOGGER.info("Unresolved: Packages: {}, Types: {}, Fields: {}, Methods: {}, Parameters: {}",
                    _objectRegistry.getRegister(IPackage.class).getAll().stream().filter(p -> !p.isResolved()).count(),
                    _objectRegistry.getRegister(IType.class).getAll().stream().filter(t -> !t.isResolved()).count(),
                    _objectRegistry.getRegister(IField.class).getAll().stream().filter(f -> !f.isResolved()).count(),
                    _objectRegistry.getRegister(IMethod.class).getAll().stream().filter(m -> !m.isResolved()).count(),
                    _objectRegistry.getRegister(IParameter.class).getAll().stream().filter(p -> !p.isResolved()).count()
            );
            iteration++;
        }

        LOGGER.info("Built wrapper model in {} iterations.", iteration);


        var scope = List.of("net.minecraft", "com.mojang", "net.minecraftforge", "org.lwjgl");
        //TODO: Remove the .distinct() filter as we should already get distinct objects from the register hashmaps.
        //TODO: Find out why that's not the case
        CodeModel codeModel = new CodeModel(
                _modelName,
                filterScope(_objectRegistry.getRegister(IPackage.class).getAll().stream().filter(IResolvable::isResolved).distinct().toList(), scope),
                filterScope(_objectRegistry.getRegister(IType.class).getAll().stream().filter(IResolvable::isResolved).distinct().toList(), scope),
                filterScope(_objectRegistry.getRegister(IField.class).getAll().stream().filter(IResolvable::isResolved).distinct().toList(), scope),
                filterScope(_objectRegistry.getRegister(IMethod.class).getAll().stream().filter(IResolvable::isResolved).distinct().toList(), scope),
                filterScope(_objectRegistry.getRegister(IParameter.class).getAll().stream().filter(IResolvable::isResolved).distinct().toList(), scope));

        codeModel.printStatistics();
        codeModel.getTypes()
                .stream()
                .map(IType::getName)
                .forEach(typeName -> LOGGER.info("Found type: " + typeName.getQualifiedName()));

        LOGGER.info("Model built successfully.");
        return codeModel;
    }
}