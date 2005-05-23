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
import java.util.Map;
import java.util.HashMap;
import java.awt.RenderingHints;

// Geotools Dependencies
import org.geotools.resources.Utilities;


/**
 * A set of hints providing control on factories to be used. 
 * <p>
 * Those hints are typically used by renderers or
 * {@linkplain org.opengis.coverage.processing.GridCoverageProcessor grid coverage processors}
 * for example. They provides a way to control low-level details. Example:
 * </p>
 * <blockquote><pre>
 * CoordinateOperationFactory myFactory = &hellip;
 * RenderingHints hints = new RenderingHints(Hints.{@link #COORDINATE_OPERATION_FACTORY}, myFactory);
 * GridCoverageProcessor processor = new GridCoverageProcessor2D(hints);
 * </pre></blockquote>
 * <p>
 * Any hint mentioned by this interface is considered to be API, failure to make use of a hint by
 * a geotools factory implementation is considered a bug (as it will prevent the use of this library
 * for application specific tasks).
 * </p>
 * <p>
 * When hints are used in conjuction with the Factory service discovery mechanism we have the
 * complete geotools plugin system. By using hints to allow application code to effect service
 * discovery we allow client code to retarget the geotools library for their needs.
 * </p>
 * <p>
 * While this works in practice for services which we control (like Feature creation), we also make
 * use of other services.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Hints extends RenderingHints {
    /**
     * Hint for the {@link com.vividsolutions.jts.geom.GeometryFactory} instance to use.
     *
     * @see org.geotools.geometry.jts.FactoryFinder#getGeometryFactory
     */
    public static final Key JTS_GEOMETRY_FACTORY =
            new Key("com.vividsolutions.jts.geom.GeometryFactory");

    /**
     * Hint for the {@link com.vividsolutions.jts.geom.CoordinateSequenceFactory} instance to use.
     *
     * @see org.geotools.geometry.jts.FactoryFinder#getCoordinateSequenceFactory
     */
    public static final Key JTS_COORDINATE_SEQUENCE_FACTORY =
            new Key("com.vividsolutions.jts.geom.CoordinateSequenceFactory");

    /**
     * Hint for the {@link com.vividsolutions.jts.geom.PrecisionModel} instance to use.
     *
     * @see org.geotools.geometry.jts.FactoryFinder#getPrecisionModel
     */
    public static final Key JTS_PRECISION_MODEL =
            new Key("com.vividsolutions.jts.geom.PrecisionModel");

    /**
     * The spatial reference ID for {@link com.vividsolutions.jts.geom.GeometryFactory}.
     *
     * @see org.geotools.geometry.jts.FactoryFinder#getGeometryFactory
     */
    public static final Key JTS_SRID =
            new Key(Integer.class);

    /**
     * Hint for the {@link org.opengis.referencing.crs.CRSAuthorityFactory} instance to use.
     *
     * @see org.geotools.referencing.FactoryFinder#getCRSAuthorityFactory
     */
    public static final Key CRS_AUTHORITY_FACTORY =
            new Key("org.opengis.referencing.crs.CRSAuthorityFactory");

    /**
     * Hint for the {@link org.opengis.referencing.cs.CSAuthorityFactory} instance to use.
     *
     * @see org.geotools.referencing.FactoryFinder#getCSAuthorityFactory
     */
    public static final Key CS_AUTHORITY_FACTORY =
            new Key("org.opengis.referencing.cs.CSAuthorityFactory");

    /**
     * Hint for the {@link org.opengis.referencing.datum.DatumAuthorityFactory} instance to use.
     *
     * @see org.geotools.referencing.FactoryFinder#getDatumAuthorityFactory
     */
    public static final Key DATUM_AUTHORITY_FACTORY =
            new Key("org.opengis.referencing.datum.DatumAuthorityFactory");

    /**
     * Hint for the {@link org.opengis.referencing.crs.CRSFactory} instance to use.
     *
     * @see org.geotools.referencing.FactoryFinder#getCRSFactory
     */
    public static final Key CRS_FACTORY =
            new Key("org.opengis.referencing.crs.CRSFactory");

    /**
     * Hint for the {@link org.opengis.referencing.cs.CSFactory} instance to use.
     *
     * @see org.geotools.referencing.FactoryFinder#getCSFactory
     */
    public static final Key CS_FACTORY =
            new Key("org.opengis.referencing.cs.CSFactory");

    /**
     * Hint for the {@link org.opengis.referencing.datum.DatumFactory} instance to use.
     *
     * @see org.geotools.referencing.FactoryFinder#getDatumFactory
     */
    public static final Key DATUM_FACTORY =
            new Key("org.opengis.referencing.datum.DatumFactory");

    /**
     * Hint for the {@link org.opengis.referencing.operation.CoordinateOperationFactory}
     * instance to use.
     *
     * @see org.geotools.referencing.FactoryFinder#getCoordinateOperationFactory
     */
    public static final Key COORDINATE_OPERATION_FACTORY =
            new Key("org.opengis.referencing.operation.CoordinateOperationFactory");

    /**
     * Hint for the {@link org.opengis.referencing.operation.MathTransformFactory} instance to use.
     *
     * @see org.geotools.referencing.FactoryFinder#getMathTransformFactory
     */
    public static final Key MATH_TRANSFORM_FACTORY =
            new Key("org.opengis.referencing.operation.MathTransformFactory");

    /**
     * Hint for the {@link org.opengis.coverage.processing.GridCoverageProcessor} instance to use.
     */
    public static final Key GRID_COVERAGE_PROCESSOR =
            new Key("org.opengis.coverage.processing.GridCoverageProcessor");

    /**
     * Hint for the {@link javax.media.jai.JAI} instance to use.
     */
    public static final Key JAI_INSTANCE =
            new Key("javax.media.jai.JAI");

    /**
     * Hint for the {@link org.opengis.coverage.SampleDimensionType} to use.
     */
    public static final Key SAMPLE_DIMENSION_TYPE =
            new Key("org.opengis.coverage.SampleDimensionType");

    /**
     * Hint for the preferred datum shift method to use for coordinate operation.
     * Valid values are {@code "Molodenski"}, {@code "Abridged_Molodenski"} or {@code "Geocentric"}.
     * Other values may be supplied if a {@linkplain org.opengis.referencing.operation.MathTransform
     * math transform} exists for that name, but this is not guaranteed to work.
     *
     * @see org.geotools.referencing.FactoryFinder#getCoordinateOperationFactory
     */
    public static final Key DATUM_SHIFT_METHOD =
            new Key(String.class);

    /**
     * Tells if coordinate operations should be allowed even when a datum shift is required while
     * no method is found applicable. It may be for example that no
     * {@linkplain org.geotools.referencing.datum.BursaWolfParameters Bursa Wolf parameters} were
     * found for a datum shift. The default value is {@link Boolean#FALSE FALSE}, which means that
     * {@linkplain org.geotools.referencing.operation.CoordinateOperationFactory coordinate
     * operation factory} throws an exception if such a case occurs. If this hint is set to
     * {@code TRUE}, then the user is strongly encouraged to check the
     * {@linkplain org.opengis.referencing.operation.CoordinateOperation#getPositionalAccuracy
     * positional accuracy} for every transformation created. If the set of positional accuracy
     * contains {@link org.geotools.metadata.iso.quality.PositionalAccuracy#DATUM_SHIFT_OMITTED
     * DATUM_SHIFT_OMITTED}, this means that an "ellipsoid shift" were applied without real
     * datum shift method available, and the transformed coordinates may have one kilometer
     * error. The application should warn the user (e.g. popup a message dialog box) in such
     * case.
     *
     * @see org.geotools.referencing.FactoryFinder#getCoordinateOperationFactory
     */
    public static final Key LENIENT_DATUM_SHIFT =
            new Key(Boolean.class);

    /**
     * Constructs a new object with keys and values initialized
     * from the specified map (which may be null).
     *
     * @param hints A map of key/value pairs to initialize the hints,
     *        or {@code null} if the object should be empty.
     */
    public Hints(final Map hints) {
        super(hints);
    }

    /**
     * Constructs a new object with the specified key/value pair.
     *
     * @param key   The key of the particular hint property.
     * @param value The value of the hint property specified with {@code key}.
     */
    public Hints(final RenderingHints.Key key, final Object value) {
        super(key, value);
    }

    /**
     * The type for keys used to control various aspects of the factory creation. Factory creation
     * impacts rendering (which is why extending {@linkplain java.awt.RenderingHints.Key rendering
     * key} is not a complete non-sense), but may impact other aspects of an application as well.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static final class Key extends RenderingHints.Key {
        /**
         * The number of key created up to date.
         */
        private static volatile int count;

        /**
         * The class name for {@link #valueClass}.
         */
        private final String className;

        /**
         * Base class of all values for this key. Will be created from {@link #className}
         * only when first required, in order to avoid too early class loading. This is
         * significant for the {@link #JAI_INSTANCE} key for example, in order to avoid
         * JAI dependencies in applications that do not need it.
         */
        private transient Class valueClass;

        /**
         * Constructs a new key for values of the given class.
         *
         * @param classe The base class for all valid values.
         */
        Key(final Class classe) {
            this(classe.getName());
            valueClass = classe;
        }

        /**
         * Constructs a new key for values of the given class. The class is specified
         * by name instead of a {@link Class} object. This allows to defer class loading
         * until needed.
         *
         * @param className Name of base class for all valid values.
         */
        Key(final String className) {
            super(count++);
            this.className = className;
            try {
                assert !Class.forName(className).isPrimitive() : className;
            } catch (ClassNotFoundException exception) {
                throw new AssertionError(exception);
            }
        }

        /**
         * Returns the expected class for values stored under this key.
         */
        public Class getValueClass() {
            if (valueClass == null) try {
                valueClass = Class.forName(className);
            } catch (ClassNotFoundException exception) {
                Utilities.unexpectedException("org.geotools.factory", "Hints.Key",
                                              "isCompatibleValue", exception);
                valueClass = Object.class;
            }
            return valueClass;
        }

        /**
         * Returns {@code true} if the specified object is a valid value for this key.
         * This method checks if the specified value is non-null and is one of the following:
         * <p>
         * <ul>
         *   <li>An instance of the {@linkplain #getValueClass expected value class}.</li>
         *   <li>A {@link Class} assignable to the expected value class.</li>
         *   <li>An array of {@code Class} objects assignable to the expected value class.</li>
         * </ul>
         *
         * @param  value The object to test for validity.
         * @return {@code true} if the value is valid; {@code false} otherwise.
         */
        public boolean isCompatibleValue(final Object value) {
            if (value == null) {
                return false;
            }
            if (value instanceof Class[]) {
                final Class[] types = (Class[]) value;
                for (int i=0; i<types.length; i++) {
                    if (!isCompatibleValue(types[i])) {
                        return false;
                    }
                }
                return types.length != 0;
            }
            final Class type;
            if (value instanceof Class) {
                type = (Class) value;
            } else {
                type = value.getClass();
            }
            return getValueClass().isAssignableFrom(type);
        }
    }
}