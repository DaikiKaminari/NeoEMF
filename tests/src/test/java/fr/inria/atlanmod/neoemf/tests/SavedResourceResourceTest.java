/*
 * Copyright (c) 2013 Atlanmod INRIA LINA Mines Nantes.
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
import fr.inria.atlanmod.neoemf.tests.models.mapSample.SampleModel;
import fr.inria.atlanmod.neoemf.tests.models.mapSample.SampleModelContentObject;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

public class SavedResourceResourceTest extends AllSavedResourceTest {

    @Test
    public void testEResourceMapDB() {
        checkEResource(mapResource, mapSampleModel, mapSampleContentObject);
    }

    @Test
    public void testEResourceNeo4j() {
        checkEResource(neo4jResource, neo4jSampleModel, neo4jSampleContentObject);
    }

    @Test
    public void testEResourceTinker() {
        checkEResource(tinkerResource, tinkerSampleModel, tinkerSampleContentObject);
    }

    @Test
    public void testGetAllContentsEResourceMapDB() {
        getAllContentsEResource(mapResource);
    }

    @Test
    public void testGetAllContentsEResourceNeo4j() {
        getAllContentsEResource(neo4jResource);
    }

    @Test
    public void testGetAllContentsEResourceTinker() {
        getAllContentsEResource(tinkerResource);
    }

    @Test
    public void testEDirectResourceMapDB() {
        checkEDirectResource(mapResource, mapSampleModel, mapSampleContentObject);
    }

    @Test
    public void testEDirectResourceNeo4j() {
        checkEDirectResource(neo4jResource, neo4jSampleModel, neo4jSampleContentObject);
    }

    @Test
    public void testEDirectResourceTinker() {
        checkEDirectResource(tinkerResource, tinkerSampleModel, tinkerSampleContentObject);
    }

    @Test
    public void testGetAllContentsEDirectResourceMapDB() {
        getAllContentsEDirectResource(mapResource);
    }

    @Test
    public void testGetAllContentsEDirectResourceNeo4j() {
        getAllContentsEDirectResource(neo4jResource);
    }

    @Test
    public void testGetAllContentsEDirectResourceTinker() {
        getAllContentsEDirectResource(tinkerResource);
    }

    private void checkEResource(PersistentResource persistentResource, SampleModel sampleModel, SampleModelContentObject sampleModelContentObject) {
        assertThat("Wrong eResource value", sampleModel.eResource(), sameInstance((Resource) persistentResource));
        assertThat("Wrong eResource value", sampleModelContentObject.eResource(), sameInstance((Resource) persistentResource));
    }

    private void checkEDirectResource(PersistentResource persistentResource, SampleModel sampleModel, SampleModelContentObject sampleModelContentObject) {
        InternalEObject internalMapSampleModel = (InternalEObject) sampleModel;
        assertThat("Wrong eDirectResource value", internalMapSampleModel.eDirectResource(), sameInstance((Resource) persistentResource));

        InternalEObject internalMapSampleContentObject = (InternalEObject) sampleModelContentObject;
        assertThat("Non top level element eDirectResource is not null", internalMapSampleContentObject.eDirectResource(), nullValue());
    }

    private void getAllContentsEDirectResource(PersistentResource persistentResource) {
        Iterator<EObject> it = persistentResource.getAllContents();

        InternalEObject sampleModel = (InternalEObject) it.next();
        assertThat("Wrong eDirectResource value", sampleModel.eDirectResource(), sameInstance((Resource) persistentResource));

        InternalEObject sampleContentObject = (InternalEObject) it.next();
        assertThat("Non top level element eDirectResource is not null", sampleContentObject.eDirectResource(), nullValue());
    }

}
