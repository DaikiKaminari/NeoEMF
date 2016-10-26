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

package fr.inria.atlanmod.neoemf.resources.impl;

import fr.inria.atlanmod.neoemf.datastore.PersistenceBackendFactoryRegistry;
import fr.inria.atlanmod.neoemf.resources.PersistentResourceFactory;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

public class PersistentResourceFactoryImpl implements PersistentResourceFactory {

	private static PersistentResourceFactory INSTANCE;

	public static PersistentResourceFactory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PersistentResourceFactoryImpl();
		}
		return INSTANCE;
	}

	protected PersistentResourceFactoryImpl() {
	}

	@Override
	public Resource createResource(URI uri) {
		Resource resource = null;
		if (PersistenceBackendFactoryRegistry.isRegistered(uri.scheme())) {
			resource = new PersistentResourceImpl(uri);
		}
		return resource;
	}
}
