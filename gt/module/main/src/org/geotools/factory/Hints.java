/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
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
package org.geotools.factory;

// J2SE dependencies
import java.awt.RenderingHints;

// JAI dependencies
import javax.media.jai.JAI; // For Javadoc

// OpenGIS dependencies
import org.opengis.coverage.SampleDimensionType;                      // For Javadoc
import org.opengis.coverage.processing.GridCoverageProcessor;         // For Javadoc
import org.opengis.referencing.cs.CSFactory;                          // For Javadoc
import org.opengis.referencing.cs.CSAuthorityFactory;                 // For Javadoc
import org.opengis.referencing.crs.CRSFactory;                        // For Javadoc
import org.opengis.referencing.crs.CRSAuthorityFactory;               // For Javadoc
import org.opengis.referencing.datum.DatumFactory;                    // For Javadoc
import org.opengis.referencing.datum.DatumAuthorityFactory;           // For Javadoc
import org.opengis.referencing.operation.CoordinateOperationFactory;  // For Javadoc
import org.opengis.referencing.operation.MathTransformFactory;        // For Javadoc

// Geotools Dependencies
import org.geotools.resources.Utilities;


/**
 * A set of hints providing some control on factories to be used. Those hints are typically used
 * by renderers or {@linkplain org.opengis.coverage.processing.GridCoverageProcessor grid coverage
 * processors} for example. They provides a way to control low-level details. Implementations may
 * use the hints or ignore them. Example:
 *
 * <blockquote><pre>
 * CoordinateOperationFactory myFactory = <FONT FACE="Arial">...</FONT>
 * RenderingHints hints = new RenderingHints(Hints.{@link #COORDINATE_OPERATION_FACTORY}, myFactory);
 * GridCoverageProcessor processor = new GridCoverageProcessor2D(hints);
 * </pre></blockquote>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Hints extends RenderingHints.Key {
    /**
     * The amount of hints declared in this class.
     */
    private static int count;

    /**
     * Hint for the {@link CRSAuthorityFactory} to use.
     */
    public static final RenderingHints.Key CRS_AUTHORITY_FACTORY =
            new Hints("org.opengis.referencing.crs.CRSAuthorityFactory");

    /**
     * Hint for the {@link CSAuthorityFactory} to use.
     */
    public static final RenderingHints.Key CS_AUTHORITY_FACTORY =
            new Hints("org.opengis.referencing.cs.CSAuthorityFactory");

    /**
     * Hint for the {@link DatumAuthorityFactory} to use.
     */
    public static final RenderingHints.Key DATUM_AUTHORITY_FACTORY =
            new Hints("org.opengis.referencing.datum.DatumAuthorityFactory");

    /**
     * Hint for the {@link CRSFactory} to use.
     */
    public static final RenderingHints.Key CRS_FACTORY =
            new Hints("org.opengis.referencing.crs.CRSFactory");

    /**
     * Hint for the {@link CSFactory} to use.
     */
    public static final RenderingHints.Key CS_FACTORY =
            new Hints("org.opengis.referencing.cs.CSFactory");

    /**
     * Hint for the {@link DatumFactory} to use.
     */
    public static final RenderingHints.Key DATUM_FACTORY =
            new Hints("org.opengis.referencing.datum.DatumFactory");

    /**
     * Hint for the {@link CoordinateOperationFactory} to use.
     */
    public static final RenderingHints.Key COORDINATE_OPERATION_FACTORY =
            new Hints("org.opengis.referencing.operation.CoordinateOperationFactory");

    /**
     * Hint for the {@link MathTransformFactory} to use.
     */
    public static final RenderingHints.Key MATH_TRANSFORM_FACTORY =
            new Hints("org.opengis.referencing.operation.MathTransformFactory");

    /**
     * Hint for the {@link GridCoverageProcessor} instance to use.
     */
    public static final RenderingHints.Key GRID_COVERAGE_PROCESSOR =
            new Hints("org.opengis.coverage.processing.GridCoverageProcessor");

    /**
     * Hint for the {@link JAI} instance to use.
     */
    public static final RenderingHints.Key JAI_INSTANCE =
            new Hints("javax.media.jai.JAI");

    /**
     * Hint for the {@link SampleDimensionType} to use.
     */
    public static final RenderingHints.Key SAMPLE_DIMENSION_TYPE =
            new Hints("org.opengis.coverage.SampleDimensionType");

    /**
     * The class name for {@link #valueClass}.
     */
    private final String className;

    /**
     * Base class of all values for this key. Will be created from {@link #className}
     * only when first required, in order to avoid too early class loading.
     */
    private transient Class valueClass;

    /**
     * Constructs a new key.
     *
     * @param className Name of base class for all valid values.
     */
    private Hints(final String className) {
        super(count++);
        this.className = className;
        try {
            assert !Class.forName(className).isPrimitive();
        } catch (ClassNotFoundException exception) {
            throw new AssertionError(exception);
        }
    }

    /**
     * Returns {@code true} if the specified object is a valid value for this key.
     *
     * @param  value The object to test for validity.
     * @return {@code true} if the value is valid; <code>false</code> otherwise.
     */
    public boolean isCompatibleValue(final Object value) {
        if (value == null) {
            return false;
        }
        if (valueClass == null) try {
            valueClass = Class.forName(className);
        } catch (ClassNotFoundException exception) {
            Utilities.unexpectedException("org.geotools.factory", "Hints",
                                          "isCompatibleValue", exception);
            valueClass = Object.class;
        }
        return valueClass.isAssignableFrom(value.getClass());
    }
}
