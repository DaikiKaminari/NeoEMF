/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data.store.listener;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A success {@link AbstractCallReport}.
 *
 * @param <K> the type of the key used during the call
 * @param <V> the type of the value used during the call
 * @param <R> the type of the result of the call
 */
@ParametersAreNonnullByDefault
public class SuccessCallReport<K, V, R> extends AbstractCallReport<K, V> {

    /**
     * The result of the call.
     */
    @Nullable
    private final R result;

    /**
     * Constructs a new succeeded call information.
     *
     * @param backend information about the backend related to this call
     * @param method  the name of the called method
     * @param key     the key used during the call
     * @param value   the value used during the call
     * @param result  the result of the call
     */
    public SuccessCallReport(BackendReport backend, String method, @Nullable K key, @Nullable V value, @Nullable R result) {
        super(backend, method, key, value);
        this.result = result;
    }

    /**
     * Returns the result of the call.
     *
     * @return the result of the call
     */
    @Nullable
    public R result() {
        return result;
    }
}
