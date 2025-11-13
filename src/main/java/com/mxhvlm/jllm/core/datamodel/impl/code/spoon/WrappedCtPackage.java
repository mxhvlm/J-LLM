package com.mxhvlm.jllm.core.datamodel.impl.code.spoon;

import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.ICodeObjectRegistry;
import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.IPackage;
import com.mxhvlm.jllm.core.datamodel.api.code.wrapper.IType;
import com.mxhvlm.jllm.core.datamodel.impl.code.wrapper.AbstractWrappedPackage;
import spoon.reflect.declaration.CtPackage;

import java.util.List;

public class WrappedCtPackage extends AbstractWrappedPackage<CtPackage> implements IPackage {

    public WrappedCtPackage(CtPackage pkg) {
        super(pkg, QualifiedNameFactory.fromCtElement(pkg));
    }

    @Override
    protected List<IType> resolveSubTypes(ICodeObjectRegistry registry) {
        return getWrappedObject().getTypes()
                .stream()
                .map(type -> registry.getRegister(IType.class).getOrCreate(
                        QualifiedNameFactory.fromCtElement(type),
                        () -> new WrappedCtType(type)))
                .toList();
    }

    @Override
    protected List<IPackage> resolveSubPackages(ICodeObjectRegistry registry) {
        return getWrappedObject().getPackages()
                .stream()
                .map(subPkg -> registry.getRegister(IPackage.class).getOrCreate(
                        QualifiedNameFactory.fromCtElement(subPkg), () -> new WrappedCtPackage(subPkg)))
                .toList();
    }
}
