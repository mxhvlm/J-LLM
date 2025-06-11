package org.example.codeModel;

import org.example.codeModel.wrapper.*;

import java.util.List;

/**
 * Interface representing a parsed code model.
 *
 * @author MaxHvlm
 */
public interface ICodeModel {
    List<IPackage> getPackages();
    List<IField> getFields();
    List<IType> getTypes();
    List<IMethod> getMethods();
    List<IParameter> getParameters();
    List<IType> getInstantiatedTypes();
}
