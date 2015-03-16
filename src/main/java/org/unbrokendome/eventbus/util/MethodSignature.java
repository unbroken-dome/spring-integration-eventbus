package org.unbrokendome.eventbus.util;

import java.lang.reflect.Method;
import java.util.Arrays;


public final class MethodSignature {

    private final String name;
    private final Class<?>[] parameterTypes;


    private MethodSignature(String name, Class<?>[] parameterTypes) {
        this.name = name;
        this.parameterTypes = parameterTypes;
    }


    public static MethodSignature of(Method method) {
        return new MethodSignature(method.getName(), method.getParameterTypes());
    }


    public String getName() {
        return name;
    }


    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }


    public boolean matches(Method method) {
        return method != null
                && name.equals(method.getName())
                && Arrays.equals(parameterTypes, method.getParameterTypes());
    }


    @Override
    public boolean equals(Object obj) {
        return (this == obj)
                || (obj instanceof MethodSignature && equals((MethodSignature) obj));
    }


    private boolean equals(MethodSignature other) {
        return this.name.equals(other.getName())
                && Arrays.equals(this.parameterTypes, other.getParameterTypes());
    }


    @Override
    public int hashCode() {
        return 31 * name.hashCode()
                + Arrays.hashCode(parameterTypes);
    }
}
