/*
 * Copyright (c) 2013-2016 Atlanmod INRIA LINA Mines Nantes.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 */

package fr.inria.atlanmod.neoemf.datastore;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class PersistenceBackendFactoryRegistry {

    /**
     * A map containing all registered {@link PersistenceBackendFactory backend factory} identified by a uri scheme.
     */
    private static final Map<String, PersistenceBackendFactory> FACTORIES = new HashMap<>();

    private PersistenceBackendFactoryRegistry() {
    }

    /**
     * Returns all registered factories.
     *
     * @return an immutable {@link Map map} of {@link PersistenceBackendFactory backend factory}
     */
    public static Map<String, PersistenceBackendFactory> getFactories() {
        return Collections.unmodifiableMap(FACTORIES);
    }

    /**
     * Returns a specific {@link PersistenceBackendFactory backend factory} identified by {@code uriScheme}.
     *
     * @param uriScheme the uri scheme identifying the backend factory
     *
     * @return the backend factory
     *
     * @throws NullPointerException if no backend factory is registered for the given {@code uriScheme}
     */
    public static PersistenceBackendFactory getFactoryProvider(String uriScheme) {
        return checkNotNull(FACTORIES.get(uriScheme), "Can not find a factory for the given type %s", uriScheme);
    }

    /**
     * Defines if a {@link PersistenceBackendFactory backend factory} is registered for the given {@code uriScheme}.
     *
     * @param uriScheme the uri scheme identifying the backend factory
     *
     * @return {@code true} if a backend factory is registered for the given {@code uriScheme}
     */
    public static boolean isRegistered(String uriScheme) {
        return FACTORIES.containsKey(uriScheme);
    }

    /**
     * Register a {@link PersistenceBackendFactory backend factory} identified by the given {@code uriScheme}.
     *
     * @param uriScheme the uri scheme identifying the backend factory
     */
    public static void register(String uriScheme, PersistenceBackendFactory factory) {
        FACTORIES.put(uriScheme, factory);
    }

    /**
     * Unregister a {@link PersistenceBackendFactory backend factory} identified by the given {@code uriScheme}.
     *
     * @param uriScheme the uri scheme identifying the backend factory
     */
    public static void unregister(String uriScheme) {
        FACTORIES.remove(uriScheme);
    }

    /**
     * Unregister all backend factories.
     */
    public static void unregisterAll() {
        FACTORIES.clear();
    }
}
