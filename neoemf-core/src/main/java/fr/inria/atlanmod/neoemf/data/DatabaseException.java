/*
 * Copyright (c) 2013-2018 Atlanmod, Inria, LS2N, and IMT Nantes.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v2.0 which accompanies
 * this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 */

package fr.inria.atlanmod.neoemf.data;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Exception thrown when an error occurred when reading/manipulating a database.
 */
@ParametersAreNonnullByDefault
public class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = -5045169255855866489L;

    /**
     * Constructs an {@code DatabaseException} with {@code null} as its error detail message.
     */
    public DatabaseException() {
    }

    /**
     * Constructs an {@code DatabaseException} with the specified detail {@code message}.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public DatabaseException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code DatabaseException} with the specified {@code cause} and its detail message.
     *
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method). (A {@code null}
     *              value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public DatabaseException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an {@code DatabaseException} with the specified detail {@code message} and {@code cause}.
     * <p>
     * The detail message associated with cause is not automatically incorporated into this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method). (A {@code null}
     *                value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
