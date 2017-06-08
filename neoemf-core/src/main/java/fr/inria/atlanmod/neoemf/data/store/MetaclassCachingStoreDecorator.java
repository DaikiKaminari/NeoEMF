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
import fr.inria.atlanmod.neoemf.data.structure.ClassDescriptor;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A {@link Store} wrapper that caches {@link ClassDescriptor}s.
 */
@ParametersAreNonnullByDefault
public class MetaclassCachingStoreDecorator extends AbstractCachingStoreDecorator<Id, Optional<ClassDescriptor>> {

    /**
     * Constructs a new {@code MetaclassCachingStoreDecorator} on the given {@code store}.
     *
     * @param store the inner store
     */
    @SuppressWarnings("unused") // Called dynamically
    protected MetaclassCachingStoreDecorator(Store store) {
        super(store);
    }

    @Nonnull
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Optional<ClassDescriptor> metaclassOf(Id id) {
        return cache.get(id, super::metaclassOf);
    }

    @Override
    public void metaclassFor(Id id, ClassDescriptor metaclass) {
        cache.put(id, Optional.of(metaclass));
        super.metaclassFor(id, metaclass);
    }
}
