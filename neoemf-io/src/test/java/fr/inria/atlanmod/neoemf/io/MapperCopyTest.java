/*
 * Copyright (c) 2013-2018 Atlanmod, Inria, LS2N, and IMT Nantes.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.io;

import fr.inria.atlanmod.commons.AbstractFileBasedTest;
import fr.inria.atlanmod.commons.log.Log;
import fr.inria.atlanmod.neoemf.bind.Bindings;
import fr.inria.atlanmod.neoemf.data.Backend;
import fr.inria.atlanmod.neoemf.data.BackendFactoryRegistry;
import fr.inria.atlanmod.neoemf.data.im.DefaultInMemoryBackend;
import fr.inria.atlanmod.neoemf.io.util.ResourceManager;
import fr.inria.atlanmod.neoemf.resource.PersistentResource;
import fr.inria.atlanmod.neoemf.resource.PersistentResourceFactory;
import fr.inria.atlanmod.neoemf.util.ModelComparisonUtils;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A test-case about the copy from a {@link Backend} to another.
 */
@ParametersAreNonnullByDefault
class MapperCopyTest extends AbstractFileBasedTest {

    @BeforeAll
    static void registerPackages() {
        ResourceManager.registerAllPackages();
    }

    /**
     * Creates a {@link PersistentResource} on the given {@code backend}.
     *
     * @param file    the file path of the resource
     * @param backend the backend of the resource
     *
     * @return a new {@link PersistentResource}
     */
    @Nonnull
    private static PersistentResource createMockResource(File file, Backend backend) throws IOException {
        BackendFactoryRegistry.getInstance().register(Bindings.schemeOf(MockBackendFactory.MockUriFactory.class), new MockBackendFactory(backend));

        PersistentResource resource = PersistentResourceFactory.getInstance().createResource(new MockBackendFactory.MockUriFactory().createLocalUri(file));
        resource.save(new MockBackendFactory.MockConfig());
        return resource;
    }

    /**
     * Checks the copy from a {@link Backend} to another.
     */
    @Tag("slow")
    @Test
    void testDirectCopy() throws IOException {
        final File sourceFile = currentTempFile();
        final File targetFile = Paths.get(currentTempFile() + "-copy").toFile();

        URI expectedUri = ResourceManager.xmiStandard();

        try (Backend sourceBackend = new DefaultInMemoryBackend(); InputStream in = new URL(expectedUri.toString()).openStream()) {
            Migrator.fromXmi(in).toMapper(sourceBackend).migrate();

            Log.info("Copying backends...");

            try (Backend targetBackend = new DefaultInMemoryBackend()) {
                Migrator.fromMapper(sourceBackend).toMapper(targetBackend).migrate();

                EObject actual = createMockResource(targetFile, targetBackend).getContents().get(0);
                EObject expected = createMockResource(sourceFile, sourceBackend).getContents().get(0);

                // Comparing back-ends
                ModelComparisonUtils.assertEObjectAreEqual(actual, expected);

                // Comparing with EMF
                expected = ResourceManager.load(expectedUri);
                ModelComparisonUtils.assertEObjectAreEqual(actual, expected);
            }
        }
    }
}
