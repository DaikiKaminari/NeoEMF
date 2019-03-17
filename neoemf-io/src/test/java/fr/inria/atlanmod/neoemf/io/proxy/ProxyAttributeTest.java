/*
 * Copyright (c) 2013-2018 Atlanmod, Inria, LS2N, and IMT Nantes.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.io.proxy;

import fr.inria.atlanmod.neoemf.core.Id;

import org.atlanmod.commons.AbstractTest;
import org.junit.jupiter.api.Test;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A test-case about {@link ProxyAttribute}.
 */
@ParametersAreNonnullByDefault
class ProxyAttributeTest extends AbstractTest {

    @Test
    void testName() {
        String name0 = "attribute0";
        String name1 = "attribute1";

        ProxyAttribute attr0 = new ProxyAttribute();
        attr0.setName(name0);
        assertThat(attr0.getName()).isEqualTo(name0);

        ProxyAttribute attr1 = new ProxyAttribute();
        attr1.setName(name1);
        assertThat(attr1.getName()).isEqualTo(name1);

        assertThat(attr0.getName()).isNotEqualTo(attr1.getName());
    }

    @Test
    void testId() {
        ProxyAttribute attr0 = new ProxyAttribute();
        attr0.setName("attribute0");
        assertThat(attr0.getOwner()).isNull();

        Id id0 = Id.getProvider().fromLong(42);
        Id id1 = Id.getProvider().fromLong(44);

        attr0.setOwner(id0);
        assertThat(attr0.getOwner()).isEqualTo(id0);

        attr0.setOwner(id1);
        assertThat(attr0.getOwner()).isNotEqualTo(id0).isEqualTo(id1);
    }

    @Test
    void testMany() {
        ProxyAttribute attr0 = new ProxyAttribute();
        attr0.setName("attribute0");
        assertThat(attr0.isMany()).isFalse();

        attr0.setMany(true);
        assertThat(attr0.isMany()).isTrue();

        attr0.setMany(false);
        assertThat(attr0.isMany()).isFalse();
    }

    @Test
    void testValue() {
        ProxyAttribute attr0 = new ProxyAttribute();
        attr0.setName("attribute0");
        assertThat(attr0.getValue().isPresent()).isFalse();

        String value0 = "value0";
        String value1 = "value1";

        attr0.setValue(ProxyValue.resolved(value0));
        assertThat(attr0.getValue().getResolved()).isEqualTo(value0);

        attr0.setValue(ProxyValue.resolved(value1));
        assertThat(attr0.getValue().getResolved()).isNotEqualTo(value0).isEqualTo(value1);
    }

    @Test
    void testHashCode() {
        ProxyAttribute attr0 = new ProxyAttribute();
        attr0.setName("attribute0");
        ProxyAttribute attr0Bis = new ProxyAttribute();
        attr0Bis.setName("attribute0");
        ProxyAttribute attr1 = new ProxyAttribute();
        attr1.setName("attribute1");

        assertThat(attr0.hashCode()).isEqualTo(attr0Bis.hashCode());
        assertThat(attr0.hashCode()).isNotEqualTo(attr1.hashCode());
        assertThat(attr1.hashCode()).isNotEqualTo(attr0Bis.hashCode());
    }

    @Test
    void testEquals() {
        ProxyAttribute attr0 = new ProxyAttribute();
        attr0.setName("attribute0");
        ProxyAttribute attr0Bis = new ProxyAttribute();
        attr0Bis.setName("attribute0");
        ProxyAttribute attr1 = new ProxyAttribute();
        attr1.setName("attribute1");

        assertThat(attr0).isEqualTo(attr0Bis);
        assertThat(attr0).isNotEqualTo(attr1);
        assertThat(attr1).isNotEqualTo(attr0Bis);

        assertThat(attr0).isEqualTo(attr0);
        assertThat(attr0).isNotEqualTo(null);
        assertThat(attr0).isNotEqualTo(0);
    }
}