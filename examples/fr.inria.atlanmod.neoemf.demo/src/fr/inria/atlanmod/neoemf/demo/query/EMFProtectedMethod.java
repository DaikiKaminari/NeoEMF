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

package fr.inria.atlanmod.neoemf.demo.query;

import fr.inria.atlanmod.common.log.Log;
import fr.inria.atlanmod.neoemf.data.BackendFactoryRegistry;
import fr.inria.atlanmod.neoemf.data.berkeleydb.BerkeleyDbBackendFactory;
import fr.inria.atlanmod.neoemf.data.berkeleydb.util.BerkeleyDbUri;
import fr.inria.atlanmod.neoemf.data.blueprints.BlueprintsBackendFactory;
import fr.inria.atlanmod.neoemf.data.blueprints.util.BlueprintsUri;
import fr.inria.atlanmod.neoemf.data.hbase.HBaseBackendFactory;
import fr.inria.atlanmod.neoemf.data.hbase.util.HBaseUri;
import fr.inria.atlanmod.neoemf.data.mapdb.MapDbBackendFactory;
import fr.inria.atlanmod.neoemf.data.mapdb.util.MapDbUri;
import fr.inria.atlanmod.neoemf.resource.PersistentResource;
import fr.inria.atlanmod.neoemf.resource.PersistentResourceFactory;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.gmt.modisco.java.BodyDeclaration;
import org.eclipse.gmt.modisco.java.ClassDeclaration;
import org.eclipse.gmt.modisco.java.JavaPackage;
import org.eclipse.gmt.modisco.java.MethodDeclaration;
import org.eclipse.gmt.modisco.java.VisibilityKind;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Computes a query using the EMF generated code on top of existing {@link PersistentResource}s storing models in
 * Blueprints, MapDB, and HBase.
 */
public class EMFProtectedMethod {

    public static void main(String[] args) throws IOException {
        BackendFactoryRegistry.register(BlueprintsUri.SCHEME, BlueprintsBackendFactory.getInstance());
        BackendFactoryRegistry.register(MapDbUri.SCHEME, MapDbBackendFactory.getInstance());
        BackendFactoryRegistry.register(BerkeleyDbUri.SCHEME, BerkeleyDbBackendFactory.getInstance());
        BackendFactoryRegistry.register(HBaseUri.SCHEME, HBaseBackendFactory.getInstance());

        ResourceSet resourceSet = new ResourceSetImpl();

        resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(BlueprintsUri.SCHEME, PersistentResourceFactory.getInstance());
        resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(MapDbUri.SCHEME, PersistentResourceFactory.getInstance());
        resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(BerkeleyDbUri.SCHEME, PersistentResourceFactory.getInstance());
        resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put(HBaseUri.SCHEME, PersistentResourceFactory.getInstance());

        Instant start, end;

        try (PersistentResource resource = (PersistentResource) resourceSet.createResource(BlueprintsUri.builder().fromFile("models/sample.graphdb"))) {
            resource.load(Collections.emptyMap());
            start = Instant.now();
            EList<MethodDeclaration> result = getProtectedMethodDeclarations(resource);
            end = Instant.now();
            Log.info("[ProtectedMethods - GraphDB] Done, found {0} elements in {1} seconds", result.size(), Duration.between(start, end).getSeconds());
        }

        try (PersistentResource resource = (PersistentResource) resourceSet.createResource(MapDbUri.builder().fromFile("models/sample.mapdb"))) {
            resource.load(Collections.emptyMap());
            start = Instant.now();
            EList<MethodDeclaration> result = getProtectedMethodDeclarations(resource);
            end = Instant.now();
            Log.info("[ProtectedMethods - MapDB] Done, found {0} elements in {1} seconds", result.size(), Duration.between(start, end).getSeconds());
        }

        try (PersistentResource resource = (PersistentResource) resourceSet.createResource(BerkeleyDbUri.builder().fromFile("models/sample.berkeleydb"))) {
            resource.load(Collections.emptyMap());
            start = Instant.now();
            EList<MethodDeclaration> result = getProtectedMethodDeclarations(resource);
            end = Instant.now();
            Log.info("[ProtectedMethods - BerkeleyB] Done, found {0} elements in {1} seconds", result.size(), Duration.between(start, end).getSeconds());
        }

//        try (PersistentResource resource = (PersistentResource) resourceSet.createResource(HBaseUri.builder().fromServer("localhost", 2181, "sample.hbase"))) {
//            resource.load(Collections.emptyMap());
//            start = Instant.now();
//            EList<MethodDeclaration> result = getProtectedMethodDeclarations(resource);
//            end = Instant.now();
//            Log.info("[ProtectedMethods - HBase] Done, found {0} elements in {1} seconds", result.size(), Duration.between(start, end).getSeconds());
//        }
    }

    private static EList<MethodDeclaration> getProtectedMethodDeclarations(Resource resource) {
        EList<MethodDeclaration> methodDeclarations = new BasicEList<>();
        List<? extends EObject> allClasses = getAllInstances(resource, JavaPackage.eINSTANCE.getClassDeclaration());
        for (EObject cls : allClasses) {
            for (BodyDeclaration method : ((ClassDeclaration) cls).getBodyDeclarations()) {
                if (!(method instanceof MethodDeclaration)) {
                    continue;
                }
                if (method.getModifier() == null) {
                    continue;
                }
                if (method.getModifier().getVisibility() == VisibilityKind.PROTECTED) {
                    methodDeclarations.add((MethodDeclaration) method);
                }
            }
        }
        return methodDeclarations;
    }

    /**
     * Compute allInstances by traversing the entire resource.
     */
    private static List<EObject> getAllInstances(Resource resource, EClass eClass) {
        List<EObject> result = new ArrayList<>();
        Iterable<EObject> allContents = resource::getAllContents;
        for (EObject o : allContents) {
            if (eClass.isInstance(o)) {
                result.add(o);
            }
        }
        return result;
    }
}
