package org.example.datamodel.code;

import org.apache.commons.lang3.NotImplementedException;
import org.example.code.wrapper.*;
import org.example.datamodel.code.wrapper.*;

import java.util.List;

import static org.example.JLLM.LOGGER;

/**
 * Value Object representing a code model containing types, methods, and parameters.
 *
 * @Author MaxHvlm
 */
public class CodeModel implements ICodeModel{
    private final String _name;

    private final List<IPackage> _allPackages;
    private final List<IType> _allTypes;
    private final List<IMethod> _allMethods;
    private final List<IParameter> _allParameters;
    private final List<IField> _allFields;

    //TODO: Check what requirements we have for getting instantiated types, then store them in an appropriate datastructure.

    public CodeModel(String name, List<IPackage> packages, List<IType> allTypes, List<IField> allFields, List<IMethod> allMethods, List<IParameter> allParameters) {
        _name = name;
        _allPackages = packages;
        _allTypes = allTypes;
        _allFields = allFields;
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
            "Model Name: " + _name,
            "Total Packages: " + _allPackages.size(),
            "Total Types: " + _allTypes.size(),
            "Total Methods: " + _allMethods.size(),
            "Total Parameters: " + _allParameters.size()
        };
    }

    @Override
    public List<IPackage> getPackages() {
        return _allPackages;
    }

    @Override
    public List<IField> getFields() {
        return _allFields;
    }

    @Override
    public List<IType> getTypes() {
        return _allTypes;
    }

    @Override
    public List<IMethod> getMethods() {
        return _allMethods;
    }

    @Override
    public List<IParameter> getParameters() {
        return _allParameters;
    }

    @Override
    public List<IType> getInstantiatedTypes() {
        throw new NotImplementedException("getInstantiatedTypes() is not implemented yet. Please implement this method to retrieve instantiated types from the code model.");
    }
}
