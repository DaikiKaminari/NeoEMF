/*
 * Copyright (c) 2013-2018 Atlanmod, Inria, LS2N, and IMT Nantes.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.io.reader;

import fr.inria.atlanmod.neoemf.core.Id;
import fr.inria.atlanmod.neoemf.data.bean.ClassBean;
import fr.inria.atlanmod.neoemf.data.bean.SingleFeatureBean;
import fr.inria.atlanmod.neoemf.data.mapping.DataMapper;
import fr.inria.atlanmod.neoemf.io.Handler;
import fr.inria.atlanmod.neoemf.io.bean.BasicAttribute;
import fr.inria.atlanmod.neoemf.io.bean.BasicElement;
import fr.inria.atlanmod.neoemf.io.bean.BasicMetaclass;
import fr.inria.atlanmod.neoemf.io.bean.BasicNamespace;
import fr.inria.atlanmod.neoemf.io.bean.BasicReference;
import fr.inria.atlanmod.neoemf.resource.PersistentResource;
import fr.inria.atlanmod.neoemf.util.EObjects;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The default implementation of a {@link Reader} that reads data from a {@link DataMapper}.
 */
@ParametersAreNonnullByDefault
public class DefaultMapperReader extends AbstractReader<DataMapper> {

    /**
     * A LIFO that holds the current {@link EClass} chain. It contains the current element and the previous.
     */
    @Nonnull
    private final Deque<EClass> previousClasses = new ArrayDeque<>();
    /**
     * The mapper to read.
     */
    private DataMapper mapper;

    /**
     * Constructs a new {@code DefaultMapperReader} with the given {@code handler}.
     *
     * @param handler the handler to notify
     */
    public DefaultMapperReader(Handler handler) {
        super(handler);
    }

    @Override
    public void read(DataMapper source) {
        mapper = source;

        notifyInitialize();

        // TODO Calculates the feature identifier
        SingleFeatureBean rootKey = SingleFeatureBean.of(PersistentResource.ROOT_ID, -1);
        source.allReferencesOf(rootKey).forEach(id -> readElement(id, true));

        notifyComplete();
    }

    /**
     * Reads the element identified by its {@code id}.
     *
     * @param id     the identifier of the element
     * @param isRoot {@code true} if the element is a root element
     */
    private void readElement(Id id, boolean isRoot) {
        // Retrieve the meta-class and namespace
        EClass eClass = mapper.metaClassOf(id)
                .map(ClassBean::get)
                .<IllegalArgumentException>orElseThrow(IllegalArgumentException::new);

        BasicNamespace ns = BasicNamespace.Registry.getInstance().register(eClass.getEPackage());

        // Retrieve the name of the element
        // If root it's the name of the meta-class, otherwise the name of the containing feature from the previous class
        String name = isRoot ? eClass.getName() : mapper.containerOf(id)
                .map(SingleFeatureBean::id)
                .map(previousClasses.getLast()::getEStructuralFeature)
                .map(EStructuralFeature::getName)
                .<IllegalStateException>orElseThrow(IllegalStateException::new);

        // Create the meta-class
        BasicMetaclass metaClass = new BasicMetaclass(ns)
                .eClass(eClass);

        // Create the element
        BasicElement element = new BasicElement()
                .name(name)
                .id(id)
                .isRoot(isRoot)
                .metaClass(metaClass);

        notifyStartElement(element);
        previousClasses.addLast(eClass);

        // Process all features
        readAllFeatures(id, eClass);

        previousClasses.removeLast();
        notifyEndElement();
    }

    /**
     * Reads all features of the speficied {@code eClass} for the given {@code id}.
     *
     * @param id     the identifier of the element
     * @param eClass the meta-class of the element
     */
    private void readAllFeatures(Id id, EClass eClass) {
        // Read all feature of the element, and notify the next handler
        List<Id> containmentId = eClass.getEAllStructuralFeatures().stream()
                .flatMap(f -> {
                    Stream<Id> containmentStream = Stream.empty();

                    SingleFeatureBean bean = SingleFeatureBean.of(id, eClass.getFeatureID(f));

                    if (EObjects.isAttribute(f)) {
                        EAttribute eAttribute = EObjects.asAttribute(f);

                        if (!f.isMany()) {
                            mapper.valueOf(bean).ifPresent(v -> createAttribute(bean, eAttribute, v));
                        }
                        else {
                            mapper.allValuesOf(bean).forEach(v -> createAttribute(bean, eAttribute, v));
                        }
                    }
                    else {
                        EReference eReference = EObjects.asReference(f);
                        boolean isContainment = eReference.isContainment();

                        if (!f.isMany()) {
                            Optional<Id> reference = mapper.referenceOf(bean)
                                    .map(r -> createReference(bean, eReference, r));

                            if (isContainment) {
                                containmentStream = reference.map(Stream::of).orElseGet(Stream::empty);
                            }
                        }
                        else {
                            List<Id> references = mapper.allReferencesOf(bean)
                                    .map(r -> createReference(bean, eReference, r))
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());

                            if (isContainment) {
                                containmentStream = references.stream();
                            }
                        }
                    }

                    return containmentStream;
                })
                .collect(Collectors.toList());

        // Read the next element only if containerOf(next) == parent
        containmentId.forEach(r -> mapper.containerOf(r)
                .filter(c -> Objects.equals(c.owner(), id))
                .ifPresent(c -> readElement(r, false)));
    }

    /**
     * Creates and notifies a new attribute.
     *
     * @param feature    the owner of the attribute
     * @param eAttribute the associated EMF attribute
     * @param value      the value of the attribute
     */
    private void createAttribute(SingleFeatureBean feature, EAttribute eAttribute, Object value) {
        BasicAttribute attribute = new BasicAttribute()
                .owner(feature.owner())
                .id(feature.id())
                .eFeature(eAttribute)
                .value(value);

        notifyAttribute(attribute);
    }

    /**
     * Creates and notify a new reference.
     *
     * @param feature    the owner of the reference
     * @param eReference the associated EMF reference
     * @param value      the value of the reference
     *
     * @return the identifier of the referenced element if the reference is a containment, {@code null} otherwise
     */
    @Nullable
    private Id createReference(SingleFeatureBean feature, EReference eReference, Id value) {
        BasicReference reference = new BasicReference()
                .owner(feature.owner())
                .id(feature.id())
                .eFeature(eReference)
                .value(value);

        notifyReference(reference);

        return eReference.isContainment() ? value : null;
    }
}
