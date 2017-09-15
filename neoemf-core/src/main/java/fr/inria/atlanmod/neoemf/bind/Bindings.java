/*
 * Copyright (c) 2013-2017 Atlanmod INRIA LINA Mines Nantes.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 */

package fr.inria.atlanmod.neoemf.bind;

import fr.inria.atlanmod.commons.annotation.Builder;
import fr.inria.atlanmod.commons.annotation.Singleton;
import fr.inria.atlanmod.commons.annotation.Static;
import fr.inria.atlanmod.commons.cache.Cache;
import fr.inria.atlanmod.commons.cache.CacheBuilder;
import fr.inria.atlanmod.commons.concurrent.MoreExecutors;
import fr.inria.atlanmod.neoemf.bind.annotation.FactoryBinding;
import fr.inria.atlanmod.neoemf.bind.annotation.FactoryName;
import fr.inria.atlanmod.neoemf.data.BackendFactory;
import fr.inria.atlanmod.neoemf.util.UriBuilder;

import org.eclipse.emf.common.util.URI;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static fr.inria.atlanmod.commons.Preconditions.checkNotNull;
import static java.util.Objects.nonNull;

/**
 * A static utility class for binding and reflection.
 */
@Static
@ParametersAreNonnullByDefault
public final class Bindings {

    /**
     * The concurrent pool.
     */
    @Nonnull
    private static final ExecutorService POOL = MoreExecutors.newFixedThreadPool();

    /**
     * A set that holds the {@link URL} of the classpath to explore.
     */
    @Nonnull
    private static final Set<URL> URLS = new HashSet<>();

    /**
     * A cache that holds the result of recent queries on types.
     */
    @Nonnull
    private static final Cache<Class<? extends Annotation>, Set<Class<?>>> ANNOTATED_TYPES = CacheBuilder.builder()
            .softValues()
            .build(Bindings::typesAnnotatedWith);

    static {
        // Add the default URLs for scanning
        addUrls(ClasspathHelper.forJavaClassPath());
        addUrls(ClasspathHelper.forManifest());
    }

    /**
     * This class should not be instantiated.
     *
     * @throws IllegalStateException every time
     */
    private Bindings() {
        throw new IllegalStateException("This class should not be instantiated");
    }

    /**
     * Adds {@code urls} to be scanned for binding. Theses {@link URL}s will be used to retrieves types, fields or any
     * kind of reflective element at runtime.
     * <p>
     * All {@link URL}s are filtered to keep only those that can be related to NeoEMF. The {@link FactoryBinding}
     * annotation is used to determine this relation.
     *
     * @param urls the {@link URL}s to add for scanning
     *
     * @throws NullPointerException if {@code urls} is {@code null}
     */
    public static void addUrls(Collection<URL> urls) {
        checkNotNull(urls);

        ConfigurationBuilder baseConfig = new ConfigurationBuilder()
                .setExecutorService(POOL)
                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner());

        // Filter URLs, and remove any that cannot be related to NeoEMF (Java, EMF,...)
        urls.stream().filter(url -> {
            try {
                Set<Class<?>> relatedClasses = new Reflections(baseConfig.setUrls(url))
                        .getTypesAnnotatedWith(FactoryBinding.class);

                return !relatedClasses.isEmpty();
            }
            catch (RuntimeException e) {
                return false;
            }
        }).collect(Collectors.toCollection(() -> URLS));

        // Refresh the cache: annotations stay the same, but the result may have been changed
        ANNOTATED_TYPES.invalidateAll();
    }

    // region Reflection

    /**
     * Retrieves all types annotated with the specified {@code annotation}.
     *
     * @param annotation the expected annotation
     *
     * @return a set of annotated instances
     *
     * @see Reflections#getTypesAnnotatedWith(Class)
     */
    @Nonnull
    public static Set<Class<?>> typesAnnotatedWith(Class<? extends Annotation> annotation) {
        return new Reflections(new ConfigurationBuilder()
                .setExecutorService(POOL)
                .setUrls(URLS)
                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()))
                .getTypesAnnotatedWith(annotation);
    }

    /**
     * Retrieves all types annotated with the specified {@code annotation}, which are also assignable from the given
     * {@code type}.
     *
     * @param annotation the expected annotation
     * @param type       the type of the expected classes
     * @param <T>        the type of the instances to look for
     *
     * @return a set of annotated instances of {@code type}
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T> Set<Class<? extends T>> typesAnnotatedWith(Class<? extends Annotation> annotation, Class<? extends T> type) {
        return ANNOTATED_TYPES.get(annotation).stream()
                .filter(type::isAssignableFrom)
                .map(c -> (Class<? extends T>) c)
                .collect(Collectors.toSet());
    }

    /**
     * Gets or creates a instance of the given {@code type} by using the static method named {@code name}.
     * <p>
     * If the {@code type} is annotated by {@link Singleton} or by {@link Builder}, then the static method identified by
     * the value of the annotation is used to get the instance. Otherwise, the default constructor is used.
     *
     * @param type the class to look for
     * @param <T>  the type of the instance
     *
     * @return the single instance if the {@code type} is a singleton, or a new instance
     *
     * @throws BindingException if an error occurs during the instantiation
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> type) {
        Optional<String> methodName;

        if (type.isAnnotationPresent(Singleton.class)) {
            methodName = Optional.of(type.getAnnotation(Singleton.class).value());
        }
        else if (type.isAnnotationPresent(Builder.class)) {
            methodName = Optional.of(type.getAnnotation(Builder.class).value());
        }
        else {
            methodName = Optional.empty();
        }

        try {
            return methodName.isPresent()
                    ? (T) type.getMethod(methodName.get()).invoke(null)
                    : type.newInstance();
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new BindingException(e);
        }
    }

    // endregion

    /**
     * Retrieves the {@link URI} scheme for the speficied {@code type}.
     * <p>
     * The {@code type} must be a sub-class of {@link BackendFactory}, or must be annotated with {@link
     * FactoryBinding}.
     *
     * @param type the type of the factory
     *
     * @return the {@link URI} scheme
     *
     * @throws BindingException if no scheme is defined for this {@code type}
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static String schemeOf(Class<?> type) {
        Class<? extends BackendFactory> factoryType = null;

        if (BackendFactory.class.isAssignableFrom(type)) {
            factoryType = (Class<? extends BackendFactory>) type;
        }
        else if (type.isAnnotationPresent(FactoryBinding.class)) {
            factoryType = type.getAnnotation(FactoryBinding.class).value();
        }

        if (nonNull(factoryType)) {
            return UriBuilder.PREFIX + nameOf(factoryType).toLowerCase();
        }

        throw new BindingException(
                String.format("%s is not annotated with %s: Unable to retrieve the associated scheme", type.getName(), FactoryBinding.class.getSimpleName()));
    }

    /**
     * Retrieves the name for the speficied {@code factoryType}.
     * <p>
     * The name of a {@link BackendFactory} must be annotated with {@link FactoryName}.
     *
     * @param factoryType the type of the factory
     *
     * @return the name
     *
     * @throws BindingException if no name is defined for this {@code factoryType}
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    public static String nameOf(Class<? extends BackendFactory> factoryType) {
        Set<Field> boundFields = ReflectionUtils.getFields(factoryType,
                ReflectionUtils.withAnnotation(FactoryName.class),
                ReflectionUtils.withType(String.class),
                ReflectionUtils.withModifier(Modifier.PUBLIC));

        if (boundFields.size() != 1) {
            throw new BindingException(
                    String.format("%s must have only one field annotated with %s", factoryType.getName(), FactoryName.class.getName()));
        }

        Optional<Field> field = boundFields.stream()
                .filter(f -> factoryType.isAssignableFrom(f.getDeclaringClass()))
                .findAny();

        if (field.isPresent()) {
            try {
                return String.class.cast(field.get().get(factoryType));
            }
            catch (ClassCastException | IllegalAccessException e) {
                throw new BindingException(e);
            }
        }

        throw new BindingException(
                String.format("Unable to find the name of the factory %s", factoryType.getName()));
    }

    /**
     * Retrieves the instance of the {@code type} that is bound to a {@link fr.inria.atlanmod.neoemf.data.BackendFactory}
     * with the given {@code value}, by using the speficied {@code valueMapping}.
     * <p>
     * The {@code type} <b>must</b> be annotated with {@link FactoryBinding}.
     *
     * @param type         the type of the instance to look for
     * @param value        the expected value
     * @param valueMapping the function used to retrieve the current value of a factory
     * @param <T>          the type of the instance
     *
     * @return a new instance of the {@code type}
     *
     * @throws BindingException if no instance of {@code type} is found for the given {@code value} by using the given
     *                          {@code valueMapping}, or if an error occurs during the instantiation
     */
    @Nonnull
    public static <T> T findBy(Class<? extends T> type, String value, Function<Class<? extends BackendFactory>, String> valueMapping) {
        for (Class<? extends T> t : typesAnnotatedWith(FactoryBinding.class, type)) {
            if (Objects.equals(value, valueMapping.apply(t.getAnnotation(FactoryBinding.class).value()))) {
                return newInstance(t);
            }
        }

        throw new BindingException(
                String.format("Unable to find a %s instance for value \"%s\"", type.getSimpleName(), value));
    }
}
