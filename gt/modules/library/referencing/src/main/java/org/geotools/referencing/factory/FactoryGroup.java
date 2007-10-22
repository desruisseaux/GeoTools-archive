/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
 *   
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.referencing.factory;

import org.geotools.factory.Hints;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.operation.MathTransformFactory;


/**
 * A set of utilities methods working on factories. Many of those methods requires more than
 * one factory. Consequently, they can't be a method in a single factory. Furthermore, since
 * they are helper methods and somewhat implementation-dependent, they are not part of GeoAPI.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @deprecated Renamed to ReferencingFactoryContainer in order to reflect use 
 */
public final class FactoryGroup extends ReferencingFactoryContainer {
    /**
     * Constructs an instance using the specified factories. If any factory is null,
     * a default instance will be created by {@link GeometryFactoryFinder} when first needed.
     *
     * @param datumFactory The {@linkplain Datum datum} factory.
     * @param    csFactory The {@linkplain CoordinateSystem coordinate system} factory.
     * @param   crsFactory The {@linkplain CoordinateReferenceSystem coordinate reference system}
     *                     factory.
     * @param    mtFactory The {@linkplain MathTransform math transform} factory.
     *
     * @deprecated Use {@link #createInstance} instead. The fate of this constructor is
     *             incertain. It may be removed in Geotools 2.4, or refactored as a new
     *             {@code createInstance} convenience method.
     */
    public FactoryGroup(final DatumFactory      datumFactory,
                        final CSFactory            csFactory,
                        final CRSFactory          crsFactory,
                        final MathTransformFactory mtFactory)
    {
        super(datumFactory, csFactory, crsFactory, mtFactory);
    }

    /**
     * Creates an instance from the specified hints. This constructor recognizes the
     * {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS}, {@link Hints#DATUM_FACTORY DATUM}
     * and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM} {@code FACTORY} hints.
     * <p>
     * This constructor is public mainly for {@link org.geotools.factory.FactoryCreator} usage.
     * Consider invoking <code>{@linkplain #createInstance createInstance}(userHints)</code> instead.
     *
     * @param userHints The hints, or {@code null} if none.
     *
     * @since 2.2
     */
    public FactoryGroup(final Hints userHints) {
        super(userHints);
    }

    /**
     * Creates an instance from the specified hints. This method recognizes the
     * {@link Hints#CRS_FACTORY CRS}, {@link Hints#CS_FACTORY CS}, {@link Hints#DATUM_FACTORY DATUM}
     * and {@link Hints#MATH_TRANSFORM_FACTORY MATH_TRANSFORM} {@code FACTORY} hints.
     *
     * @param  hints The hints, or {@code null} if none.
     * @return A factory group created from the specified set of hints.
     *
     * @since 2.2
     */
    public static FactoryGroup createInstance(final Hints hints) {
        /* just so we can play the deprecation game */
        ReferencingFactoryContainer tmp = ReferencingFactoryContainer.instance(hints);
        return new FactoryGroup(tmp.getDatumFactory(),
                                tmp.getCSFactory(),
                                tmp.getCRSFactory(),
                                tmp.getMathTransformFactory());
    }
}
