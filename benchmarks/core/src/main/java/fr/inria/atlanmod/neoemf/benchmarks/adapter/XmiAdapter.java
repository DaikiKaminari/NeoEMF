/*
 * Copyright (c) 2013-2018 Atlanmod, Inria, LS2N, and IMT Nantes.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.benchmarks.adapter;

import fr.inria.atlanmod.neoemf.config.ImmutableConfig;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An {@link Adapter} on top a an original XMI {@link Resource}.
 */
@ParametersAreNonnullByDefault
public class XmiAdapter extends AbstractAdapter {

    /**
     * Constructs a new {@code XmiAdapter}.
     */
    @SuppressWarnings("unused") // Called dynamically
    public XmiAdapter() {
        super("xmi", "xmi", org.eclipse.gmt.modisco.java.emf.impl.JavaPackageImpl.class);
    }

    @Nonnull
    @Override
    public Resource createResource(File file, ResourceSet resourceSet) {
        URI targetUri = URI.createFileURI(file.getAbsolutePath());

        return resourceSet.createResource(targetUri);
    }

    @Nonnull
    @Override
    public Resource load(File file, ImmutableConfig config) throws IOException {
        initAndGetEPackage();

        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("zxmi", new XMIResourceFactoryImpl());

        Resource resource = createResource(file, resourceSet);
        resource.load(getOptions());

        return resource;
    }

    @Override
    public void unload(Resource resource) {
        if (resource.isLoaded()) {
            resource.unload();
        }
    }
}
