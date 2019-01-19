/*
 * Copyright (c) 2013-2018 Atlanmod, Inria, LS2N, and IMT Nantes.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data.store;

import org.atlanmod.commons.cache.Cache;
import org.atlanmod.commons.cache.CacheBuilder;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An abstract {@link Store} that provides a pre-loaded {@link org.atlanmod.commons.cache.Cache}.
 *
 * @param <K> the type of keys maintained by the cache
 * @param <V> the type of cached values
 */
@ParametersAreNonnullByDefault
public abstract class AbstractCachingStore<K, V> extends AbstractStore {

    /**
     * In-memory cache that holds loaded values, identified by their key.
     */
    protected final Cache<K, V> cache = CacheBuilder.builder()
            .softValues()
            .build();

    /**
     * Constructs a new {@code AbstractCachingStore}.
     */
    protected AbstractCachingStore() {
        super(100);
    }

    @Override
    public void close() {
        cache.invalidateAll();
        cache.cleanUp();

        super.close();
    }
}
