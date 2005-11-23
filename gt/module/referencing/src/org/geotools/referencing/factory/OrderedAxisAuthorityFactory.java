/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.referencing.factory;

// J2SE dependencies
import java.util.Map;

// OpenGIS dependencies
import org.opengis.referencing.cs.*;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.FactoryException;
import org.opengis.metadata.citation.Citation;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.referencing.cs.AbstractCS;


/**
 * An authority factory which delegates all the work to an other factory, and reorder the axis in
 * some pre-determined order. This factory is mostly used by application expecting geographic
 * coordinates in (<var>longitude</var>, <var>latitude</var>) order, while most geographic CRS
 * specified in the EPSG database use the opposite axis order.
 *
 * @since 2.2
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class OrderedAxisAuthorityFactory extends AuthorityFactoryAdapter {
    /**
     * {@code true} if this authority factory should also force all angular units to degrees
     * and linear units to meters. The default value is {@code false}.
     */
    private final boolean fixUnits;

    /**
     * The authority for this factory. Will be inferred from the {@linkplain #factory underlying
     * factory} when first requested.
     */
    private transient Citation authority;

    /**
     * Creates a factory which will reorder the axis of all objects created by
     * the supplied factory. The priority level will be equals to the specified
     * {@linkplain AbstractAuthorityFactory#priority factory's priority} plus one.
     *
     * @param factory  The factory that produces objects using arbitrary axis order.
     */
    public OrderedAxisAuthorityFactory(final AbstractAuthorityFactory factory) {
        this(factory, false);
    }

    /**
     * Creates a factory which will optionally fix units in addition of axis order.
     * The priority level will be equals to the specified
     * {@linkplain AbstractAuthorityFactory#priority factory's priority} plus one.
     *
     * @param factory  The factory that produces objects using arbitrary axis order.
     * @param fixUnits {@code true} if this authority factory should also force all angular units
     *                 to degrees and linear units to meters. The default value is {@code false}.
     */
    public OrderedAxisAuthorityFactory(final AbstractAuthorityFactory factory,
                                       final boolean fixUnits)
    {
        super(factory);
        this.fixUnits = fixUnits;
    }

    /**
     * Returns the organization or party responsible for definition and maintenance of the
     * database. The default implementation returns the authority of the {@linkplain #factory
     * underlying factory} with "(modified axis)" label appended.
     */
    public Citation getAuthority() {
        // No need to synchronize; not a big deal if the citation is created twice.
        if (authority == null) {
            final Citation fc = crsFactory.getAuthority();
            authority = new CitationImpl(Vocabulary.formatInternational(
                            VocabularyKeys.MODIFIED_AXIS_$1, fc.getTitle()));
        }
        return authority;
    }

    /**
     * Reorder (if needed) the axis in the specified coordinate system. The
     * default implementation uses the same axis order than the one returned by
     * <code>{@linkplain AbstractCS#standard AbstractCS.standard}(cs)</code>.
     * <p>
     * <strong>Implementation note:</strong> It would have been possible to reorder axis using some
     * algorithm more generic than <code>{@linkplain AbstractCS#standard standard}(cs)</code>. But
     * we use the above-cited method anyway in order to get consistent "standard" axis accross the
     * whole Geotools implementation.
     *
     * @throws FactoryException If this method can't rearange the axis for the specified {@code cs}.
     */
    protected CoordinateSystem replace(CoordinateSystem cs) throws FactoryException {
        final CoordinateSystem candidate;
        final Matrix changes;
        try {
            candidate = AbstractCS.standard(cs);
            if (!fixUnits) {
                return candidate;
            }
            changes = AbstractCS.swapAndScaleAxis(cs, candidate);
        } catch (RuntimeException e) {
            /*
             * Catchs all runtime exceptions for simplicity, but the only two ones that we
             * really want to catch are IllegalArgumentException and ConversionException.
             */
            throw new FactoryException(getErrorMessage(cs), e);
        }
        /*
         * Reorder the axis according the information provided in the changes matrix. Each
         * column (except the offset column, which is ignored) should have one and only one
         * non-null value. The row position of this non-null value gives us the axis position
         * in the new CS to be created.
         */
        final CoordinateSystemAxis[] axis = new CoordinateSystemAxis[cs.getDimension()];
        assert changes.getNumCol() == axis.length + 1 : changes;
        assert changes.getNumRow() == axis.length + 1 : changes;
        boolean changed = false;
        for (int i=0; i<axis.length; i++) {
            int pos = -1;
            for (int j=0; j<axis.length; j++) {
                if (changes.getElement(j,i) != 0) {
                    if (pos >= 0) {
                        throw new FactoryException(getErrorMessage(cs));
                    }
                    pos = j;
                }
            }
            if (pos < 0 || axis[pos] != null) {
                throw new FactoryException(getErrorMessage(cs));
            }
            axis[pos] = cs.getAxis(i);
            if (pos != i) {
                changed = true;
            }
        }
        /*
         * If the axis order changed, creates a new coordinate system using the new axis.
         */
        return (changed) ? createCS(cs, axis) : cs;
    }

    /**
     * Creates a new coordinate system of the same kind than the specified CS, but different
     * axis. This method is invoked automatically by {@link #replace(CoordinateSystem)} after
     * it determined that the axis order need to be changed. Subclasses can override this method
     * if they want to performs some extra processing on the axis order.
     *
     * @param  cs   The original coordinate system.
     * @param  axis The axis to give to the new coordinate system. Subclasses are allowed to write
     *              directly in this array (no need to copy it).
     * @return A new coordinate system of the same kind than {@code cs} but with the specified axis.
     * @throws FactoryException if the coordinate system can't be created.
     * @throws IndexOutOfBoundsException if the length of {@code axis} is smaller than the number of
     *         dimensions in {@code cs}.
     */
    protected CoordinateSystem createCS(final CoordinateSystem cs, final CoordinateSystemAxis[] axis)
            throws FactoryException
    {
        final int dimension = cs.getDimension();
        final Map properties = getProperties(cs);
        final CSFactory csFactory = factories.getCSFactory();
        if (cs instanceof CartesianCS) {
            switch (dimension) {
                case 2: return csFactory.createCartesianCS(properties, axis[0], axis[1]);
                case 3: return csFactory.createCartesianCS(properties, axis[0], axis[1], axis[2]);
            }
        }
        if (cs instanceof EllipsoidalCS) {
            switch (dimension) {
                case 2: return csFactory.createEllipsoidalCS(properties, axis[0], axis[1]);
                case 3: return csFactory.createEllipsoidalCS(properties, axis[0], axis[1], axis[2]);
            }
        }
        if (cs instanceof SphericalCS) {
            switch (dimension) {
                case 3: return csFactory.createSphericalCS(properties, axis[0], axis[1], axis[2]);
            }
        }
        if (cs instanceof CylindricalCS) {
            switch (dimension) {
                case 3: return csFactory.createCylindricalCS(properties, axis[0], axis[1], axis[2]);
            }
        }
        if (cs instanceof PolarCS) {
            switch (dimension) {
                case 2: return csFactory.createPolarCS(properties, axis[0], axis[1]);
            }
        }
        if (cs instanceof VerticalCS) {
            switch (dimension) {
                case 1: return csFactory.createVerticalCS(properties, axis[0]);
            }
        }
        if (cs instanceof TimeCS) {
            switch (dimension) {
                case 1: return csFactory.createTimeCS(properties, axis[0]);
            }
        }
        if (cs instanceof LinearCS) {
            switch (dimension) {
                case 1: return csFactory.createLinearCS(properties, axis[0]);
            }
        }
        if (cs instanceof UserDefinedCS) {
            switch (dimension) {
                case 2: return csFactory.createUserDefinedCS(properties, axis[0], axis[1]);
                case 3: return csFactory.createUserDefinedCS(properties, axis[0], axis[1], axis[2]);
            }
        }
        throw new FactoryException(getErrorMessage(cs));
    }

    /**
     * Returns the error message for the specified coordinate system.
     * Used when throwing {@link FactoryException}.
     */
    private static final String getErrorMessage(final CoordinateSystem cs) {
        return Errors.format(ErrorKeys.UNSUPPORTED_COORDINATE_SYSTEM_$1, cs.getName().getCode());
    }
}
