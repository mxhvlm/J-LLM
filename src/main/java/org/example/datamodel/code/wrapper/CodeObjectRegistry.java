package org.example.datamodel.code.wrapper;

import org.example.datamodel.code.QualifiedName;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CodeObjectRegistry {
    private final Map<Class<?>, CodeObjectRegister<? extends INamedElement>> _registers;

    public CodeObjectRegistry() {
        _registers = new HashMap<>();
    }

    public <T, U> void createRegister(Class<T> wrapperClass, Class<U> libraryClass) {
        CodeObjectRegister<? extends INamedElement> register = new CodeObjectRegister<>();
        _registers.put(wrapperClass, register);
//        _registers.put(libraryClass, register);
    }

    @SuppressWarnings("unchecked")
    public <T extends INamedElement> CodeObjectRegister<T> getRegister(Class<T> clazz) {
        return (CodeObjectRegister<T>) _registers.get(clazz);
    }

    public Stream<INamedElement> getAllObjects() {
        return _registers.values().stream()
                .flatMap(register -> register.getAll().stream());
    }

    // T = IPackage, U = corresponding library object (e.g., spoon.reflect.declaration.CtPackage)
    public static class CodeObjectRegister<T extends INamedElement> {

        private final Map<QualifiedName, T> _nameRegister;

        public CodeObjectRegister() {
            _nameRegister = new HashMap<>();
        }

        public T getOrCreate(QualifiedName name, Supplier<T> supplier) {
            return _nameRegister.computeIfAbsent(name, k -> supplier.get());
        }

        public T get(QualifiedName name) {
            return _nameRegister.get(name);
        }

        public List<T> getAll() {
            return new LinkedList<>(_nameRegister.values());
        }
    }
}