package org.example.impl.datamodel.impl.code;

import org.apache.commons.lang3.NotImplementedException;
import org.example.impl.datamodel.api.code.ICodeModel;
import org.example.impl.datamodel.api.code.wrapper.*;

import java.util.Collection;

import static org.example.impl.JLLM.LOGGER;

/**
 * Value Object representing a code model containing types, methods, and parameters.
 *
 * @Author MaxHvlm
 */
public class CodeModel implements ICodeModel {
    private final String _name;

    private final Collection<IPackage> _allPackages;
    private final Collection<IType> _allTypes;
    private final Collection<IMethod> _allMethods;
    private final Collection<IParameter> _allParameters;
    private final Collection<IField> _allFields;

    //TODO: Check what requirements we have for getting instantiated types, then store them in an appropriate datastructure.

    public CodeModel(String name, Collection<IPackage> packages, Collection<IType> allTypes, Collection<IField> allFields, Collection<IMethod> allMethods, Collection<IParameter> allParameters) {
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
        return new String[]{
                "Model Name: " + _name,
                "Total Packages: " + _allPackages.size(),
                "Total Types: " + _allTypes.size(),
                "Total Fields: " + _allFields.size(),
                "Total Methods: " + _allMethods.size(),
                "Total Parameters: " + _allParameters.size()
        };
    }

    @Override
    public Collection<IPackage> getPackages() {
        return _allPackages;
    }

    @Override
    public Collection<IField> getFields() {
        return _allFields;
    }

    @Override
    public Collection<IType> getTypes() {
        return _allTypes;
    }

    @Override
    public Collection<IMethod> getMethods() {
        return _allMethods;
    }

    @Override
    public Collection<IParameter> getParameters() {
        return _allParameters;
    }

    @Override
    public Collection<IType> getInstantiatedTypes() {
        throw new NotImplementedException("getInstantiatedTypes() is not implemented yet. Please implement this method to retrieve instantiated types from the code model.");
    }
}
