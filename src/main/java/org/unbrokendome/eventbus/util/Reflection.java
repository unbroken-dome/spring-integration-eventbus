package org.unbrokendome.eventbus.util;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public final class Reflection {

    private static final int REFLECTION_CHARACTERISTICS =
            Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.IMMUTABLE;


    private Reflection() {
        // prevent instantiation
    }


    public static Stream<Class<?>> typeAndAllSuperTypes(Class<?> clazz) {
        if (clazz == null) {
            return Stream.empty();
        }

        return Stream.concat(
                Stream.of(clazz),
                allSuperTypes(clazz));
    }


    public static Stream<Class<?>> allSuperTypes(Class<?> clazz) {
        Stream<Class<?>> interfaces = StreamSupport.stream(
                () -> Spliterators.spliterator(Arrays.asList(clazz.getInterfaces()), REFLECTION_CHARACTERISTICS),
                REFLECTION_CHARACTERISTICS, true)
                .flatMap(Reflection::typeAndAllSuperTypes);

        return Stream.concat(typeAndAllSuperTypes(clazz.getSuperclass()), interfaces)
                .distinct();
    }


    public static Stream<Method> allMethods(Class<?> clazz) {
        return typeAndAllSuperTypes(clazz)
                .flatMap(Reflection::getDeclaredMethods);
    }


    public static Stream<Method> allMethodsMatching(Class<?> clazz, MethodSignature signature) {
        return typeAndAllSuperTypes(clazz)
                .map(cls -> safeGetMethod(cls, signature))
                .filter(Objects::nonNull);
    }


    private static Method safeGetMethod(Class<?> clazz, MethodSignature signature) {
        try {
            return clazz.getMethod(signature.getName(), signature.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }


    public static Stream<Method> getDeclaredMethods(Class<?> clazz) {
        return StreamSupport.stream(
                () -> Spliterators.spliterator(clazz.getDeclaredMethods(), REFLECTION_CHARACTERISTICS),
                REFLECTION_CHARACTERISTICS, true);
    }


    public static Comparator<Method> methodOverridesComparator() {
        return MethodOverridesComparator.INSTANCE;
    }


    private static class MethodOverridesComparator implements Comparator<Method> {

        public static final MethodOverridesComparator INSTANCE = new MethodOverridesComparator();


        private MethodOverridesComparator() { }


        @Override
        public int compare(Method method1, Method method2) {
            if (method1 == null || method2 == null) {
                throw new IllegalArgumentException("Argument must not be null");
            }
            if (method1 == method2) {
                return 0;
            }
            if (!MethodSignature.of(method1).matches(method2)) {
                throw new IllegalArgumentException("Method signatures do not match");
            }

            Class<?> declaringType1 = method1.getDeclaringClass();
            Class<?> declaringType2 = method2.getDeclaringClass();

            if (declaringType1.isAssignableFrom(declaringType2)) {
                // method2 overrides method1
                return -1;

            } else if (declaringType2.isAssignableFrom(declaringType1)) {
                // method1 overrides method2
                return 1;

            } else {
                // neither method overrides the other; this might be the case if a class implements two
                // interfaces that both declare the same method
                return 0;
            }
        }
    }
}
