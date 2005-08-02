/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Managment Committee (PMC)
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
package org.geotools.openoffice;

// J2SE dependencies
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collection;
import java.text.Format;
import java.text.ParseException;

// OpenOffice dependencies
import com.sun.star.lang.Locale;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.comp.loader.FactoryHelper;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.beans.XPropertySet;

// GeoAPI dependencies
import org.opengis.util.InternationalString;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.referencing.ReferenceSystem;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeneralDerivedCRS;
import org.opengis.referencing.operation.Operation;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.metadata.quality.PositionalAccuracy;
import org.opengis.metadata.quality.QuantitativeResult;
import org.opengis.metadata.quality.Result;

// Geotools dependencies
import org.geotools.measure.Angle;
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.measure.AngleFormat;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.factory.AbstractAuthorityFactory;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;


/**
 * Exports methods from the {@link org.geotools.referencing} package as
 * <A HREF="http://www.openoffice.org">OpenOffice</A> add-ins.
 *
 * @since 2.2
 * @version $Id$
 * @author Richard Deplanque
 * @author Martin Desruisseaux
 */
public final class Referencing extends Formulas implements XReferencing {
    /**
     * The name for the registration of this component.<BR>
     * <strong>NOTE:</strong> OpenOffice expects a field with exactly that name; do not rename!
     */
    private static final String __serviceName = "org.geotools.openoffice.Referencing";

    /**
     * The name of the provided service.
     */
    private static final String ADDIN_SERVICE = "com.sun.star.sheet.AddIn";

    /**
     * The authority used in this implementation.
     */
    private static final String AUTHORITY = "EPSG";

    /**
     * The pattern for the {@link #angleFormat}. Used in order to avoid creating
     * new formats when the pattern didn't changed.
     */
    private transient String anglePattern;

    /**
     * The format to use for formatting angles. Will be created only when first needed.
     */
    private transient Format angleFormat;

    /**
     * The format to use for parsing angles. Will be created only when first needed.
     */
    private transient Format angleParser;

    /**
     * The CRS authority factory. Will be created only when first needed.
     */
    private transient CRSAuthorityFactory crsFactory;

    /**
     * The coordinate operation factory. Will be created only when first needed.
     */
    private transient CoordinateOperationFactory opFactory;

    /**
     * The last geodetic calculator used, or {@code null} if none. Cached for better
     * performance when many orthodromic distances are computed on the same ellipsoid.
     */
    private transient GeodeticCalculator calculator;

    /**
     * The CRS authority code used for {@link #calculator} setup,
     * or {@code null} if not yet defined.
     */
    private transient String calculatorCRS;

    /**
     * Constructs a default implementation of {@code XReferencing} interface.
     */
    public Referencing() {
        methods.put("getValueAngle", new MethodInfo("Text", "VALUE.ANGLE",
            "Converts text in degrees-minutes-seconds to an angle in decimal degrees.",
            new String[] {
                "xOptions",   "Provided by OpenOffice.",
                "text",       "The text to be converted to an angle."
        }));
        methods.put("getTextAngle", new MethodInfo("Text", "TEXT.ANGLE",
            "Converts an angle to text according to a given format.",
            new String[] {
                "xOptions",   "Provided by OpenOffice.",
                "value",      "The angle value (in decimal degrees) to be converted.",
                "pattern",    "The text that describes the format (example: \"D°MM.m'\")."
        }));
        methods.put("getTextLongitude", new MethodInfo("Text", "TEXT.LONGITUDE",
            "Converts a longitude to text according to a given format.",
            new String[] {
                "xOptions",   "Provided by OpenOffice.",
                "value",      "The longitude value (in decimal degrees) to be converted.",
                "pattern",    "The text that describes the format (example: \"D°MM.m'\")."
        }));
        methods.put("getTextLatitude", new MethodInfo("Text", "TEXT.LATITUDE",
            "Converts a latitude to text according to a given format.",
            new String[] {
                "xOptions",   "Provided by OpenOffice.",
                "value",      "The latitude value (in decimal degrees) to be converted.",
                "pattern",    "The text that describes the format (example: \"D°MM.m'\")."
        }));
        methods.put("getDescription", new MethodInfo("Referencing", "CRS.DESCRIPTION",
            "Returns a description for an object identified by the given authority code.",
            new String[] {
                "xOptions",   "Provided by OpenOffice.",
                "code",       "The code allocated by authority."
        }));
        methods.put("getScope", new MethodInfo("Referencing", "CRS.SCOPE",
            "Returns the scope for an identified object.",
            new String[] {
                "xOptions",   "Provided by OpenOffice.",
                "code",       "The code allocated by authority."
        }));
        methods.put("getRemarks", new MethodInfo("Referencing", "CRS.REMARKS",
            "Returns the remarks for an identified object.",
            new String[] {
                "xOptions",   "Provided by OpenOffice.",
                "code",       "The code allocated by authority."
        }));
        methods.put("getAxis", new MethodInfo("Referencing", "CRS.AXIS",
            "Returns the axis name for the specified dimension in an identified object.",
            new String[] {
                "xOptions",   "Provided by OpenOffice.",
                "code",       "The code allocated by authority.",
                "dimension",  "The dimension (1, 2, ...)."
        }));
        methods.put("getWKT", new MethodInfo("Referencing", "CRS.WKT",
            "Returns the Well Know Text (WKT) for an identified object.",
            new String[] {
                "xOptions",   "Provided by OpenOffice.",
                "code",       "The code allocated by authority."
        }));
        methods.put("getParameter", new MethodInfo("Referencing", "CRS.PARAMETER",
            "Returns the value for a coordinate reference system parameter.",
            new String[] {
                "xOptions",   "Provided by OpenOffice.",
                "code",       "The code allocated by authority.",
                "parameter",  "The parameter name (e.g. \"False easting\")."
        }));
        methods.put("getAccuracy", new MethodInfo("Referencing", "CRS.ACCURACY",
            "Returns the accuracy of a transformation between two coordinate reference systems.",
            new String[] {
                "xOptions",    "Provided by OpenOffice.",
                "source CRS",  "The source coordinate reference system.",
                "target CRS",  "The target coordinate reference system."
        }));
        methods.put("getTransformedCoordinates", new MethodInfo("Referencing", "CRS.TRANSFORM",
            "Transform coordinates from the given source CRS to the given target CRS.",
            new String[] {
                "xOptions",    "Provided by OpenOffice.",
                "coordinates", "The coordinate values to transform.",
                "source CRS",  "The source coordinate reference system.",
                "target CRS",  "The target coordinate reference system."
        }));
        methods.put("getOrthodromicDistance", new MethodInfo("Referencing", "ORTHODROMIC.DISTANCE",
            "Computes the orthodromic distance and azimuth between two coordinates.",
            new String[] {
                "xOptions",    "Provided by OpenOffice.",
                "CRS",         "Authority code of the coordinate reference system.",
                "source",      "The source positions.",
                "target",      "The target positions."
        }));
        methods.put("getOrthodromicForward", new MethodInfo("Referencing", "ORTHODROMIC.FORWARD",
            "Computes the coordinates after a displacement of the specified distance.",
            new String[] {
                "xOptions",    "Provided by OpenOffice.",
                "CRS",         "Authority code of the coordinate reference system.",
                "source",      "The source positions.",
                "displacement","The distance and azimuth."
        }));
    }

    /**
     * Returns a factory for creating the service.
     * This method is called by the {@code com.sun.star.comp.loader.JavaLoader}; do not rename!
     *
     * @param   implementation The name of the implementation for which a service is desired.
     * @param   factories      The service manager to be used if needed.
     * @param   registry       The registry key
     * @return  A factory for creating the component.
     */
    public static XSingleServiceFactory __getServiceFactory(
                                        final String               implementation,
                                        final XMultiServiceFactory factories,
                                        final XRegistryKey         registry)
    {
        if (implementation.equals(Referencing.class.getName())) {
            return FactoryHelper.getServiceFactory(Referencing.class, __serviceName, factories, registry);
        }
        return null;
    }

    /**
     * Writes the service information into the given registry key.
     * This method is called by the {@code com.sun.star.comp.loader.JavaLoader}; do not rename!
     *
     * @param  registry     The registry key.
     * @return {@code true} if the operation succeeded.
     */
    public static boolean __writeRegistryServiceInfo(final XRegistryKey registry) {
        final String classname = Referencing.class.getName();
        return FactoryHelper.writeRegistryServiceInfo(classname, __serviceName, registry)
            && FactoryHelper.writeRegistryServiceInfo(classname, ADDIN_SERVICE, registry);
    }
    
    /**
     * The service name that can be used to create such an object by a factory.
     */
    public String getServiceName() {
        return __serviceName;
    }

    /**
     * Provides the supported service names of the implementation, including also
     * indirect service names.
     *
     * @return Sequence of service names that are supported.
     */
    public String[] getSupportedServiceNames() {
        return new String[] {ADDIN_SERVICE, __serviceName};
    }

    /**
     * Tests whether the specified service is supported, i.e. implemented by the implementation.
     *
     * @param  name Name of service to be tested.
     * @return {@code true} if the service is supported, {@code false} otherwise.
     */
    public boolean supportsService(final String name) {
        return name.equals(ADDIN_SERVICE) || name.equals(__serviceName);
    }

    /**
     * Sets the locale to be used by this object.
     */
    public void setLocale(final Locale locale) {
        anglePattern = null;
        angleFormat  = null;
        angleParser  = null;
        super.setLocale(locale);
    }




    // --------------------------------------------------------------------------------
    //     H E L P E R   M E T H O D S
    // --------------------------------------------------------------------------------

    /**
     * Returns the CRS authority factory.
     */
    private CRSAuthorityFactory crsFactory() {
        if (crsFactory == null) {
            crsFactory = FactoryFinder.getCRSAuthorityFactory(AUTHORITY, null);
        }
        return crsFactory;
    }

    /**
     * Returns the coordinate operation factory.
     */
    private CoordinateOperationFactory opFactory() {
        if (opFactory == null) {
            opFactory = FactoryFinder.getCoordinateOperationFactory(null);
        }
        return opFactory;
    }

    /**
     * Returns the geodetic calculator for the specified CRS, datum or ellipsoid.
     * This method cache the last calculator used for better performance when many
     * orthodromic distances are computed on the same ellipsoid.
     *
     * @throws FactoryException if the geodetic calculator can't be created.
     */
    private GeodeticCalculator getGeodeticCalculator(final String authorityCode)
            throws FactoryException
    {
        if (calculatorCRS==null || !calculatorCRS.equals(authorityCode)) {
            final IdentifiedObject object = crsFactory().createObject(authorityCode);
            if (object instanceof Ellipsoid) {
                calculator = new GeodeticCalculator((Ellipsoid) object);
            } else if (object instanceof GeodeticDatum) {
                calculator = new GeodeticCalculator(((GeodeticDatum) object).getEllipsoid());
            } else if (object instanceof CoordinateReferenceSystem) {
                calculator = new GeodeticCalculator((CoordinateReferenceSystem) object);
            } else {
                throw new FactoryException(Errors.format(
                                           ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM));
            }
            calculatorCRS = authorityCode;
        }
        return calculator;
    }




    // --------------------------------------------------------------------------------
    //     F O R M U L A   I M P L E M E N T A T I O N S
    // --------------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public double getValueAngle(final XPropertySet xOptions,
                                final String       text)
    {
        if (angleParser == null) {
            angleParser = new AngleFormat("D°MM'SS.s\"", getJavaLocale());
        }
        try {
            return ((Angle) angleParser.parseObject(text)).degrees();
        } catch (ParseException exception) {
            throw new IllegalArgumentException(getLocalizedMessage(exception));
        }
    }

    /**
     * Converts an angle to text according to a given format.
     *
     * @param value The angle value (in decimal degrees) to be converted.
     * @param pattern he text that describes the format (example: "D°MM.m'").
     */
    private String getTextAngle(final Object value,
                                final String pattern)
    {
        if (angleFormat == null) {
            angleFormat = new AngleFormat(pattern, getJavaLocale());
            anglePattern = pattern;
        } else if (!pattern.equals(anglePattern)) {
            ((AngleFormat) angleFormat).applyPattern(pattern);
            anglePattern = pattern;
        }
        return angleFormat.format(value);
    }

    /**
     * {@inheritDoc}
     */
    public String getTextAngle(final XPropertySet xOptions,
                               final double       value,
                               final String       pattern)
    {
        return getTextAngle(new Angle(value), pattern);
    }

    /**
     * {@inheritDoc}
     */
    public String getTextLongitude(final XPropertySet xOptions,
                                   final double       value,
                                   final String       pattern)
    {
        return getTextAngle(new Longitude(value), pattern);
    }

    /**
     * {@inheritDoc}
     */
    public String getTextLatitude(final XPropertySet xOptions,
                                  final double       value,
                                  final String       pattern)
    {
        return getTextAngle(new Latitude(value), pattern);
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription(final XPropertySet xOptions,
                                 final String authorityCode)
    {
        final InternationalString description;
        try {
            description = crsFactory().getDescriptionText(authorityCode);
        } catch (Exception exception) {
            return getLocalizedMessage(exception);
        }
        return (description!=null) ? description.toString(getJavaLocale()) : "(none)";
    }

    /**
     * {@inheritDoc}
     */
    public String getScope(final XPropertySet xOptions,
                           final String authorityCode)
    {
        final IdentifiedObject object;
        try {
            object = crsFactory().createObject(authorityCode);
        } catch (Exception exception) {
            return getLocalizedMessage(exception);
        }
        final InternationalString description;
        if (object instanceof Datum) {
            description = ((Datum) object).getScope();
        } else if (object instanceof ReferenceSystem) {
            description = ((ReferenceSystem) object).getScope();
        } else if (object instanceof CoordinateOperation) {
            description = ((CoordinateOperation) object).getScope();
        } else {
            description = null;
        }
        return (description!=null) ? description.toString(getJavaLocale()) : "(none)";
    }

    /**
     * {@inheritDoc}
     */
    public String getRemarks(final XPropertySet xOptions,
                             final String authorityCode)
    {
        final IdentifiedObject object;
        try {
            object = crsFactory().createObject(authorityCode);
        } catch (Exception exception) {
            return getLocalizedMessage(exception);
        }
        final InternationalString remarks = object.getRemarks();
        return (remarks!=null) ? remarks.toString(getJavaLocale()) : "(none)";
    }

    /**
     * {@inheritDoc}
     */
    public String getAxis(final XPropertySet xOptions,
                          final String  authorityCode,
                          final int         dimension)
    {
        CoordinateSystem cs;
        try {
            cs = crsFactory().createCoordinateReferenceSystem(authorityCode).getCoordinateSystem();
        } catch (Exception exception) {
            try {
                cs = FactoryFinder.getCSAuthorityFactory(AUTHORITY, null).createCoordinateSystem(authorityCode);
            } catch (Exception ignore) {
                // Ignore - we will report the previous exception instead.
            }
            return getLocalizedMessage(exception);
        }
        if (dimension>=1 && dimension<=cs.getDimension()) {
            return cs.getAxis(dimension-1).getName().getCode();
        } else {
            return Errors.format(ErrorKeys.INDEX_OUT_OF_BOUNDS_$1, new Integer(dimension));
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getWKT(final XPropertySet xOptions,
                         final String authorityCode)
    {
        final IdentifiedObject object;
        try {
            object = crsFactory().createObject(authorityCode);
        } catch (Exception exception) {
            return getLocalizedMessage(exception);
        }
        try {
            return object.toWKT();
        } catch (UnsupportedOperationException exception) {
            return getLocalizedMessage(exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object getParameter(final XPropertySet xOptions,
                               final String  authorityCode,
                               final String      parameter)
    {
        final IdentifiedObject object;
        try {
            object = crsFactory().createObject(authorityCode);
        } catch (FactoryException exception) {
            return getLocalizedMessage(exception);
        }
        final ParameterValueGroup parameters;
        if (object instanceof Operation) {
            parameters = ((Operation) object).getParameterValues();
        } else if (object instanceof GeneralDerivedCRS) {
            parameters = ((GeneralDerivedCRS) object).getConversionFromBase().getParameterValues();
        } else {
            parameters = ParameterGroup.EMPTY;
        }
        try {
            return parameters.parameter(parameter).getValue();
        } catch (ParameterNotFoundException exception) {
            return Errors.format(ErrorKeys.UNKNOW_PARAMETER_$1, parameter);
        } catch (RuntimeException exception) {
            return getLocalizedMessage(exception);
        }
    }

    /**
     * {@inheritDoc}
     */
    public double getAccuracy(final XPropertySet xOptions,
                              final String       sourceCode,
                              final String       targetCode)
    {
        final CoordinateReferenceSystem sourceCRS;
        final CoordinateReferenceSystem targetCRS;
        final CoordinateOperation       operation;
        final CRSAuthorityFactory       crsFactory = crsFactory();
        try {
             sourceCRS = crsFactory.createCoordinateReferenceSystem(sourceCode);
             targetCRS = crsFactory.createCoordinateReferenceSystem(targetCode);
             operation = opFactory().createOperation(sourceCRS, targetCRS);
        } catch (FactoryException exception) {
            reportException("getAccuracy", exception);
            return Double.NaN;
        }
        final Collection accuracies = operation.getPositionalAccuracy();
        for (final Iterator it=accuracies.iterator(); it.hasNext();) {
            final Result accuracy = ((PositionalAccuracy) it.next()).getResult();
            if (accuracy instanceof QuantitativeResult) {
                final double[] r = ((QuantitativeResult) accuracy).getValues();
                if (r!=null && r.length!=0) {
                    return r[0];
                }
            }
        }
        if (operation instanceof Conversion) {
            /*
             * For conversions, the accuracy is up to rounding error by definition.
             */
            return 0;
        }
        return Double.NaN;
    }

    /**
     * {@inheritDoc}
     */
    public double[][] getTransformedCoordinates(final XPropertySet xOptions,
                                                final double[][]   coordinates,
                                                final String       sourceCode,
                                                final String       targetCode)
    {
        final CoordinateReferenceSystem sourceCRS;
        final CoordinateReferenceSystem targetCRS;
        final CoordinateOperation       operation;
        final CRSAuthorityFactory       crsFactory = crsFactory();
        try {
             sourceCRS = crsFactory.createCoordinateReferenceSystem(sourceCode);
             targetCRS = crsFactory.createCoordinateReferenceSystem(targetCode);
             operation = opFactory().createOperation(sourceCRS, targetCRS);
        } catch (FactoryException exception) {
            reportException("getTransformedCoordinates", exception);
            return null;
        }
        /*
         * We now have every information needed for applying the coordinate operations.
         * Creates a result array and transform every point.
         */
        boolean failureReported = false;
        final MathTransform         mt       = operation.getMathTransform();
        final GeneralDirectPosition sourcePt = new GeneralDirectPosition(mt.getSourceDimensions());
        final GeneralDirectPosition targetPt = new GeneralDirectPosition(mt.getTargetDimensions());
        final double[][] result = new double[coordinates.length][];
        for (int j=0; j<coordinates.length; j++) {
            double[] coords = coordinates[j];
            if (coords == null) {
                continue;
            }
            for (int i=sourcePt.ordinates.length; --i>=0;) {
                sourcePt.ordinates[i] = (i<coords.length) ? coords[i] : 0;
            }
            final DirectPosition pt;
            try {
                pt = mt.transform(sourcePt, targetPt);
            } catch (TransformException exception) {
                /*
                 * The coordinate operation failed for this particular point. But maybe it will
                 * succeed for an other point. Set the values to NaN and continue the loop. Note:
                 * we will report the failure for logging purpose, but only the first one since
                 * all subsequent failures are likely to be the same one.
                 */
                if (!failureReported) {
                    reportException("getTransformedCoordinates", exception);
                    failureReported = true;
                }
                continue;
            }
            coords = new double[pt.getDimension()];
            for (int i=coords.length; --i>=0;) {
                coords[i] = pt.getOrdinate(i);
            }
            result[j] = coords;
        }    
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public double[][] getOrthodromicDistance(final XPropertySet xOptions,
                                             final String       CRS,
                                             final double[][]   source,
                                             final double[][]   target)
    {
        final GeodeticCalculator calculator;
        try {
            calculator = getGeodeticCalculator(CRS);
        } catch (FactoryException exception) {
            reportException("getOrthodromicDistance", exception);
            return null;
        }
        boolean failureReported = false;
        final int dim = calculator.getCoordinateReferenceSystem().getCoordinateSystem().getDimension();
        final GeneralDirectPosition sourcePt = new GeneralDirectPosition(dim);
        final GeneralDirectPosition targetPt = new GeneralDirectPosition(dim);
        final double[][] result = new double[getLength(source, target)][];
        for (int j=0; j<result.length; j++) {
            final double[] src = source[j % source.length];
            final double[] dst = target[j % target.length];
            if (src==null || dst==null) {
                continue;
            }
            for (int i=dim; --i>=0;) {
                sourcePt.ordinates[i] = (i<src.length) ? src[i] : 0;
                targetPt.ordinates[i] = (i<dst.length) ? dst[i] : 0;
            }
            try {
                calculator.setAnchorPosition     (sourcePt);
                calculator.setDestinationPosition(targetPt);
            } catch (TransformException exception) {
                if (!failureReported) {
                    reportException("getOrthodromicDistance", exception);
                    failureReported = true;
                }
                continue;
            }
            result[j] = new double[] {
                calculator.getOrthodromicDistance(),
                calculator.getAzimuth()
            };
        }    
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public double[][] getOrthodromicForward(final XPropertySet xOptions,
                                            final String       CRS,
                                            final double[][]   source,
                                            final double[][]   displacement)
    {
        final GeodeticCalculator calculator;
        try {
            calculator = getGeodeticCalculator(CRS);
        } catch (FactoryException exception) {
            reportException("getOrthodromicForward", exception);
            return null;
        }
        boolean failureReported = false;
        final int dim = calculator.getCoordinateReferenceSystem().getCoordinateSystem().getDimension();
        final GeneralDirectPosition sourcePt = new GeneralDirectPosition(dim);
        final double[][] result = new double[getLength(source, displacement)][];
        for (int j=0; j<result.length; j++) {
            final double[] src = source      [j % source.length];
            final double[] mov = displacement[j % displacement.length];
            if (src==null || mov==null) {
                continue;
            }
            for (int i=dim; --i>=0;) {
                sourcePt.ordinates[i] = (i<src.length) ? src[i] : 0;
            }
            double distance=0, azimuth=0;
            switch (mov.length) {
                default:                    // Fall through
                case 2:  azimuth  = mov[1]; // Fall through
                case 1:  distance = mov[0]; // Fall through
                case 0:  break;
            }
            final DirectPosition targetPt;
            try {
                calculator.setAnchorPosition(sourcePt);
                calculator.setDirection(azimuth, distance);
                targetPt = calculator.getDestinationPosition();
            } catch (TransformException exception) {
                if (!failureReported) {
                    reportException("getOrthodromicForward", exception);
                    failureReported = true;
                }
                continue;
            }
            result[j] = targetPt.getCoordinates();
        }    
        return result;
    }

    /**
     * Release resources used by this implementation.
     */
    protected void finalize() throws Throwable {
        if (crsFactory instanceof AbstractAuthorityFactory) try {
            ((AbstractAuthorityFactory) crsFactory).dispose();
            crsFactory = null;
        } catch (Exception exception) {
            // Ignore, since we are probably shuting down.
        }
        super.finalize();
    }
}
