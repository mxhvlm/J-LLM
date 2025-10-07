package org.example.datamodel.code.impl.spoon;

import org.example.datamodel.code.QualifiedName;
import org.example.datamodel.code.wrapper.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CodeObjectRegistry<T, U> {
    private final Map<Class<? extends INamedElement<T>>, CodeObjectRegister<? extends INamedElement<T>, U>> _registers;

    public CodeObjectRegistry() {
        _registers = new HashMap<>();
    }

    CodeObjectRegister<? extends INamedElement<T>, U> getRegister(Class<T> clazz) {
        return _registers.computeIfAbsent(clazz, k -> new CodeObjectRegister<INamedElement<T>, T>());
    }

    // T = IPackage, U = corresponding library object (e.g., spoon.reflect.declaration.CtPackage)
    static class CodeObjectRegister<T extends INamedElement<T>, U> {

        private final Map<QualifiedName, T> _nameRegister;
        private final Map<INamedElement<T>, U> _libObjectRegister;

        public CodeObjectRegister() {
            _nameRegister = new HashMap<>();
            _libObjectRegister = new HashMap<>();
        }

        public T getOrCreate(QualifiedName name, Supplier<T> supplier, U libObject) {
            return _nameRegister.computeIfAbsent(name, k -> {
                T obj = supplier.get();
                _libObjectRegister.put(obj, libObject);
                return obj;
            });
        }

        public T get(QualifiedName name) {
            return _nameRegister.get(name);
        }
    }

}