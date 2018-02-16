/*
 * Copyright (c) 2013-2018 Atlanmod, Inria, LS2N, and IMT Nantes.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.tests;

import fr.inria.atlanmod.neoemf.context.Context;
import fr.inria.atlanmod.neoemf.resource.PersistentResource;
import fr.inria.atlanmod.neoemf.tests.provider.ContextProvider;
import fr.inria.atlanmod.neoemf.tests.sample.PrimaryObject;
import fr.inria.atlanmod.neoemf.tests.sample.TargetObject;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A test-case about {@link fr.inria.atlanmod.neoemf.core.internal.DirectStoreFeatureMap}.
 */
// TODO Add missing methods
@ParametersAreNonnullByDefault
class StoreFeatureMapTest extends AbstractResourceBasedTest {

    /**
     * Checks that the {@link FeatureMap}s are correctly detected and created.
     */
    @ParameterizedTest
    @ArgumentsSource(ContextProvider.All.class)
    void testNewInstance(Context context) throws IOException {
        try (PersistentResource resource = createPersistentResource(context)) {
            PrimaryObject primary = EFACTORY.createPrimaryObject();
            resource.getContents().add(primary);

            FeatureMap featureMapAttributes = primary.getFeatureMapAttributeCollection();
            assertThat(featureMapAttributes).isInstanceOf(FeatureMap.class);

            FeatureMap featureMapReferences = primary.getFeatureMapReferenceCollection();
            assertThat(featureMapReferences).isInstanceOf(FeatureMap.class);
        }
    }

    // region Attributes

    @ParameterizedTest
    @ArgumentsSource(ContextProvider.All.class)
    void testAddAttributes(Context context) throws IOException {
        String value0 = "Value0";
        String value1 = "Value1";
        String value2 = "Value2";
        String value3 = "Value3";

        try (PersistentResource resource = createPersistentResource(context)) {
            PrimaryObject primary = fillResource(resource);

            FeatureMap featureMapAttributes = primary.getFeatureMapAttributeCollection();
            List<String> attributes1 = primary.getFeatureMapAttributeType1();
            List<String> attributes2 = primary.getFeatureMapAttributeType2();

            attributes1.add(value0);
            attributes1.add(value1);
            attributes2.add(value2);
            attributes1.add(value3);

            assertThat(attributes1).hasSize(3);
            assertThat(attributes2).hasSize(1);
            assertThat(featureMapAttributes).hasSize(4);

            assertThat(featureMapAttributes.getValue(0)).isEqualTo(value0);
            assertThat(featureMapAttributes.getValue(1)).isEqualTo(value1);
            assertThat(featureMapAttributes.getValue(2)).isEqualTo(value2);
            assertThat(featureMapAttributes.getValue(3)).isEqualTo(value3);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ContextProvider.All.class)
    void testRemoveAttributes(Context context) throws IOException {
        String value0 = "Value0";
        String value1 = "Value1";
        String value2 = "Value2";
        String value3 = "Value3";

        try (PersistentResource resource = createPersistentResource(context)) {
            PrimaryObject primary = fillResource(resource);

            FeatureMap featureMapAttributes = primary.getFeatureMapAttributeCollection();
            List<String> attributes1 = primary.getFeatureMapAttributeType1();
            List<String> attributes2 = primary.getFeatureMapAttributeType2();

            attributes1.add(value0);
            attributes1.add(value1);
            attributes2.add(value2);
            attributes1.add(value3);

            attributes1.remove(1);

            assertThat(attributes1).hasSize(2);
            assertThat(featureMapAttributes).hasSize(3);

            assertThat(featureMapAttributes.getValue(0)).isEqualTo(value0);
            assertThat(featureMapAttributes.getValue(1)).isEqualTo(value2);
            assertThat(featureMapAttributes.getValue(2)).isEqualTo(value3);

            attributes1.remove(value0);

            assertThat(attributes1).hasSize(1);
            assertThat(featureMapAttributes).hasSize(2);

            assertThat(featureMapAttributes.getValue(0)).isEqualTo(value2);
            assertThat(featureMapAttributes.getValue(1)).isEqualTo(value3);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ContextProvider.All.class)
    void testSetAttributes(Context context) throws IOException {
        String value0 = "Value0";
        String value1 = "Value1";
        String value2 = "Value2";
        String value3 = "Value3";

        try (PersistentResource resource = createPersistentResource(context)) {
            PrimaryObject primary = fillResource(resource);

            FeatureMap featureMapAttributes = primary.getFeatureMapAttributeCollection();
            List<String> attributes1 = primary.getFeatureMapAttributeType1();
            List<String> attributes2 = primary.getFeatureMapAttributeType2();

            attributes1.add(value0);
            attributes2.add(value1);
            attributes1.add(value2);

            assertThat(attributes1).hasSize(2);
            assertThat(attributes2).hasSize(1);
            assertThat(featureMapAttributes).hasSize(3);

            assertThat(featureMapAttributes.getValue(0)).isEqualTo(value0);
            assertThat(featureMapAttributes.getValue(1)).isEqualTo(value1);
            assertThat(featureMapAttributes.getValue(2)).isEqualTo(value2);

            attributes2.set(0, value3); // Replace 1 by 3

            assertThat(attributes1).hasSize(2);
            assertThat(attributes2).hasSize(1);
            assertThat(featureMapAttributes).hasSize(3);

            assertThat(featureMapAttributes.getValue(0)).isEqualTo(value0);
            assertThat(featureMapAttributes.getValue(1)).isEqualTo(value3);
            assertThat(featureMapAttributes.getValue(2)).isEqualTo(value2);
        }
    }

    // endregion

    // region References

    @ParameterizedTest
    @ArgumentsSource(ContextProvider.All.class)
    void testAddReferences(Context context) throws IOException {
        TargetObject target0 = EFACTORY.createTargetObject();
        target0.setName("Target0");

        TargetObject target1 = EFACTORY.createTargetObject();
        target1.setName("Target1");

        TargetObject target2 = EFACTORY.createTargetObject();
        target2.setName("Target2");

        TargetObject target3 = EFACTORY.createTargetObject();
        target2.setName("Target3");

        try (PersistentResource resource = createPersistentResource(context)) {
            PrimaryObject primary = fillResource(resource);

            FeatureMap featureMapReferences = primary.getFeatureMapReferenceCollection();
            List<TargetObject> references1 = primary.getFeatureMapReferenceType1();
            List<TargetObject> references2 = primary.getFeatureMapReferenceType2();

            references1.add(target0);
            references1.add(target1);
            references2.add(target2);
            references1.add(target3);

            assertThat(references1).hasSize(3);
            assertThat(references2).hasSize(1);
            assertThat(featureMapReferences).hasSize(4);

            assertThat(featureMapReferences.getValue(0)).isEqualTo(target0);
            assertThat(featureMapReferences.getValue(1)).isEqualTo(target1);
            assertThat(featureMapReferences.getValue(2)).isEqualTo(target2);
            assertThat(featureMapReferences.getValue(3)).isEqualTo(target3);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ContextProvider.All.class)
    void testRemoveReferences(Context context) throws IOException {
        TargetObject target0 = EFACTORY.createTargetObject();
        target0.setName("Target0");

        TargetObject target1 = EFACTORY.createTargetObject();
        target1.setName("Target1");

        TargetObject target2 = EFACTORY.createTargetObject();
        target2.setName("Target2");

        TargetObject target3 = EFACTORY.createTargetObject();
        target2.setName("Target3");

        try (PersistentResource resource = createPersistentResource(context)) {
            PrimaryObject primary = fillResource(resource);

            FeatureMap featureMapReferences = primary.getFeatureMapReferenceCollection();
            List<TargetObject> references1 = primary.getFeatureMapReferenceType1();
            List<TargetObject> references2 = primary.getFeatureMapReferenceType2();

            references1.add(target0);
            references1.add(target1);
            references2.add(target2);
            references1.add(target3);

            references1.remove(1);

            assertThat(references1).hasSize(2);
            assertThat(featureMapReferences).hasSize(3);

            assertThat(featureMapReferences.getValue(0)).isEqualTo(target0);
            assertThat(featureMapReferences.getValue(1)).isEqualTo(target2);
            assertThat(featureMapReferences.getValue(2)).isEqualTo(target3);

            references1.remove(target0);

            assertThat(references1).hasSize(1);
            assertThat(featureMapReferences).hasSize(2);

            assertThat(featureMapReferences.getValue(0)).isEqualTo(target2);
            assertThat(featureMapReferences.getValue(1)).isEqualTo(target3);
        }
    }

    @ParameterizedTest
    @ArgumentsSource(ContextProvider.All.class)
    void testSetReferences(Context context) throws IOException {
        TargetObject target0 = EFACTORY.createTargetObject();
        target0.setName("Target0");

        TargetObject target1 = EFACTORY.createTargetObject();
        target1.setName("Target1");

        TargetObject target2 = EFACTORY.createTargetObject();
        target2.setName("Target2");

        TargetObject target3 = EFACTORY.createTargetObject();
        target2.setName("Target3");

        try (PersistentResource resource = createPersistentResource(context)) {
            PrimaryObject primary = fillResource(resource);

            FeatureMap featureMapReferences = primary.getFeatureMapReferenceCollection();
            List<TargetObject> references1 = primary.getFeatureMapReferenceType1();
            List<TargetObject> references2 = primary.getFeatureMapReferenceType2();

            references1.add(target0);
            references2.add(target1);
            references1.add(target2);

            assertThat(references1).hasSize(2);
            assertThat(references2).hasSize(1);
            assertThat(featureMapReferences).hasSize(3);

            assertThat(featureMapReferences.getValue(0)).isEqualTo(target0);
            assertThat(featureMapReferences.getValue(1)).isEqualTo(target1);
            assertThat(featureMapReferences.getValue(2)).isEqualTo(target2);

            references2.set(0, target3); // Replace 1 by 3

            assertThat(references1).hasSize(2);
            assertThat(references2).hasSize(1);
            assertThat(featureMapReferences).hasSize(3);

            assertThat(featureMapReferences.getValue(0)).isEqualTo(target0);
            assertThat(featureMapReferences.getValue(1)).isEqualTo(target3);
            assertThat(featureMapReferences.getValue(2)).isEqualTo(target2);
        }
    }

    // endregion

    @Nonnull
    private PrimaryObject fillResource(Resource resource) throws IOException {
        PrimaryObject primary = EFACTORY.createPrimaryObject();
        resource.getContents().add(primary);
        return primary;
    }
}
