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

package fr.inria.atlanmod.neoemf.data.mapper;

import fr.inria.atlanmod.neoemf.data.structure.ManyFeatureKey;
import fr.inria.atlanmod.neoemf.data.structure.SingleFeatureKey;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static fr.inria.atlanmod.common.Preconditions.checkNotNull;
import static java.util.Objects.nonNull;

/**
 * A {@link ManyValueMapper} that provides a default behavior to represent the "multi-valued" characteristic as
 * {@link SortedMap}s. The implementation used is specified by the {@link #getOrCreateMap(SingleFeatureKey)} method.
 */
@ParametersAreNonnullByDefault
public interface ManyValueWithMaps extends ManyValueMapper {

    @Nonnull
    @Override
    default <V> Optional<V> valueOf(ManyFeatureKey key) {
        checkNotNull(key);

        return this.<SortedMap<Integer, V>>valueOf(key.withoutPosition())
                .filter(m -> key.position() < (m.isEmpty() ? 0 : m.lastKey()) + 1)
                .map(values -> values.get(key.position()));
    }

    @Nonnull
    @Override
    default <V> List<V> allValuesOf(SingleFeatureKey key) {
        return this.<SortedMap<Integer, V>>valueOf(key)
                .map(m -> IntStream.range(0, m.isEmpty() ? 0 : m.lastKey() + 1)
                        .mapToObj(m::get)
                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    @Nonnull
    @Override
    default <V> Optional<V> valueFor(ManyFeatureKey key, V value) {
        checkNotNull(key);
        checkNotNull(value);

        SortedMap<Integer, V> values = this.<SortedMap<Integer, V>>valueOf(key.withoutPosition())
                .<NoSuchElementException>orElseThrow(NoSuchElementException::new);

        Optional<V> previousValue = Optional.of(values.put(key.position(), value));

        valueFor(key.withoutPosition(), values);

        return previousValue;
    }

    @Override
    default <V> void addValue(ManyFeatureKey key, V value) {
        checkNotNull(key);
        checkNotNull(value);

        SortedMap<Integer, V> values = this.<SortedMap<Integer, V>>valueOf(key.withoutPosition())
                .orElseGet(() -> getOrCreateMap(key.withoutPosition()));

        int size = values.isEmpty() ? 0 : values.lastKey() + 1;

        if (key.position() < size && nonNull(values.get(key.position()))) {
            for (int i = size; i > key.position(); i--) {
                Optional<V> movingValue = Optional.ofNullable(values.get(i - 1));
                if (movingValue.isPresent()) {
                    values.put(i, movingValue.get());
                }
            }
        }

        values.put(key.position(), value);

        valueFor(key.withoutPosition(), values);
    }

    @Nonnull
    @Override
    default <V> Optional<V> removeValue(ManyFeatureKey key) {
        checkNotNull(key);

        Optional<SortedMap<Integer, V>> optionalValues = valueOf(key.withoutPosition());

        if (!optionalValues.isPresent()) {
            return Optional.empty();
        }

        SortedMap<Integer, V> values = optionalValues.get();

        int size = values.isEmpty() ? 0 : values.lastKey() + 1;

        Optional<V> previousValue = Optional.empty();

        if (key.position() < size) {
            previousValue = Optional.of(values.get(key.position()));

            for (int i = key.position(); i < size - 1; i++) {
                Optional<V> movingValue = Optional.ofNullable(values.get(i + 1));
                if (movingValue.isPresent()) {
                    values.put(i, movingValue.get());
                }
            }

            values.remove(size - 1);

            if (values.isEmpty()) {
                removeAllValues(key.withoutPosition());
            }
            else {
                valueFor(key.withoutPosition(), values);
            }
        }

        return previousValue;
    }

    @Nonnull
    @Nonnegative
    @Override
    default <V> Optional<Integer> sizeOfValue(SingleFeatureKey key) {
        return this.<SortedMap<Integer, V>>valueOf(key)
                .filter(m -> !m.isEmpty())
                .map(m -> m.lastKey() + 1)
                .filter(s -> s != 0);
    }

    /**
     * Gets or creates a new {@link Map} to store the multi-valued features identified by the {@code key}.
     *
     * @param key the key identifying the multi-valued attribute
     * @param <V> the type of elements in this list
     *
     * @return a new {@link Map}
     */
    default <V> SortedMap<Integer, V> getOrCreateMap(SingleFeatureKey key) {
        return new TreeMap<>();
    }
}