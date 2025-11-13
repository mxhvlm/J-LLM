package org.example.datamodel.impl.code.wrapper;

import org.example.datamodel.api.code.IQualifiedName;
import org.example.datamodel.api.code.wrapper.ICodeObjectRegister;
import org.example.datamodel.api.code.wrapper.ICodeObjectRegistry;
import org.example.datamodel.api.code.wrapper.INamedElement;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CodeObjectRegistry implements ICodeObjectRegistry {
    private final Map<Class<?>, ICodeObjectRegister<? extends INamedElement>> _registers;

    public CodeObjectRegistry() {
        _registers = new HashMap<>();
    }

    @Override
    public <T, U> void createRegister(Class<T> wrapperClass, Class<U> libraryClass) {
        ICodeObjectRegister<? extends INamedElement> register = new CodeObjectRegister<>();
        _registers.put(wrapperClass, register);
//        _registers.put(libraryClass, register);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends INamedElement> ICodeObjectRegister<T> getRegister(Class<T> clazz) {
        return (CodeObjectRegister<T>) _registers.get(clazz);
    }

    @Override
    public Stream<INamedElement> getAllObjects() {
        return _registers.values().stream()
                .flatMap(register -> register.getAll().stream());
    }

    // T = IPackage, U = corresponding library object (e.g., spoon.reflect.declaration.CtPackage)
    public static class CodeObjectRegister<T extends INamedElement> implements ICodeObjectRegister<T> {

        private final Map<IQualifiedName, T> _nameRegister;

        public CodeObjectRegister() {
            _nameRegister = new HashMap<>();
        }

        @Override
        public T getOrCreate(IQualifiedName name, Supplier<T> supplier) {
            return _nameRegister.computeIfAbsent(name, k -> supplier.get());
        }

        @Override
        public T get(IQualifiedName name) {
            return _nameRegister.get(name);
        }

        @Override
        public List<T> getAll() {
            return new LinkedList<>(_nameRegister.values());
        }
    }
}