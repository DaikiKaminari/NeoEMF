/*
 * Copyright (c) 2013-2018 Atlanmod, Inria, LS2N, and IMT Nantes.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.io.processor;

import fr.inria.atlanmod.commons.log.Log;
import fr.inria.atlanmod.commons.primitive.Strings;
import fr.inria.atlanmod.neoemf.core.Id;
import fr.inria.atlanmod.neoemf.io.bean.BasicAttribute;
import fr.inria.atlanmod.neoemf.io.bean.BasicElement;
import fr.inria.atlanmod.neoemf.io.bean.BasicMetaclass;
import fr.inria.atlanmod.neoemf.io.bean.BasicNamespace;
import fr.inria.atlanmod.neoemf.io.bean.BasicReference;
import fr.inria.atlanmod.neoemf.util.EObjects;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static fr.inria.atlanmod.commons.Preconditions.checkArgument;
import static fr.inria.atlanmod.commons.Preconditions.checkNotNull;
import static java.util.Objects.nonNull;

/**
 * A {@link Processor} that transforms simple elements to an EMF structure.
 */
@ParametersAreNonnullByDefault
public class EcoreProcessor extends AbstractProcessor<Processor> {

    /**
     * A LIFO that holds the current {@link BasicElement} chain. It contains the current element and the previous.
     */
    @Nonnull
    private final Deque<BasicElement> previousElements = new ArrayDeque<>();

    /**
     * An attribute that is waiting for a value.
     *
     * @see #onCharacters(String)
     */
    @Nullable
    private BasicAttribute waitingAttribute;

    /**
     * Defines if the previous element was an attribute, or not.
     */
    private boolean previousWasAttribute;

    /**
     * Constructs a new {@code EcoreProcessor} with the given {@code processor}.
     *
     * @param processor the processor to notify
     */
    public EcoreProcessor(Processor processor) {
        super(processor);
    }

    @Override
    public void onStartElement(BasicElement element) {
        // Is root
        if (previousElements.isEmpty()) {
            processElementAsRoot(element);
        }
        // Is a feature of parent
        else {
            processElementAsFeature(element);
        }
    }

    @Override
    public void onAttribute(BasicAttribute attribute) {
        BasicElement parentElement = previousElements.getLast();
        EStructuralFeature eFeature = parentElement.metaClass().eClass().getEStructuralFeature(attribute.name());

        if (EObjects.isAttribute(eFeature)) {
            // The attribute is well a attribute
            processAttribute(attribute, EObjects.asAttribute(eFeature));
        }
        else {
            // Otherwise redirect to the reference handler
            BasicReference reference = new BasicReference()
                    .rawValue(attribute.rawValue());

            processReference(reference, EObjects.asReference(eFeature));
        }
    }

    @Override
    public void onReference(BasicReference reference) {
        BasicElement parentElement = previousElements.getLast();
        EStructuralFeature eFeature = parentElement.metaClass().eClass().getEStructuralFeature(reference.name());

        processReference(reference, EObjects.asReference(eFeature));
    }

    @Override
    public void onCharacters(String characters) {
        // Defines the value of the waiting attribute, if exists
        if (nonNull(waitingAttribute)) {
            waitingAttribute.value(ValueConverter.INSTANCE.convert(characters, waitingAttribute.eFeature()));

            onAttribute(waitingAttribute);

            waitingAttribute = null;
        }
        else {
            Log.debug("Ignoring characters: \"{0}\"", characters);
        }
    }

    @Override
    public void onEndElement() {
        if (!previousWasAttribute) {
            previousElements.removeLast();

            notifyEndElement();
        }
        else {
            Log.warn("An attribute still waiting for a value: it will be ignored");
            waitingAttribute = null;
            previousWasAttribute = false;
        }
    }

    /**
     * Creates the root element from the given {@code element}.
     *
     * @param element the element representing the root element
     *
     * @throws NullPointerException if the {@code element} does not have a namespace
     */
    private void processElementAsRoot(BasicElement element) {
        checkNotNull(element.metaClass(), "The root element must have a namespace");

        BasicMetaclass metaClass = element.metaClass();

        // Retrieve the EPackage from the NS prefix
        EPackage ePackage = metaClass.ns().ePackage();

        // Retrieve the current EClass & define the meta-class of the current element if not present
        EClass eClass = EClass.class.cast(ePackage.getEClassifier(element.name()));
        checkNotNull(eClass, "Cannot retrieve EClass %s from the EPackage %s", element.name(), ePackage);
        metaClass.eClass(eClass);

        // Define the element as root node
        element.isRoot(true);

        // Notify next handlers
        notifyStartElement(element);

        // Save the current element
        previousElements.addLast(element);
    }

    /**
     * Processes an element as a feature and redirects the processing to the associated method according to its type
     * (attribute or reference).
     *
     * @param element the element representing the feature
     *
     * @see #processElementAsAttribute(EAttribute)
     * @see #processElementAsReference(BasicElement, EReference)
     */
    private void processElementAsFeature(BasicElement element) {
        // Retrieve the parent EClass
        BasicElement parentElement = previousElements.getLast();
        EClass parentEClass = parentElement.metaClass().eClass();

        // Retrieve the structural feature from the parent, according the its local name (the attr/ref name)
        EStructuralFeature feature = parentEClass.getEStructuralFeature(element.name());

        if (EObjects.isAttribute(feature)) {
            processElementAsAttribute(EObjects.asAttribute(feature));
        }
        else {
            EReference eReference = EObjects.asReference(feature);

            // Retrieve the namespace xor the EPackage, and build the meta-class
            BasicNamespace ns;
            EPackage ePackage;

            if (nonNull(element.metaClass())) {
                ns = element.metaClass().ns();
                ePackage = ns.ePackage();
            }
            else {
                ePackage = parentEClass.getEPackage();
                ns = BasicNamespace.Registry.getInstance().getByUri(ePackage.getNsURI());
                ns.ePackage(ePackage);
                element.metaClass(new BasicMetaclass(ns));
            }

            BasicMetaclass metaClass = element.metaClass();

            // Retrieve the type the reference or gets the type from the registered meta-class
            EClass eClass = resolveInstanceOf(metaClass, EClass.class.cast(eReference.getEType()), ePackage);
            metaClass.eClass(eClass);

            processElementAsReference(element, eReference);
        }
    }

    /**
     * Processes an element as an attribute.
     *
     * @param eAttribute the associated EMF attribute
     */
    private void processElementAsAttribute(EAttribute eAttribute) {
        if (nonNull(waitingAttribute)) {
            Log.warn("An attribute still waiting for a value : it will be ignored");
        }

        BasicElement parentElement = previousElements.getLast();
        EClass parentEClass = parentElement.metaClass().eClass();

        // The attribute waiting a plain text value
        waitingAttribute = new BasicAttribute()
                .owner(parentElement.id())
                .id(parentEClass.getFeatureID(eAttribute))
                .eFeature(eAttribute);

        previousWasAttribute = true;
    }

    /**
     * Processes an element as a reference.
     *
     * @param element    the element representing the reference
     * @param eReference the associated EMF reference
     */
    private void processElementAsReference(BasicElement element, EReference eReference) {
        // Notify next handlers of new element
        notifyStartElement(element);

        // Retrieve the identifier of the element (generated by next handlers)
        Id currentId = element.id();

        // Create a reference from the parent to this element, with the given local name
        if (eReference.isContainment()) {
            BasicReference reference = new BasicReference()
                    .value(currentId);

            processReference(reference, eReference);
        }

        // Save the current element
        previousElements.addLast(element);
    }

    /**
     * Processes an attribute.
     *
     * @param attribute  the attribute to process
     * @param eAttribute the associated EMF attribute
     */
    private void processAttribute(BasicAttribute attribute, EAttribute eAttribute) {
        BasicElement parentElement = previousElements.getLast();
        EClass parentEClass = parentElement.metaClass().eClass();

        attribute.owner(parentElement.id())
                .id(parentEClass.getFeatureID(eAttribute))
                .eFeature(eAttribute)
                .value(ValueConverter.INSTANCE.convert(attribute.rawValue(), eAttribute));

        notifyAttribute(attribute);
    }

    /**
     * Processes a reference.
     *
     * @param reference  the reference to process
     * @param eReference the associated EMF reference
     */
    private void processReference(BasicReference reference, EReference eReference) {
        BasicElement parentElement = previousElements.getLast();
        EClass parentEClass = parentElement.metaClass().eClass();

        reference.owner(parentElement.id())
                .id(parentEClass.getFeatureID(eReference))
                .eFeature(eReference);

        // The unique identifier is already set
        if (nonNull(reference.value())) {
            notifyReference(reference);
        }
        else {
            processRawReference(reference, eReference);
        }
    }

    /**
     * Process one or several references from the raw value of the {@code reference}. If the reference is a multi-valued
     * reference, each raw identifier will be delimited by a space.
     *
     * @param reference  the reference to process
     * @param eReference the associated EMF reference
     */
    private void processRawReference(BasicReference reference, EReference eReference) {
        final Function<String, BasicReference> createFunc = s -> new BasicReference()
                .owner(reference.owner())
                .id(reference.id())
                .eFeature(eReference)
                .rawValue(s);

        Arrays.stream(reference.rawValue().split(Strings.SPACE))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(createFunc)
                .forEach(this::notifyReference);
    }

    /**
     * Returns the {@link EClass} associated with the given {@code element}.
     *
     * @param metaClass  the element representing the class
     * @param superClass the super-type of the class
     * @param ePackage   the package where to find the class
     *
     * @return a class
     *
     * @throws IllegalArgumentException if the {@code superClass} is not the super-type of the sought class
     */
    @Nonnull
    private EClass resolveInstanceOf(BasicMetaclass metaClass, EClass superClass, EPackage ePackage) {
        // Is a more specific meta-class defined ?
        if (nonNull(metaClass.name())) {
            EClass eClass = EClass.class.cast(ePackage.getEClassifier(metaClass.name()));

            checkNotNull(eClass, "Cannot retrieve EClass {0} from the EPackage {1}", metaClass.name(), ePackage);

            // Checks that the meta-class is a subtype of the reference type
            checkArgument(superClass.isSuperTypeOf(eClass),
                    "%s is not a subclass of %s", eClass.getName(), superClass.getName());

            return eClass;
        }

        return superClass;
    }
}
