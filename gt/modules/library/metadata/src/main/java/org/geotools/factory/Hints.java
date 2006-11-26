/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.factory;

// J2SE dependencies
import java.util.Map;
import java.util.HashMap;
import java.awt.RenderingHints;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

// Geotools Dependencies
import org.geotools.util.Logging;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;


/**
 * A set of hints providing control on factories to be used.
 * <p>
 * Those hints are typically used by renderers or
 * {@linkplain org.opengis.coverage.processing.GridCoverageProcessor grid coverage processors}
 * for example. They provides a way to control low-level details. Example:
 * <p>
 * <blockquote>
 * 
 * <pre>
 *   CoordinateOperationFactory myFactory = &amp;hellip
 *   RenderingHints hints = new RenderingHints(Hints.{@link #COORDINATE_OPERATION_FACTORY}, myFactory);
 *   AbstractProcessor processor = new DefaultProcessor(hints);
 * </pre>
 * 
 * </blockquote>
 * <p>
 * Any hint mentioned by this interface is considered to be API, failure to make
 * use of a hint by a geotools factory implementation is considered a bug (as it
 * will prevent the use of this library for application specific tasks).
 * <p>
 * When hints are used in conjuction with the Factory service discovery
 * mechanism we have the complete geotools plugin system. By using hints to
 * allow application code to effect service discovery we allow client code to
 * retarget the geotools library for their needs.
 * <p>
 * While this works in practice for services which we control (like Feature
 * creation), we also make use of other services.
 * 
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Hints extends RenderingHints {
    /**
     * The {@link com.vividsolutions.jts.geom.GeometryFactory} instance to use.
     * 
     * @see org.geotools.geometry.jts.FactoryFinder#getGeometryFactory
     */
    public static final Key JTS_GEOMETRY_FACTORY = new Key(
            "com.vividsolutions.jts.geom.GeometryFactory");

    /**
     * The {@link com.vividsolutions.jts.geom.CoordinateSequenceFactory} instance to use.
     * 
     * @see org.geotools.geometry.jts.FactoryFinder#getCoordinateSequenceFactory
     */
    public static final Key JTS_COORDINATE_SEQUENCE_FACTORY = new Key(
            "com.vividsolutions.jts.geom.CoordinateSequenceFactory");

    /**
     * The {@link com.vividsolutions.jts.geom.PrecisionModel} instance to use.
     * 
     * @see org.geotools.geometry.jts.FactoryFinder#getPrecisionModel
     */
    public static final Key JTS_PRECISION_MODEL = new Key(
            "com.vividsolutions.jts.geom.PrecisionModel");

    /**
     * The spatial reference ID for {@link com.vividsolutions.jts.geom.GeometryFactory}.
     * 
     * @see org.geotools.geometry.jts.FactoryFinder#getGeometryFactory
     */
    public static final Key JTS_SRID = new Key(Integer.class);

    /**
     * The {@link org.opengis.referencing.crs.CRSAuthorityFactory} instance to use.
     * 
     * @see org.geotools.referencing.FactoryFinder#getCRSAuthorityFactory
     */
    public static final Key CRS_AUTHORITY_FACTORY = new Key(
            "org.opengis.referencing.crs.CRSAuthorityFactory");

    /**
     * The {@link org.opengis.referencing.cs.CSAuthorityFactory} instance to use.
     * 
     * @see org.geotools.referencing.FactoryFinder#getCSAuthorityFactory
     */
    public static final Key CS_AUTHORITY_FACTORY = new Key(
            "org.opengis.referencing.cs.CSAuthorityFactory");

    /**
     * The {@link org.opengis.referencing.datum.DatumAuthorityFactory} instance to use.
     * 
     * @see org.geotools.referencing.FactoryFinder#getDatumAuthorityFactory
     */
    public static final Key DATUM_AUTHORITY_FACTORY = new Key(
            "org.opengis.referencing.datum.DatumAuthorityFactory");

    /**
     * The {@link org.opengis.referencing.crs.CRSFactory} instance to use.
     * 
     * @see org.geotools.referencing.FactoryFinder#getCRSFactory
     */
    public static final Key CRS_FACTORY = new Key(
            "org.opengis.referencing.crs.CRSFactory");

    /**
     * The {@link org.opengis.referencing.cs.CSFactory} instance to use.
     * 
     * @see org.geotools.referencing.FactoryFinder#getCSFactory
     */
    public static final Key CS_FACTORY = new Key(
            "org.opengis.referencing.cs.CSFactory");

    /**
     * The {@link org.opengis.referencing.datum.DatumFactory} instance to use.
     * 
     * @see org.geotools.referencing.FactoryFinder#getDatumFactory
     */
    public static final Key DATUM_FACTORY = new Key(
            "org.opengis.referencing.datum.DatumFactory");

    /**
     * The {@link org.opengis.referencing.operation.CoordinateOperationFactory} instance to use.
     * 
     * @see org.geotools.referencing.FactoryFinder#getCoordinateOperationFactory
     */
    public static final Key COORDINATE_OPERATION_FACTORY = new Key(
            "org.opengis.referencing.operation.CoordinateOperationFactory");

    /**
     * The {@link org.opengis.referencing.operation.CoordinateOperationAuthorityFactory} instance
     * to use.
     * 
     * @see org.geotools.referencing.FactoryFinder#getCoordinateOperationAuthorityFactory
     */
    public static final Key COORDINATE_OPERATION_AUTHORITY_FACTORY = new Key(
            "org.opengis.referencing.operation.CoordinateOperationAuthorityFactory");

    /**
     * The {@link org.opengis.referencing.operation.MathTransformFactory} instance to use.
     * 
     * @see org.geotools.referencing.FactoryFinder#getMathTransformFactory
     */
    public static final Key MATH_TRANSFORM_FACTORY = new Key(
            "org.opengis.referencing.operation.MathTransformFactory");

    /**
     * The {@link org.geotools.styling.StyleFactory} instance to use.
     * 
     * @see org.geotools.factory.CommonFactoryFinder#getStyleFactory
     *
     * @since 2.4
     */
    public static final Key STYLE_FACTORY = new Key(
            "org.geotools.styling.StyleFactory");

    /**
     * The {@link org.opengis.filter.FilterFactory} instance to use.
     * 
     * @see org.geotools.factory.CommonFactoryFinder#getFilterFactory
     *
     * @since 2.4
     */
    public static final Key FILTER_FACTORY = new Key(
            "org.opengis.filter.FilterFactory");

    /**
     * The {@link org.opengis.coverage.processing.GridCoverageProcessor} instance to use.
     * 
     * @deprecated The {@code GridCoverageProcessor} interface is not yet
     *             stable. Avoid dependencies if possible.
     */
    public static final Key GRID_COVERAGE_PROCESSOR = new Key(
            "java.lang.Object");

    // TODO new Key("org.opengis.coverage.processing.GridCoverageProcessor");

    /**
     * The {@linkplain javax.media.jai.tilecodec.TileEncoder tile encoder} name
     * (as a {@link String} value) to use during serialization of image data in
     * a {@link org.geotools.coverage.grid.GridCoverage2D} object. This encoding
     * is given to the {@link javax.media.jai.remote.SerializableRenderedImage}
     * constructor. Valid values include (but is not limited to) {@code "raw"},
     * {@code "gzip"} and {@code "jpeg"}.
     * <p>
     * <strong>Note:</strong> We recommand to avoid the {@code "jpeg"} codec
     * for grid coverages.
     * 
     * @see org.geotools.coverage.FactoryFinder#getGridCoverageFactory
     * 
     * @since 2.3
     */
    public static final Key TILE_ENCODING = new Key("java.lang.String");

    /**
     * The {@link javax.media.jai.JAI} instance to use.
     */
    public static final Key JAI_INSTANCE = new Key("javax.media.jai.JAI");

    /**
     * The {@link org.opengis.coverage.SampleDimensionType} to use.
     */
    public static final Key SAMPLE_DIMENSION_TYPE = new Key(
            "org.opengis.coverage.SampleDimensionType");

    /**
     * The default {@link org.opengis.referencing.crs.CoordinateReferenceSystem}
     * to use. This is used by some factories capable to provide a default CRS
     * when no one were explicitly specified by the user.
     * 
     * @since 2.2
     */
    public static final Key DEFAULT_COORDINATE_REFERENCE_SYSTEM = new Key(
            "org.opengis.referencing.crs.CoordinateReferenceSystem");

    /**
     * The preferred datum shift method to use for
     * {@linkplain org.opengis.referencing.operation.CoordinateOperation coordinate operations}.
     * Valid values are {@code "Molodenski"}, {@code "Abridged_Molodenski"} or {@code "Geocentric"}.
     * Other values may be supplied if a {@linkplain org.opengis.referencing.operation.MathTransform
     * math transform} exists for that name, but this is not guaranteed to work.
     * 
     * @see org.geotools.referencing.FactoryFinder#getCoordinateOperationFactory
     */
    public static final Key DATUM_SHIFT_METHOD = new Key(String.class);

    /**
     * Tells if {@linkplain org.opengis.referencing.operation.CoordinateOperation coordinate
     * operations} should be allowed even when a datum shift is required while no method is
     * found applicable. It may be for example that no
     * {@linkplain org.geotools.referencing.datum.BursaWolfParameters Bursa Wolf parameters}
     * were found for a datum shift. The default value is {@link Boolean#FALSE FALSE}, which means
     * that {@linkplain org.geotools.referencing.operation.DefaultCoordinateOperationFactory
     * coordinate operation factory} throws an exception if such a case occurs. If this hint is
     * set to {@code TRUE}, then the user is strongly encouraged to check the
     * {@linkplain org.opengis.referencing.operation.CoordinateOperation#getPositionalAccuracy
     * positional accuracy} for every transformation created. If the set of positional accuracy
     * contains {@link org.geotools.metadata.iso.quality.PositionalAccuracyImpl#DATUM_SHIFT_OMITTED
     * DATUM_SHIFT_OMITTED}, this means that an "ellipsoid shift" were applied without real datum
     * shift method available, and the transformed coordinates may have one kilometer error. The
     * application should warn the user (e.g. popup a message dialog box) in such case.
     * 
     * @see org.geotools.referencing.FactoryFinder#getCoordinateOperationFactory
     */
    public static final Key LENIENT_DATUM_SHIFT = new Key(Boolean.class);

    /**
     * Tells if the {@linkplain org.opengis.referencing.cs.CoordinateSystem coordinate systems}
     * created by an {@linkplain org.opengis.referencing.cs.CSAuthorityFactory authority factory}
     * should be forced to (<var>longitude</var>,<var>latitude</var>) axis order. This hint is
     * especially useful for creating
     * {@linkplan org.opengis.referencing.crs.CoordinateReferenceSystem coordinate reference system}
     * objects from <A HREF="http://www.epsg.org">EPSG</A> codes. Most
     * {@linkplan org.opengis.referencing.crs.GeographicCRS geographic CRS} defined in the EPSG
     * database use (<var>latitude</var>,<var>longitude</var>) axis order. Unfortunatly, many data
     * sources available in the world uses the opposite axis order and still claim to use a CRS
     * described by an EPSG code. This hint allows to handle such data.
     * <p>
     * This hint shall be passed to the
     * <code>{@linkplain org.geotools.referencing.FactoryFinder#getCRSAuthorityFactory
     * FactoryFinder.getCRSAuthorityFactory}(...)</code> method. Whatever this hint is supported
     * or not is authority dependent. In the default Geotools configuration, this hint is supported
     * for the {@code "EPSG"} authority.
     * <p>
     * If this hint is not provided, then the default value depends on many factors including
     * {@linkplain System#getProperties system properties} and plugins available in the classpath.
     * In Geotools implementation, the default value is usually {@link Boolean#FALSE FALSE} with
     * one exception: If the <code>{@value
     * org.geotools.referencing.factory.epsg.LongitudeFirstFactory#SYSTEM_DEFAULT_KEY}</code>
     * system property is set to {@code true}, then the default value is {@code true} at least
     * for the {@linkplain org.geotools.referencing.factory.epsg.DefaultFactory default EPSG
     * factory}.
     * <p>
     * If both the above-cited system property and this hint are provided, then
     * this hint has precedence. This allow axis order control on a data store
     * basis, and keep the system-wide property as the default value only for
     * cases where axis order is unspecified.
     * 
     * @see org.geotools.referencing.FactoryFinder#getCSFactory
     * @see org.geotools.referencing.FactoryFinder#getCRSFactory
     * @see org.geotools.referencing.factory.OrderedAxisAuthorityFactory
     * @see org.geotools.referencing.factory.epsg.LongitudeFirstFactory
     * @tutorial http://docs.codehaus.org/display/GEOTOOLS/The+axis+order+issue
     * 
     * @since 2.3
     */
    public static final Key FORCE_LONGITUDE_FIRST_AXIS_ORDER = new Key(
            Boolean.class);

    /**
     * Tells if the {@linkplain org.opengis.referencing.cs.CoordinateSystem coordinate systems}
     * created by an {@linkplain org.opengis.referencing.cs.CSAuthorityFactory authority factory}
     * should be forced to standard
     * {@linkplain org.opengis.referencing.cs.CoordinateSystemAxis#getDirection axis directions}.
     * If {@code true}, then {@linkplain org.opengis.referencing.cs.AxisDirection#SOUTH South} axis
     * directions are forced to {@linkplain org.opengis.referencing.cs.AxisDirection#NORTH North},
     * {@linkplain org.opengis.referencing.cs.AxisDirection#WEST West} axis directions are forced to
     * {@linkplain org.opengis.referencing.cs.AxisDirection#EAST East}, <cite>etc.</cite>
     * If {@code false}, then the axis directions are left unchanged.
     * <p>
     * This hint shall be passed to the
     * <code>{@linkplain org.geotools.referencing.FactoryFinder#getCRSAuthorityFactory
     * FactoryFinder.getCRSAuthorityFactory}(...)</code>
     * method. Whatever this hint is supported or not is authority dependent.
     * 
     * @see org.geotools.referencing.FactoryFinder#getCSFactory
     * @see org.geotools.referencing.FactoryFinder#getCRSFactory
     * @see org.geotools.referencing.factory.OrderedAxisAuthorityFactory
     * 
     * @since 2.3
     */
    public static final Key FORCE_STANDARD_AXIS_DIRECTIONS = new Key(
            Boolean.class);

    /**
     * Tells if the {@linkplain org.opengis.referencing.cs.CoordinateSystem coordinate systems}
     * created by an {@linkplain org.opengis.referencing.cs.CSAuthorityFactory authority factory}
     * should be forced to standard
     * {@linkplain org.opengis.referencing.cs.CoordinateSystemAxis#getUnit axis units}.
     * If {@code true}, then all angular units are forced to degrees and linear units to meters.
     * If {@code false}, then the axis units are left unchanged.
     * <p>
     * This hint shall be passed to the
     * <code>{@linkplain org.geotools.referencing.FactoryFinder#getCRSAuthorityFactory
     * FactoryFinder.getCRSAuthorityFactory}(...)</code> method. Whatever this hint is
     * supported or not is authority dependent.
     * 
     * @see org.geotools.referencing.FactoryFinder#getCSFactory
     * @see org.geotools.referencing.FactoryFinder#getCRSFactory
     * @see org.geotools.referencing.factory.OrderedAxisAuthorityFactory
     * 
     * @since 2.3
     */
    public static final Key FORCE_STANDARD_AXIS_UNITS = new Key(Boolean.class);

    /**
     * Tells to the {@link GridCoverageReader} instances to ignore the built-in
     * overviews when creating a {@link GridCoverage} object during a read. This
     * hints also implied that no decimation on reading is performed.
     */
    public static final Key IGNORE_COVERAGE_OVERVIEW = new Key(Boolean.class);

    /**
     * Constructs a new object with keys and values initialized from the
     * specified map (which may be null).
     * 
     * @param hints
     *            A map of key/value pairs to initialize the hints, or
     *            {@code null} if the object should be empty.
     */
    public Hints(final Map hints) {
        super(hints);
    }

    /**
     * Constructs a new object with the specified key/value pair.
     * 
     * @param key
     *            The key of the particular hint property.
     * @param value
     *            The value of the hint property specified with {@code key}.
     */
    public Hints(final RenderingHints.Key key, final Object value) {
        super(key, value);
    }

    /**
     * The type for keys used to control various aspects of the factory
     * creation. Factory creation impacts rendering (which is why extending
     * {@linkplain java.awt.RenderingHints.Key rendering key} is not a complete
     * non-sense), but may impact other aspects of an application as well.
     * 
     * @since 2.1
     * @source $URL$
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
         * Base class of all values for this key. Will be created from {@link #className} only when
         * first required, in order to avoid too early class loading. This is significant for the
         * {@link #JAI_INSTANCE} key for example, in order to avoid JAI dependencies in applications
         * that do not need it.
         */
        private transient Class valueClass;

        /**
         * Constructs a new key for values of the given class.
         * 
         * @param classe
         *            The base class for all valid values.
         */
        public Key(final Class classe) {
            this(classe.getName());
            valueClass = classe;
        }

        /**
         * Constructs a new key for values of the given class. The class is
         * specified by name instead of a {@link Class} object. This allows to
         * defer class loading until needed.
         * 
         * @param className
         *            Name of base class for all valid values.
         */
        Key(final String className) {
            super(count++);
            this.className = className;
        }

        /**
         * Returns the expected class for values stored under this key.
         */
        public Class getValueClass() {
            if (valueClass == null) {
                try {
                    valueClass = Class.forName(className);
                } catch (ClassNotFoundException exception) {
                    Logging.unexpectedException("org.geotools.factory",
                            "Hints.Key", "isCompatibleValue", exception);
                    valueClass = Object.class;
                }
            }
            return valueClass;
        }

        /**
         * Returns {@code true} if the specified object is a valid value for
         * this key. This method checks if the specified value is non-null and
         * is one of the following:
         * <p>
         * <ul>
         * <li>An instance of the
         * {@linkplain #getValueClass expected value class}.</li>
         * <li>A {@link Class} assignable to the expected value class.</li>
         * <li>An array of {@code Class} objects assignable to the expected
         * value class.</li>
         * </ul>
         * 
         * @param value
         *            The object to test for validity.
         * @return {@code true} if the value is valid; {@code false} otherwise.
         */
        public boolean isCompatibleValue(final Object value) {
            if (value == null) {
                return false;
            }
            if (value instanceof Class[]) {
                final Class[] types = (Class[]) value;
                for (int i = 0; i < types.length; i++) {
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

        /**
         * Returns a string representation of this key. The string
         * representation is mostly for debugging purpose. The default
         * implementation tries to infer the key name using reflection.
         */
        public String toString() {
            int t = 0;
            while (true) {
                final Class type;
                switch (t++) {
                case 0:
                    type = Hints.class;
                    break;
                case 1:
                    type = getValueClass();
                    break;
                default:
                    return super.toString();
                }
                final Field[] fields = type.getFields();
                for (int i = 0; i < fields.length; i++) {
                    final Field f = fields[i];
                    if (Modifier.isStatic(f.getModifiers())) {
                        final Object v;
                        try {
                            v = f.get(null);
                        } catch (IllegalAccessException e) {
                            continue;
                        }
                        if (v == this) {
                            return f.getName();
                        }
                    }
                }
            }
        }
    }
}
