package org.example.datamodel.code;

import org.example.datamodel.code.wrapper.*;

import java.util.Collection;

/**
 * Interface representing a parsed code model.
 *
 * @author MaxHvlm
 */
public interface ICodeModel {
    Collection<IPackage> getPackages();
    Collection<IField> getFields();
    Collection<IType> getTypes();
    Collection<IMethod> getMethods();
    Collection<IParameter> getParameters();
    Collection<IType> getInstantiatedTypes();
}
