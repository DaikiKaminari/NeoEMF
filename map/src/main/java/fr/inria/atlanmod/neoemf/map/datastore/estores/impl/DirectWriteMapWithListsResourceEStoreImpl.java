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

package fr.inria.atlanmod.neoemf.map.datastore.estores.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.inria.atlanmod.neoemf.core.Id;
import fr.inria.atlanmod.neoemf.core.PersistentEObject;
import fr.inria.atlanmod.neoemf.core.impl.PersistentEObjectAdapter;
import fr.inria.atlanmod.neoemf.logger.NeoLogger;
import fr.inria.atlanmod.neoemf.map.datastore.MapPersistenceBackend;

import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;



public class DirectWriteMapWithListsResourceEStoreImpl extends DirectWriteMapResourceEStoreImpl {

	private static final int DEFAULT_CACHE_SIZE = 100;

	private final LoadingCache<FeatureKey, Object> mapCache;

	public DirectWriteMapWithListsResourceEStoreImpl(Resource.Internal resource, MapPersistenceBackend persistenceBackend) {
		super(resource, persistenceBackend);
		this.mapCache = CacheBuilder.newBuilder().maximumSize(DEFAULT_CACHE_SIZE).softValues().build(new Tuple2CacheLoader());
	}

	@Override
	@SuppressWarnings("unchecked") // Unchecked cast: 'java.lang.Object' to 'java.util.List<java.lang.Object>'
	protected Object getWithAttribute(PersistentEObject object, EAttribute eAttribute, int index) {
		Object returnValue;
		Object value = getFromMap(object, eAttribute);
		if (!eAttribute.isMany()) {
			returnValue = parseProperty(eAttribute, value);
		} else {
			List<Object> list = (List<Object>) value;
			returnValue = parseProperty(eAttribute, list.get(index));
		}
		return returnValue;
	}

	@Override
	@SuppressWarnings("unchecked") // Unchecked cast: 'java.lang.Object' to 'java.util.List<java.lang.Object>'
	protected Object getWithReference(PersistentEObject object, EReference eReference, int index) {
		Object returnValue;
		Object value = getFromMap(object, eReference);
		if (!eReference.isMany()) {
			returnValue = eObject((Id) value);
		} else {
			List<Object> list = (List<Object>) value;
			returnValue = eObject((Id) list.get(index));
		}
		return returnValue;
	}


	@Override
	@SuppressWarnings("unchecked") // Unchecked cast: 'java.lang.Object' to 'java.util.List<java.lang.Object>'
	protected Object setWithAttribute(PersistentEObject object, EAttribute eAttribute, int index, Object value) {
		Object returnValue;
		if (!eAttribute.isMany()) {
			Object oldValue = persistenceBackend.storeValue(new FeatureKey(object.id(), eAttribute.getName()), serializeToProperty(eAttribute, value));
			returnValue = parseProperty(eAttribute, oldValue);
		} else {

			List<Object> list = (List<Object>) getFromMap(object, eAttribute);
			Object oldValue = list.get(index);
			list.set(index, serializeToProperty(eAttribute, value));
			persistenceBackend.storeValue(new FeatureKey(object.id(), eAttribute.getName()), list.toArray());
			returnValue = parseProperty(eAttribute, oldValue);
		}
		return returnValue;
	}

	@Override
	@SuppressWarnings("unchecked") // Unchecked cast: 'java.lang.Object' to 'java.util.List<java.lang.Object>'
	protected Object setWithReference(PersistentEObject object, EReference eReference, int index, PersistentEObject value) {
		Object returnValue;
		updateContainment(object, eReference, value);
		updateInstanceOf(value);
		if (!eReference.isMany()) {
			Object oldId = persistenceBackend.storeValue(new FeatureKey(object.id(), eReference.getName()), value.id());
			returnValue = oldId != null ? eObject((Id) oldId) : null;
		} else {
			List<Object> list = (List<Object>) getFromMap(object, eReference);
			Object oldId = list.get(index);
			list.set(index, value.id());
			persistenceBackend.storeValue(new FeatureKey(object.id(), eReference.getName()), list.toArray());
			returnValue = oldId != null ? eObject((Id) oldId) : null;
		}
		return returnValue;
	}


	@Override
	@SuppressWarnings("unchecked") // Unchecked cast: 'java.lang.Object' to 'java.util.List<java.lang.Object>'
	protected void addWithAttribute(PersistentEObject object, EAttribute eAttribute, int index, Object value) {
		List<Object> list = (List<Object>) getFromMap(object, eAttribute);
		list.add(index, serializeToProperty(eAttribute, value));
		persistenceBackend.storeValue(new FeatureKey(object.id(), eAttribute.getName()), list.toArray());
	}

	@Override
	@SuppressWarnings("unchecked") // Unchecked cast: 'java.lang.Object' to 'java.util.List<java.lang.Object>'
	protected void addWithReference(PersistentEObject object, EReference eReference, int index, PersistentEObject referencedObject) {
		updateContainment(object, eReference, referencedObject);
		updateInstanceOf(referencedObject);
		List<Object> list = (List<Object>) getFromMap(object, eReference);
		list.add(index, referencedObject.id());
		persistenceBackend.storeValue(new FeatureKey(object.id(), eReference.getName()), list.toArray());
	}

	@Override
	@SuppressWarnings("unchecked") // Unchecked cast: 'java.lang.Object' to 'java.util.List<java.lang.Object>'
	protected Object removeWithAttribute(PersistentEObject object, EAttribute eAttribute, int index) {
		List<Object> list = (List<Object>) getFromMap(object, eAttribute);
		Object oldValue = list.get(index);
		list.remove(index);
		persistenceBackend.storeValue(new FeatureKey(object.id(), eAttribute.getName()), list.toArray());
		return parseProperty(eAttribute, oldValue);
	}

	@Override
	@SuppressWarnings("unchecked") // Unchecked cast: 'java.lang.Object' to 'java.util.List<java.lang.Object>'
	protected Object removeWithReference(PersistentEObject object, EReference eReference, int index) {
		List<Object> list = (List<Object>) getFromMap(object, eReference);
		Object oldId = list.get(index);
		list.remove(index);
		persistenceBackend.storeValue(new FeatureKey(object.id(), eReference.getName()), list.toArray());
		return eObject((Id) oldId);
	}

	@Override
	@SuppressWarnings("unchecked") // Unchecked cast: 'java.lang.Object' to 'java.util.List<java.lang.Object>'
	public int size(InternalEObject object, EStructuralFeature feature) {
		PersistentEObject persistentEObject = PersistentEObjectAdapter.getAdapter(object);
		List<Object> list = (List<Object>) getFromMap(persistentEObject, feature);
		return list != null ? list.size() : 0; 
	}

	@Override
	@SuppressWarnings("unchecked") // Unchecked cast: 'java.lang.Object' to 'java.util.List<java.lang.Object>'
	public int indexOf(InternalEObject object, EStructuralFeature feature, Object value) {
		int returnValue;
		PersistentEObject persistentEObject = PersistentEObjectAdapter.getAdapter(object);
		List<Object> list = (List<Object>) getFromMap(persistentEObject, feature);
		if (list== null) {
			returnValue = -1;
		} else if (feature instanceof EAttribute) {
			returnValue = list.indexOf(serializeToProperty((EAttribute) feature, value));
		} else {
			PersistentEObject childEObject = PersistentEObjectAdapter.getAdapter(value);
			returnValue = list.indexOf(childEObject.id());
		}
		return returnValue;
	}


	@Override
	@SuppressWarnings("unchecked") // Unchecked cast: 'java.lang.Object' to 'java.util.List<java.lang.Object>'
	public int lastIndexOf(InternalEObject object, EStructuralFeature feature, Object value) {
		int returnValue;
		PersistentEObject persistentEObject = PersistentEObjectAdapter.getAdapter(object);
		List<Object> list = (List<Object>) getFromMap(persistentEObject, feature);
		if (list == null) {
			returnValue = -1;
		} else if (feature instanceof EAttribute) {
			returnValue = list.lastIndexOf(serializeToProperty((EAttribute) feature, value));
		} else {
			PersistentEObject childEObject = PersistentEObjectAdapter.getAdapter(value);
			returnValue = list.lastIndexOf(childEObject.id());
		}
		return returnValue;
	}

	@Override
	public void clear(InternalEObject object, EStructuralFeature feature) {
		PersistentEObject persistentEObject = PersistentEObjectAdapter.getAdapter(object);
		persistenceBackend.storeValue(new FeatureKey(persistentEObject.id(), feature.getName()), new ArrayList<>());
	}

	@Override
	protected Object getFromMap(PersistentEObject object, EStructuralFeature feature) {
		Object returnValue = null;
		if (!feature.isMany()) {
			returnValue = persistenceBackend.valueOf(new FeatureKey(object.id(), feature.getName()));
		} else {
			try {
				returnValue = mapCache.get(new FeatureKey(object.id(), feature.getName()));
			} catch (ExecutionException e) {
				NeoLogger.warn(e.getCause());
			}
		}
		return returnValue;
	}
	
	private class Tuple2CacheLoader extends CacheLoader<FeatureKey, Object> {

		private static final int ARRAY_SIZE_OFFSET = 10;

		@Override
        public Object load(FeatureKey key) throws Exception {
            Object returnValue;
            Object value = persistenceBackend.valueOf(key);
            if (value == null) {
                returnValue = new ArrayList<>();
            } else if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                List<Object> list = new ArrayList<>(array.length + ARRAY_SIZE_OFFSET);
                CollectionUtils.addAll(list, array);
                returnValue = list;
            } else {
                returnValue = value;
            }
            return returnValue;
        }
	}
}
