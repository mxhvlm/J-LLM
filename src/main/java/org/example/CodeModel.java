package org.example;

import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

import static org.example.JLLM.LOGGER;

public class CodeModel {
    private final CtModel _model;
    private final String _name;

    public CodeModel(CtModel model, String name) {
        _model = model;
        _name = name;
    }

    public void printStatistics() {
        LOGGER.info("Model statistics for " + _name + ":");
        for (String stat : getStatistics()) {
            LOGGER.info(stat);
        }
    }

    public String[] getStatistics() {
        return new String[] {
            "Number of classes: " + _model.getAllTypes().size(),
            "Number of methods: " + _model.getElements(f -> f instanceof CtMethod<?>).size(),
            "Number of packages: " + _model.getAllPackages().size(),
            "Number of fields: " + _model.getElements(f -> f instanceof CtField<?>).size()
        };
    }

    public CtModel getCtModel() {
        return _model;
    }
}
