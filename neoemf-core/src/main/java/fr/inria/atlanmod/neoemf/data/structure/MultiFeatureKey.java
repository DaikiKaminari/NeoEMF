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

package fr.inria.atlanmod.neoemf.data.structure;

import fr.inria.atlanmod.neoemf.core.Id;
import fr.inria.atlanmod.neoemf.core.PersistentEObject;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import java.io.Serializable;
import java.util.Objects;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A simple representation of a multi-valued {@link EStructuralFeature} of a {@link PersistentEObject}. The
 * "multi-valued" characteristic is identified with a position.
 */
@ParametersAreNonnullByDefault
public class MultiFeatureKey extends SingleFeatureKey implements Serializable {

    @SuppressWarnings("JavaDoc")
    private static final long serialVersionUID = 7159493156068733506L;

    /**
     * The position of this key.
     */
    @Nonnegative
    protected final int position;

    /**
     * Constructs a new {@code MultiFeatureKey} with the given {@code id} and the given {@code name}, which are
     * used as a simple representation of a feature of an object. The "multi-valued" characteristic is identified with
     * the {@code position}.
     *
     * @param id       the identifier of the {@link PersistentEObject}
     * @param name     the name of the {@link EStructuralFeature} of the {@link PersistentEObject}
     * @param position the position of the {@link EStructuralFeature}
     */
    protected MultiFeatureKey(Id id, String name, @Nonnegative int position) {
        super(id, name);
        checkArgument(position >= 0, "Position must be >= 0");
        this.position = position;
    }

    /**
     * Creates a new {@code MultiFeatureKey} from the given {@code internalObject} and the given {@code feature}.
     * <p>
     * This method behaves like: {@code from(PersistentEObject.from(internalObject), feature,
     * position)}.
     *
     * @param internalObject the {@link InternalEObject} that will be adapted as {@link PersistentEObject} to have its
     *                       identifier
     * @param feature        the {@link EStructuralFeature} of the {@link PersistentEObject} from which the name will be
     *                       extracted
     * @param position       the position of the {@link EStructuralFeature}
     *
     * @return a new {@code MultiFeatureKey}
     *
     * @see #from(PersistentEObject, EStructuralFeature, int)
     * @see PersistentEObject#from(Object)
     * @see EStructuralFeature#getName()
     */
    public static MultiFeatureKey from(InternalEObject internalObject, EStructuralFeature feature, @Nonnegative int position) {
        return from(PersistentEObject.from(internalObject), feature, position);
    }

    /**
     * Creates a new {@code MultiFeatureKey} from the given {@code object} and the given {@code feature}.
     * <p>
     * This method behaves like: {@code of(object.id(), feature.getName(), position)}.
     *
     * @param object   the {@link PersistentEObject} from which the identifier will be extracted
     * @param feature  the {@link EStructuralFeature} of the {@link PersistentEObject} from which the name will be
     *                 extracted
     * @param position the position of the {@link EStructuralFeature}
     *
     * @return a new {@code MultiFeatureKey}
     *
     * @see #of(Id, String, int)
     * @see PersistentEObject#id()
     * @see EStructuralFeature#getName()
     */
    public static MultiFeatureKey from(PersistentEObject object, EStructuralFeature feature, @Nonnegative int position) {
        return of(object.id(), feature.getName(), position);
    }

    /**
     * Creates a new {@code MultiFeatureKey} with the given {@code id} and the given {@code name}, which are used
     * as a simple representation of a feature of an object. The "multi-valued" characteristic is identified with the
     * {@code position}.
     *
     * @param id       the identifier of the {@link PersistentEObject}
     * @param name     the name of the {@link EStructuralFeature} of the {@link PersistentEObject}
     * @param position the position of the {@link EStructuralFeature}
     *
     * @return a new {@code MultiFeatureKey}
     */
    public static MultiFeatureKey of(Id id, String name, @Nonnegative int position) {
        return new MultiFeatureKey(id, name, position);
    }

    /**
     * Returns the position of the {@link EStructuralFeature}.
     *
     * @return the position of the feature
     */
    @Nonnegative
    public int position() {
        return position;
    }

    /**
     * Creates a new {@link SingleFeatureKey} with the {@link Id} and the name of this {@code MultiFeatureKey}, without
     * its position.
     *
     * @return a new {@link SingleFeatureKey}
     *
     * @see SingleFeatureKey#of(Id, String)
     */
    @Nonnull
    public SingleFeatureKey withoutPosition() {
        return SingleFeatureKey.of(id, name);
    }

    @Override
    public int compareTo(SingleFeatureKey o) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (!(o instanceof MultiFeatureKey)) {
            return AFTER;
        }
        int comparison = super.compareTo(o);
        if (comparison == EQUAL) {
            MultiFeatureKey that = (MultiFeatureKey) o;
            return (position > that.position) ? AFTER : (position < that.position) ? BEFORE : EQUAL;
        }
        else {
            return comparison;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id(), name(), position);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return super.equals(o) && position == ((MultiFeatureKey) o).position;
    }
}