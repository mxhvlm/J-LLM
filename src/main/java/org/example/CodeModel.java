package org.example;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.List;
import java.util.stream.Stream;

import static org.example.JLLM.LOGGER;

public class CodeModel {
    private final CtModel _model;
    private final String _name;

    private final List<CtType<?>> _allTypes;
    private final List<CtMethod<?>> _allMethods;
    private final List<CtParameter<?>> _allParameters;

    public CodeModel(CtModel model, String name, List<CtType<?>> allTypes, List<CtMethod<?>> allMethods, List<CtParameter<?>> allParameters) {
        _model = model;
        _name = name;
        _allTypes = allTypes;
        _allMethods = allMethods;
        _allParameters = allParameters;
    }

    public void printStatistics() {
        LOGGER.info("Model statistics for " + _name + ":");
        for (String stat : getStatistics()) {
            LOGGER.info(stat);
        }
    }

    public String[] getStatistics() {
        return new String[] {
            "Number of classes: " + _model.getAllTypes().size() + " from processor: " + _allTypes.size(),
            "Number of methods: " + _model.getElements(f -> f instanceof CtMethod<?>).size() + " from processor: " + _allMethods.size(),
            "Number of packages: " + _model.getAllPackages().size(),
            "Number of fields: " + _model.getElements(f -> f instanceof CtField<?>).size()
        };
    }

    public CtModel getCtModel() {
        return _model;
    }
}
