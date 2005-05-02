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
     */
    public static final Key JTS_GEOMETRY_FACTORY =
            new Key("com.vividsolutions.jts.geom.GeometryFactory");

    /**
     * Hint for the {@link com.vividsolutions.jts.geom.CoordinateSequenceFactory} instance to use.
     */
    public static final Key JTS_COORDINATE_SEQUENCE_FACTORY =
            new Key("com.vividsolutions.jts.geom.CoordinateSequenceFactory");

    /**
     * Hint for the {@link com.vividsolutions.jts.geom.PrecisionModel} instance to use.
     */
    public static final Key JTS_PRECISION_MODEL =
            new Key("com.vividsolutions.jts.geom.PrecisionModel");

    /**
     * The spatial reference ID for {@link com.vividsolutions.jts.geom.GeometryFactory}.
     */
    public static final Key JTS_SRID =
            new Key("java.lang.Integer");

    /**
     * Hint for the {@link CRSAuthorityFactory} instance to use.
     */
    public static final Key CRS_AUTHORITY_FACTORY =
            new Key("org.opengis.referencing.crs.CRSAuthorityFactory");

    /**
     * Hint for the {@link CSAuthorityFactory} instance to use.
     */
    public static final Key CS_AUTHORITY_FACTORY =
            new Key("org.opengis.referencing.cs.CSAuthorityFactory");

    /**
     * Hint for the {@link DatumAuthorityFactory} instance to use.
     */
    public static final Key DATUM_AUTHORITY_FACTORY =
            new Key("org.opengis.referencing.datum.DatumAuthorityFactory");

    /**
     * Hint for the {@link CRSFactory} instance to use.
     */
    public static final Key CRS_FACTORY =
            new Key("org.opengis.referencing.crs.CRSFactory");

    /**
     * Hint for the {@link CSFactory} instance to use.
     */
    public static final Key CS_FACTORY =
            new Key("org.opengis.referencing.cs.CSFactory");

    /**
     * Hint for the {@link DatumFactory} instance to use.
     */
    public static final Key DATUM_FACTORY =
            new Key("org.opengis.referencing.datum.DatumFactory");

    /**
     * Hint for the {@link CoordinateOperationFactory} instance to use.
     */
    public static final Key COORDINATE_OPERATION_FACTORY =
            new Key("org.opengis.referencing.operation.CoordinateOperationFactory");

    /**
     * Hint for the {@link MathTransformFactory} instance to use.
     */
    public static final Key MATH_TRANSFORM_FACTORY =
            new Key("org.opengis.referencing.operation.MathTransformFactory");

    /**
     * Hint for the {@link GridCoverageProcessor} instance to use.
     */
    public static final Key GRID_COVERAGE_PROCESSOR =
            new Key("org.opengis.coverage.processing.GridCoverageProcessor");

    /**
     * Hint for the {@link JAI} instance to use.
     */
    public static final Key JAI_INSTANCE =
            new Key("javax.media.jai.JAI");

    /**
     * Hint for the {@link SampleDimensionType} to use.
     */
    public static final Key SAMPLE_DIMENSION_TYPE =
            new Key("org.opengis.coverage.SampleDimensionType");

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
         * A map of hints created up to date, referenced by their
         * {@linkplain #getValueClass value class} name.
         */
        private static final Map/*<String,Key>*/ byClassName = new HashMap();

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
         * Constructs a new key.
         *
         * NOTE: if this constructor become public, then all usage of {@link #byClassName} will
         *       need to be synchronized. This synchronization is hard to do in the constructor
         *       because 'super(...)' references the map.
         *
         * @param className Name of base class for all valid values.
         */
        Key(final String className) {
            super(byClassName.size());
            this.className = className;
            try {
                assert !Class.forName(className).isPrimitive() : className;
            } catch (ClassNotFoundException exception) {
                throw new AssertionError(exception);
            }
            final Key previous = (Key) byClassName.put(className, this);
            if (previous != null) {
                throw new IllegalArgumentException(className);
            }
        }

        /**
         * Returns the hint key for the specified category.
         *
         * @deprecated This method need to be removed. The key should be explicitly specified
         *             as an argument to {@link FactoryRegistry#getServiceProvider} instead.
         */
        static Key getKeyForCategory(final Class type) {
            return (Key) byClassName.get(type.getName());
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
         * <br><br>
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