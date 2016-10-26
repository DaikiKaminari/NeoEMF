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

package fr.inria.atlanmod.neoemf.tests;

import fr.inria.atlanmod.neoemf.resources.PersistentResource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AllInstancesTransientTest extends AllInstancesTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        createTransientStores();
        createResourceContent(mapResource);
        createResourceContent(neo4jResource);
        createResourceContent(tinkerResource);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testAllInstancesTransientMapDB() {
        allInstancesTransient(mapResource);
    }

    @Test
    public void testAllInstancesTransientNeo4j() {
        allInstancesTransient(neo4jResource);
    }

    @Test
    public void testAllInstancesTransientTinker() {
        allInstancesTransient(tinkerResource);
    }

    @Test
    public void testAllInstancesStrictTransientMapDB() {
        allInstancesStrictTransient(mapResource);
    }

    @Test
    public void testAllInstancesStrictTransientNeo4j() {
        allInstancesStrictTransient(neo4jResource);
    }

    @Test
    public void testAllInstancesStrictTransientTinker() {
        allInstancesStrictTransient(tinkerResource);
    }

    private void allInstancesTransient(PersistentResource persistentResource) {
        allInstancesPersistentTranscient(persistentResource, false, ABSTRACT_PACK_CONTENT_COUNT, PACK_CONTENT_COUNT);
    }

    private void allInstancesStrictTransient(PersistentResource persistentResource) {
        allInstancesPersistentTranscient(persistentResource, true, ABSTRACT_PACK_CONTENT_STRICT_COUNT, PACK_CONTENT_STRICT_COUNT);
    }
}
