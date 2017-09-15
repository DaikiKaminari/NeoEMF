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

package fr.inria.atlanmod.neoemf.data.hbase.util;

import fr.inria.atlanmod.neoemf.data.hbase.context.HBaseTest;
import fr.inria.atlanmod.neoemf.util.AbstractUriTest;

import org.eclipse.emf.common.util.URI;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * A test-case about {@link HBaseUri}.
 */
public class HBaseUriTest extends AbstractUriTest implements HBaseTest {

    @Ignore("Not supported because of the mini-cluster")
    @Override
    public void testCreateUriFromStandardUriInvalidScheme() {
    }

    /**
     * Checks the creation of a file-based {@link URI} from another {@link URI}.
     * <p>
     * HBase does not support file-based {@link URI}s, so this operation must fail.
     */
    @Test
    public void testCreateUriFromUri() {
        assertThat(catchThrowable(() -> HBaseUri.builder().fromUri(URI.createURI("uri0"))))
                .isExactlyInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Checks the creation of a file-based {@link URI} from a {@link File}.
     * <p>
     * HBase does not support file-based {@link URI}s, so this operation must fail.
     */
    @Test
    public void testCreateUriFromFile() {
        assertThat(catchThrowable(() -> HBaseUri.builder().fromFile("file0")))
                .isExactlyInstanceOf(UnsupportedOperationException.class);
    }
}
