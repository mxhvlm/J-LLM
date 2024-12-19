package org.example.sourceImport;

import org.example.CodeModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.*;

import java.util.HashSet;
import java.util.Set;

public class ModelExtractor {
    private final CodeModel _model;

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ModelExtractor.class);

    public ModelExtractor(CodeModel model) {
        LOGGER.info("ModelExtractor: Initializing...");
        _model = model;
    }

    public Set<String> extractPackages() {
        LOGGER.info("ModelExtractor: Extracting packages...");
        Set<String> packages = new HashSet<>();
        for (CtPackage ctPackage : _model.getCtModel().getAllPackages()) {
            // Spoon returns a root package "<empty>" which you might skip if it's not useful
            if (!ctPackage.isUnnamedPackage()) {
                packages.add(ctPackage.getQualifiedName());
            } else {
                LOGGER.warn("Skipping unnamed package.");
            }
        }
        LOGGER.info("ModelExtractor: Extracted " + packages.size() + " packages.");
        return packages;
    }

    public Set<String> extractModules() {
        LOGGER.info("ModelExtractor: Extracting modules...");
        Set<String> modules = new HashSet<>();
        for (CtModule ctModule : _model.getCtModel().getAllModules()) {
            // There might be a "unnamed" module, handle it as needed
            if (!ctModule.isUnnamedModule()) {
                modules.add(ctModule.getSimpleName());
            }
        }
        LOGGER.info("ModelExtractor: Extracted " + modules.size() + " modules.");
        return modules;
    }

    public Set<CtType<?>> extractTypes() {
        LOGGER.info("ModelExtractor: Extracting types...");
        var types = new HashSet<>(_model.getCtModel().getAllTypes());

        LOGGER.info("ModelExtractor: Extracted " + types.size() + " types.");
        return types;
    }

    /**
     * Extract all fields from all types.
     */
    public Set<CtField<?>> extractFields() {
        LOGGER.info("ModelExtractor: Extracting fields...");
        Set<CtField<?>> fields = new HashSet<>();
        for (CtType<?> type : extractTypes()) {
            fields.addAll(type.getFields());
        }
        LOGGER.info("ModelExtractor: Extracted " + fields.size() + " fields.");
        return fields;
    }

    /**
     * Extract all methods from all types.
     * Includes methods from classes, interfaces, etc.
     */
    public Set<CtMethod<?>> extractMethods() {
        LOGGER.info("ModelExtractor: Extracting methods...");
        Set<CtMethod<?>> methods = new HashSet<>();
        for (CtType<?> type : extractTypes()) {
            // getAllMethods() can be used if you need inherited methods too,
            // but usually type.getMethods() returns declared methods.
            methods.addAll(type.getMethods());
        }
        LOGGER.info("ModelExtractor: Extracted " + methods.size() + " methods.");
        return methods;
    }

    /**
     * Extract all invocations in the entire codebase.
     * We can use the model's elements and filter by CtInvocation.
     */
    public Set<CtInvocation<?>> extractInvocations() {
        LOGGER.info("ModelExtractor: Extracting invocations...");
        Set<CtInvocation<?>> invocations = new HashSet<>(_model.getCtModel().getElements(e -> e instanceof CtInvocation));
        LOGGER.info("ModelExtractor: Extracted " + invocations.size() + " invocations.");
        return invocations;
    }

    /**
     * Dependencies might be gathered during field and method analysis, or re-extracted here.
     * For now, we return an empty set or a placeholder. You should implement actual dependency logic.
     */
    public static class Dependency {
        private final String sourceType;
        private final String targetType;

        public Dependency(String sourceType, String targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        public String getSourceType() {
            return sourceType;
        }

        public String getTargetType() {
            return targetType;
        }
    }

    public Set<Dependency> extractDependencies() {
        LOGGER.info("ModelExtractor: Extracting dependencies...");
        // Placeholder: you need to implement logic to record dependencies during field/method export
        Set<Dependency> dependencies = new HashSet<>();
        LOGGER.info("ModelExtractor: Extracted " + dependencies.size() + " dependencies.");
        return dependencies;
    }
}
