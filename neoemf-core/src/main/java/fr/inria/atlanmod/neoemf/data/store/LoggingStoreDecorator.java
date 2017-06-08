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

package fr.inria.atlanmod.neoemf.data.store;

import fr.inria.atlanmod.neoemf.core.Id;
import fr.inria.atlanmod.neoemf.data.Backend;
import fr.inria.atlanmod.neoemf.data.structure.ClassDescriptor;
import fr.inria.atlanmod.neoemf.data.structure.ContainerDescriptor;
import fr.inria.atlanmod.neoemf.data.structure.FeatureKey;
import fr.inria.atlanmod.neoemf.data.structure.ManyFeatureKey;
import fr.inria.atlanmod.neoemf.util.log.Level;
import fr.inria.atlanmod.neoemf.util.log.Log;
import fr.inria.atlanmod.neoemf.util.log.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static java.util.Objects.nonNull;

/**
 * A {@link Store} wrapper that logs every call to its methods in the {@link Log}.
 */
@ParametersAreNonnullByDefault
@SuppressWarnings("MethodDoesntCallSuperMethod")
public class LoggingStoreDecorator extends AbstractStoreDecorator {

    /**
     * The {@link Logger} for the associated {@link Backend}.
     */
    private final Logger log;

    /**
     * The default {@link Level} for the {@link #log}.
     */
    private final Level level;

    /**
     * Constructs a new {@code LoggingStoreDecorator}.
     *
     * @param store the inner store
     */
    @SuppressWarnings("unused") // Called dynamically
    public LoggingStoreDecorator(Store store) {
        this(store, Level.DEBUG);
    }

    /**
     * Constructs a new {@code LoggingStoreDecorator} with the given logging {@code level}.
     *
     * @param store the underlying store
     * @param level the logging level to use
     */
    @SuppressWarnings("unused") // Called dynamically
    public LoggingStoreDecorator(Store store, Level level) {
        super(store);
        this.level = level;

        this.log = Log.customLogger(backend().getClass().getSimpleName() + "@" + backend().hashCode());
    }

    @Nonnull
    @Override
    public Optional<ContainerDescriptor> containerOf(Id id) {
        return callAndReturn(super::containerOf, id);
    }

    @Override
    public void containerFor(Id id, ContainerDescriptor container) {
        call(super::containerFor, id, container);
    }

    @Override
    public void unsetContainer(Id id) {
        call(super::unsetContainer, id);
    }

    @Override
    public boolean hasContainer(Id id) {
        return callAndReturn(super::hasContainer, id);
    }

    @Nonnull
    @Override
    public Optional<ClassDescriptor> metaclassOf(Id id) {
        return callAndReturn(super::metaclassOf, id);
    }

    @Override
    public void metaclassFor(Id id, ClassDescriptor metaclass) {
        call(super::metaclassFor, id, metaclass);
    }

    @Override
    public boolean hasMetaclass(Id id) {
        return callAndReturn(super::hasMetaclass, id);
    }

    @Nonnull
    @Override
    public <V> Optional<V> valueOf(FeatureKey key) {
        return callAndReturn(super::valueOf, key);
    }

    @Nonnull
    @Override
    public <V> Optional<V> valueFor(FeatureKey key, V value) {
        return callAndReturn(super::valueFor, key, value);
    }

    @Override
    public <V> void unsetValue(FeatureKey key) {
        call(super::unsetValue, key);
    }

    @Override
    public <V> boolean hasValue(FeatureKey key) {
        return callAndReturn(super::hasValue, key);
    }

    @Nonnull
    @Override
    public Optional<Id> referenceOf(FeatureKey key) {
        return callAndReturn(super::referenceOf, key);
    }

    @Nonnull
    @Override
    public Optional<Id> referenceFor(FeatureKey key, Id reference) {
        return callAndReturn(super::referenceFor, key, reference);
    }

    @Override
    public void unsetReference(FeatureKey key) {
        call(super::unsetReference, key);
    }

    @Override
    public boolean hasReference(FeatureKey key) {
        return callAndReturn(super::hasReference, key);
    }

    @Nonnull
    @Override
    public <V> Optional<V> valueOf(ManyFeatureKey key) {
        return callAndReturn(super::valueOf, key);
    }

    @Nonnull
    @Override
    public <V> List<V> allValuesOf(FeatureKey key) {
        return callAndReturn(super::allValuesOf, key);
    }

    @Nonnull
    @Override
    public <V> Optional<V> valueFor(ManyFeatureKey key, V value) {
        return callAndReturn(super::valueFor, key, value);
    }

    @Override
    public <V> boolean hasAnyValue(FeatureKey key) {
        return callAndReturn(super::hasAnyValue, key);
    }

    @Override
    public <V> void addValue(ManyFeatureKey key, V value) {
        call(super::addValue, key, value);
    }

    @Nonnegative
    @Override
    public <V> int appendValue(FeatureKey key, V value) {
        return callAndReturn(super::appendValue, key, value);
    }

    @Nonnegative
    @Override
    public <V> int appendAllValues(FeatureKey key, List<V> values) {
        return callAndReturn(super::appendAllValues, key, values);
    }

    @Nonnull
    @Override
    public <V> Optional<V> removeValue(ManyFeatureKey key) {
        return callAndReturn(super::removeValue, key);
    }

    @Override
    public <V> void removeAllValues(FeatureKey key) {
        call(super::removeAllValues, key);
    }

    @Nonnull
    @Override
    public <V> Optional<V> moveValue(ManyFeatureKey source, ManyFeatureKey target) {
        return callAndReturn(super::moveValue, source, target);
    }

    @Override
    public <V> boolean containsValue(FeatureKey key, @Nullable V value) {
        return callAndReturn(super::containsValue, key, value);
    }

    @Nonnull
    @Nonnegative
    @Override
    public <V> Optional<Integer> indexOfValue(FeatureKey key, @Nullable V value) {
        return callAndReturn(super::indexOfValue, key, value);
    }

    @Nonnull
    @Nonnegative
    @Override
    public <V> Optional<Integer> lastIndexOfValue(FeatureKey key, @Nullable V value) {
        return callAndReturn(super::lastIndexOfValue, key, value);
    }

    @Nonnull
    @Nonnegative
    @Override
    public <V> Optional<Integer> sizeOfValue(FeatureKey key) {
        return callAndReturn(super::sizeOfValue, key);
    }

    @Nonnull
    @Override
    public Optional<Id> referenceOf(ManyFeatureKey key) {
        return callAndReturn(super::referenceOf, key);
    }

    @Nonnull
    @Override
    public List<Id> allReferencesOf(FeatureKey key) {
        return callAndReturn(super::allReferencesOf, key);
    }

    @Nonnull
    @Override
    public Optional<Id> referenceFor(ManyFeatureKey key, Id reference) {
        return callAndReturn(super::referenceFor, key, reference);
    }

    @Override
    public boolean hasAnyReference(FeatureKey key) {
        return callAndReturn(super::hasAnyReference, key);
    }

    @Override
    public void addReference(ManyFeatureKey key, Id reference) {
        call(super::addReference, key, reference);
    }

    @Nonnegative
    @Override
    public int appendReference(FeatureKey key, Id reference) {
        return callAndReturn(super::appendReference, key, reference);
    }

    @Nonnegative
    @Override
    public int appendAllReferences(FeatureKey key, List<Id> references) {
        return callAndReturn(super::appendAllReferences, key, references);
    }

    @Nonnull
    @Override
    public Optional<Id> removeReference(ManyFeatureKey key) {
        return callAndReturn(super::removeReference, key);
    }

    @Override
    public void removeAllReferences(FeatureKey key) {
        call(super::removeAllReferences, key);
    }

    @Nonnull
    @Override
    public Optional<Id> moveReference(ManyFeatureKey source, ManyFeatureKey target) {
        return callAndReturn(super::moveReference, source, target);
    }

    @Override
    public boolean containsReference(FeatureKey key, @Nullable Id reference) {
        return callAndReturn(super::containsReference, key, reference);
    }

    @Nonnull
    @Nonnegative
    @Override
    public Optional<Integer> indexOfReference(FeatureKey key, @Nullable Id reference) {
        return callAndReturn(super::indexOfReference, key, reference);
    }

    @Nonnull
    @Nonnegative
    @Override
    public Optional<Integer> lastIndexOfReference(FeatureKey key, @Nullable Id reference) {
        return callAndReturn(super::lastIndexOfReference, key, reference);
    }

    @Nonnull
    @Nonnegative
    @Override
    public Optional<Integer> sizeOfReference(FeatureKey key) {
        return callAndReturn(super::sizeOfReference, key);
    }

    /**
     * Logs the call of a method.
     *
     * @param consumer the method to call
     * @param key      the key used during the call
     */
    private <K> void call(Consumer<K> consumer, K key) {
        try {
            consumer.accept(key);
            logSuccess(key, null, null);
        }
        catch (RuntimeException e) {
            logFailure(key, null, e);
            throw e;
        }
    }

    /**
     * Logs the call of a method.
     *
     * @param consumer the method to call
     * @param key      the key used during the call
     * @param value    the value of the key
     */
    private <K, V> void call(BiConsumer<K, V> consumer, K key, V value) {
        try {
            consumer.accept(key, value);
            logSuccess(key, value, null);
        }
        catch (RuntimeException e) {
            logFailure(key, value, e);
            throw e;
        }
    }

    /**
     * Logs the call of a method and returns the result.
     *
     * @param function the method to call
     * @param key      the key used during the call
     *
     * @return the result of the call
     */
    private <K, R> R callAndReturn(Function<K, R> function, K key) {
        try {
            R result = function.apply(key);
            logSuccess(key, null, result);
            return result;
        }
        catch (RuntimeException e) {
            logFailure(key, null, e);
            throw e;
        }
    }

    /**
     * Logs the call of a method and returns the result.
     *
     * @param function the method to call
     * @param key      the key used during the call
     * @param value    the value of the key
     *
     * @return the result of the call
     */
    private <K, V, R> R callAndReturn(BiFunction<K, V, R> function, K key, @Nullable V value) {
        try {
            R result = function.apply(key, value);
            logSuccess(key, value, result);
            return result;
        }
        catch (RuntimeException e) {
            logFailure(key, value, e);
            throw e;
        }
    }

    /**
     * Logs a successful call of a method.
     *
     * @param key    the key used during the call
     * @param value  the value of the key
     * @param result the result of the call
     */
    private void logSuccess(Object key, @Nullable Object value, @Nullable Object result) {
        log.log(level, "Called {0}() for {1}" + (nonNull(value) ? " with {2}" : "") + (nonNull(result) ? " = {3}" : ""), getCallingMethod(), key, value, result);
    }

    /**
     * Logs a successful call of a method.
     *
     * @param key   the key used during the call
     * @param value the value of the key
     * @param e     the exception thrown during the the call
     */
    private void logFailure(Object key, @Nullable Object value, Throwable e) {
        log.log(level, "Called {0}() for {1}" + (nonNull(value) ? " with {2}" : "") + " but failed with {3}", getCallingMethod(), key, value, e.getClass().getSimpleName());
    }

    /**
     * Returns the name of the calling method.
     *
     * @return the name
     */
    private String getCallingMethod() {
        return Thread.currentThread().getStackTrace()[4].getMethodName();
    }
}
