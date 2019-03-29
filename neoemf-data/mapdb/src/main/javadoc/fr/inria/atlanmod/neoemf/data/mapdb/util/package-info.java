/*
 * Copyright (c) 2013 Atlanmod.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

/**
 * Provides utility classes related to MapDB.
 * <p>
 * This package defines the {@link fr.inria.atlanmod.neoemf.data.mapdb.util.MapDbURI} class, that extends {@link
 * fr.inria.atlanmod.neoemf.util.URIBuilder} to create MapDB specific URIs. {@link
 * fr.inria.atlanmod.neoemf.data.mapdb.util.MapDbURI}s are convenience wrappers of EMF URIs that set a dedicated
 * protocol that is parsed by NeoEMF to create the appropriate database.
 * <p>
 * {@link fr.inria.atlanmod.neoemf.data.mapdb.util.MapDbURI}s are created using the following code:
 * <pre>{@code
 * URI mapDBURI = MapDBURI.createFileURI(new File("model_path"));
 *
 * // The created URI can be used as a regular EMF URI to create a NeoEMF resource
 * Resource neoEMFResource = resourceSet.createResource(hbaseURI);
 *
 * // And accessed as a regular EMF resource
 * neoEMFResource.getContents() [...]
 * }</pre>
 */

package fr.inria.atlanmod.neoemf.data.mapdb.util;