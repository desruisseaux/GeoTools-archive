/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 *
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.cs;

// OpenGIS dependencies
import org.opengis.cs.CS_Info;
import org.opengis.cs.CS_Ellipsoid;
import org.opengis.cs.CS_PrimeMeridian;
import org.opengis.cs.CS_CoordinateSystem;
import org.opengis.cs.CS_LocalCoordinateSystem;
import org.opengis.cs.CS_FittedCoordinateSystem;
import org.opengis.cs.CS_CompoundCoordinateSystem;
import org.opengis.cs.CS_VerticalCoordinateSystem;
import org.opengis.cs.CS_HorizontalCoordinateSystem;
import org.opengis.cs.CS_GeocentricCoordinateSystem;
import org.opengis.cs.CS_GeographicCoordinateSystem;
import org.opengis.cs.CS_ProjectedCoordinateSystem;
import org.opengis.cs.CS_CoordinateSystemFactory;
import org.opengis.cs.CS_CoordinateSystemAuthorityFactory;

import org.opengis.cs.CS_Datum;
import org.opengis.cs.CS_DatumType;
import org.opengis.cs.CS_LocalDatum;
import org.opengis.cs.CS_VerticalDatum;
import org.opengis.cs.CS_HorizontalDatum;

import org.opengis.cs.CS_AxisInfo;
import org.opengis.cs.CS_AxisOrientationEnum;
import org.opengis.cs.CS_WGS84ConversionInfo;
import org.opengis.cs.CS_ProjectionParameter;
import org.opengis.cs.CS_Projection;

import org.opengis.cs.CS_Unit;
import org.opengis.cs.CS_LinearUnit;
import org.opengis.cs.CS_AngularUnit;

// J2SE dependencies
import java.rmi.RemoteException;
import java.rmi.ServerException;

// JAI dependencies (parameters)
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListImpl;
import javax.media.jai.ParameterListDescriptorImpl;

// OpenGIS dependencies
import org.opengis.referencing.FactoryException;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.resources.XArray;
import org.geotools.resources.RemoteProxy;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.MathTransform; // For javadoc


/**
 * Provide methods for interoperability with OpenGIS CS package.
 * All methods accept null argument. All OpenGIS objects are suitable for RMI use.
 *
 * A default instance of the <code>Adapters</code> classes is provided in the
 * {@link org.geotools.ct.Adapters#getDefault() org.geotools.ct} package.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see org.geotools.ct.Adapters#getDefault()
 *
 * @deprecated The legacy OpenGIS CS package is deprecated.
 *             There is no replacement at this time for RMI objects.
 */
public class Adapters extends org.geotools.pt.Adapters {
    /**
     * The factory to use for creating {@link CoordinateSystem} objects.
     */
    protected final CoordinateSystemFactory csFactory;

    /**
     * The factory to use for creating {@link MathTransform} objects.
     * This is used by {@link #wrap(CS_FittedCoordinateSystem)}.
     *
     * @task REVISIT: Commented out for now in order to avoid premature class loading.
     */
//  protected final MathTransformFactory mtFactory;

    /**
     * Construct an adapter with default factories.
     */
    protected Adapters() {
        csFactory = CoordinateSystemFactory.getDefault();
//      mtFactory = MathTransformFactory.getDefault();
    }

    /**
     * Construct an adapter with the specified factories.
     *
     * @param csFactory The factory to use for creating {@link CoordinateSystem} objects.
     * @param mtFactory The factory to use for creating {@link MathTransform} objects.
     *                  This is used by {@link #wrap(CS_FittedCoordinateSystem)}.
     */
//  protected Adapters(final CoordinateSystemFactory csFactory,
//                     final MathTransformFactory    mtFactory)
//  {
//      this.csFactory = csFactory;
//      this.mtFactory = mtFactory;
//  }
    
    /**
     * Returns an OpenGIS interface for a coordinate system authority factory.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_CoordinateSystemAuthorityFactory export(final CoordinateSystemAuthorityFactory factory)
            throws RemoteException
    {
        return (factory!=null) ? (CS_CoordinateSystemAuthorityFactory)factory.toOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a coordinate system factory.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_CoordinateSystemFactory export(final CoordinateSystemFactory factory)
            throws RemoteException
    {
        return (factory!=null) ? (CS_CoordinateSystemFactory)factory.toOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for an info. If the argument is an
     * <code>Info</code> subclass, the returned object will implement
     * the corresponding interface. For example, a call with an argument
     * of type {@link GeographicCoordinateSystem} will return an object
     * implementing the {@link CS_GeographicCoordinateSystem} interface.
     *
     * @throws RemoteException if the object can't be exported.
     */
    public CS_Info export(final Info info) throws RemoteException {
        return (info!=null) ? (CS_Info)info.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a coordinate system. If the argument
     * is a <code>CoordinateSystem</code> subclass, the returned object will
     * implement the corresponding interface.
     *
     * @throws RemoteException if the object can't be exported.
     */
    public CS_CoordinateSystem export(final CoordinateSystem cs) throws RemoteException {
        return (cs!=null) ? (CS_CoordinateSystem)cs.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a fitted coordinate system.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_FittedCoordinateSystem export(final FittedCoordinateSystem cs)
            throws RemoteException
    {
        return (cs!=null) ? (CS_FittedCoordinateSystem)cs.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a compound coordinate system.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_CompoundCoordinateSystem export(final CompoundCoordinateSystem cs)
            throws RemoteException
    {
        return (cs!=null) ? (CS_CompoundCoordinateSystem)cs.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a local coordinate system.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_LocalCoordinateSystem export(final LocalCoordinateSystem cs)
            throws RemoteException
    {
        return (cs!=null) ? (CS_LocalCoordinateSystem)cs.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a geocentric coordinate system.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_GeocentricCoordinateSystem export(final GeocentricCoordinateSystem cs)
            throws RemoteException
    {
        return (cs!=null) ? (CS_GeocentricCoordinateSystem)cs.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a vertical coordinate system.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_VerticalCoordinateSystem export(final VerticalCoordinateSystem cs)
            throws RemoteException
    {
        return (cs!=null) ? (CS_VerticalCoordinateSystem)cs.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a horizontal coordinate system.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_HorizontalCoordinateSystem export(final HorizontalCoordinateSystem cs)
            throws RemoteException
    {
        return (cs!=null) ? (CS_HorizontalCoordinateSystem)cs.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a geographic coordinate system.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_GeographicCoordinateSystem export(final GeographicCoordinateSystem cs)
            throws RemoteException
    {
        return (cs!=null) ? (CS_GeographicCoordinateSystem)cs.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a projected coordinate system.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_ProjectedCoordinateSystem export(final ProjectedCoordinateSystem cs)
            throws RemoteException
    {
        return (cs!=null) ? (CS_ProjectedCoordinateSystem)cs.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a projection.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_Projection export(final Projection projection)
            throws RemoteException
    {
        return (projection!=null) ? (CS_Projection)projection.cachedOpenGIS(this) : null;
    }
    
    /**
     * Constructs an array of OpenGIS structures from a parameters list.
     * This method is named <code>exportProjectionParameters</code> in
     * order to avoid conflict with {@link org.geotools.ct.Adapters#export(ParameterList)}.
     */
    final CS_ProjectionParameter[] exportProjectionParameters(final ParameterList parameters) {
        if (parameters == null) {
            return null;
        }
        final String[] names = parameters.getParameterListDescriptor().getParamNames();
        final CS_ProjectionParameter[] param = new CS_ProjectionParameter[names!=null ? names.length : 0];
        int count=0;
        for (int i=0; i<param.length; i++) {
            final String name = names[i];
            final Object value;
            try {
                value = parameters.getObjectParameter(name);
            } catch (IllegalStateException exception) {
                // No value and no default. Ignore...
                continue;
            }
            if (value instanceof Number) {
                param[count++] = new CS_ProjectionParameter(name, ((Number)value).doubleValue());
            }
        }
        return (CS_ProjectionParameter[]) XArray.resize(param, count);
    }
    
    /**
     * Returns an OpenGIS interface for a prime meridian.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_PrimeMeridian export(final PrimeMeridian meridian) throws RemoteException {
        return (meridian!=null) ? (CS_PrimeMeridian)meridian.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for an ellipsoid.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_Ellipsoid export(final Ellipsoid ellipsoid) throws RemoteException {
        return (ellipsoid!=null) ? (CS_Ellipsoid)ellipsoid.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS enumeration for a datum type.
     */
    public CS_DatumType export(final DatumType type) {
        return (type!=null) ? new CS_DatumType(type.getValue()) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a datum.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_Datum export(final Datum datum) throws RemoteException {
        return (datum!=null) ? (CS_Datum)datum.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a local datum.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_LocalDatum export(final LocalDatum datum) throws RemoteException {
        return (datum!=null) ? (CS_LocalDatum)datum.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a horizontal datum.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_HorizontalDatum export(final HorizontalDatum datum) throws RemoteException {
        return (datum!=null) ? (CS_HorizontalDatum)datum.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a vertical datum.
     * @throws RemoteException if the object can't be exported.
     */
    public CS_VerticalDatum export(final VerticalDatum datum) throws RemoteException {
        return (datum!=null) ? (CS_VerticalDatum)datum.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS enumeration for an axis orientation.
     */
    public CS_AxisOrientationEnum export(final AxisOrientation orientation) {
        return (orientation!=null) ? new CS_AxisOrientationEnum(orientation.getValue()) : null;
    }
    
    /**
     * Returns an OpenGIS structure for an axis info.
     */
    public CS_AxisInfo export(final AxisInfo axis) {
        return (axis!=null) ? new CS_AxisInfo(axis.name, export(axis.orientation)) : null;
    }
    
    /**
     * Returns an OpenGIS structure for conversion info.
     */
    public CS_WGS84ConversionInfo export(final WGS84ConversionInfo info) {
        if (info == null) {
            return null;
        }
        final CS_WGS84ConversionInfo nf = new CS_WGS84ConversionInfo();
        nf.dx        = info.dx;
        nf.dy        = info.dy;
        nf.dz        = info.dz;
        nf.ex        = info.ex;
        nf.ey        = info.ey;
        nf.ez        = info.ez;
        nf.ppm       = info.ppm;
        nf.areaOfUse = info.areaOfUse;
        return nf;
    }
    
    /**
     * Returns an OpenGIS interface for a unit. The returned interface may
     * extend {@link CS_LinearUnit} or {@link CS_AngularUnit} according to
     * the specified unit.
     *
     * @throws RemoteException if the object can't be exported.
     */
    public CS_Unit export(final Unit unit) throws RemoteException {
        if (unit == null) {
            return null;
        }
        final Info info = new Info(unit.getLocalizedName());
        if (unit.canConvert(Unit.METRE)) {
            return info.new LinearUnit(this, unit.convert(1, Unit.METRE));
        }
        if (unit.canConvert(Unit.RADIAN)) {
            return info.new AngularUnit(this, unit.convert(1, Unit.RADIAN));
        }
        return info.new AbstractUnit(this, Double.NaN);
    }
    
    /**
     * Checks whether the specified coordinate system has the expected number
     * of dimensions.
     *
     * @param  cs The coordinate system to check.
     * @param  expected The expected number of dimensions.
     * @throws IllegalArgumentException if the coordinate system
     *         doesn't have the expected number of dimensions.
     */
    private static void checkDimension(final CS_CoordinateSystem cs, final int expected)
            throws RemoteException, IllegalArgumentException
    {
        final int dimension = cs.getDimension();
        if (dimension != expected) {
            throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_ILLEGAL_CS_DIMENSION_$1, new Integer(dimension)));
        }
    }
    
    /**
     * Returns info for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public Info wrap(final CS_Info info) throws RemoteException {
        if (info == null) {
            return null;
        }
        if (info instanceof CS_Datum) {
            return wrap((CS_Datum) info);
        }
        if (info instanceof CS_CoordinateSystem) {
            return wrap((CS_CoordinateSystem) info);
        }
        if (info instanceof RemoteProxy) {
            return (Info) ((RemoteProxy)info).getImplementation();
        }
        return new Info(new InfoProperties.Adapter(info));
    }
    
    /**
     * Returns a coordinate system for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public CoordinateSystem wrap(final CS_CoordinateSystem cs) throws RemoteException {
        if (cs == null) {
            return null;
        }
        if (cs instanceof CS_FittedCoordinateSystem) {
            return wrap(( CS_FittedCoordinateSystem)cs);
        }
        if (cs instanceof CS_CompoundCoordinateSystem) {
            return wrap(( CS_CompoundCoordinateSystem)cs);
        }
        if (cs instanceof CS_LocalCoordinateSystem) {
            return wrap(( CS_LocalCoordinateSystem)cs);
        }
        if (cs instanceof CS_GeocentricCoordinateSystem) {
            return wrap(( CS_GeocentricCoordinateSystem)cs);
        }
        if (cs instanceof CS_VerticalCoordinateSystem) {
            return wrap(( CS_VerticalCoordinateSystem)cs);
        }
        if (cs instanceof CS_HorizontalCoordinateSystem) {
            return wrap(( CS_HorizontalCoordinateSystem)cs);
        }
        if (cs instanceof RemoteProxy) {
            return (CoordinateSystem) ((RemoteProxy)cs).getImplementation();
        }
        // CoordinateSystem is abstract
        throw new UnsupportedOperationException("Unknow CS not yet implemented");
    }
    
    /**
     * Returns a fitted coordinate system for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public FittedCoordinateSystem wrap(final CS_FittedCoordinateSystem cs)
            throws RemoteException
    {
        if (cs == null) {
            return null;
        }
        if (cs instanceof RemoteProxy) {
            return (FittedCoordinateSystem) ((RemoteProxy)cs).getImplementation();
        }
        try {
            return csFactory.createFittedCoordinateSystem(new InfoProperties.Adapter(cs),
                             wrap(cs.getBaseCoordinateSystem()),
//                           mtFactory.createFromWKT(cs.getToBase()),
                             MathTransformFactory.getDefault().createFromWKT(cs.getToBase()),
                             null);
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns a compound coordinate system for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public CompoundCoordinateSystem wrap(final CS_CompoundCoordinateSystem cs)
            throws RemoteException
    {
        if (cs == null) {
            return null;
        }
        if (cs instanceof RemoteProxy) {
            return (CompoundCoordinateSystem) ((RemoteProxy)cs).getImplementation();
        }
        try {
            return csFactory.createCompoundCoordinateSystem(new InfoProperties.Adapter(cs),
                             wrap(cs.getHeadCS()), wrap(cs.getTailCS()));
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns a local coordinate system for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public LocalCoordinateSystem wrap(final CS_LocalCoordinateSystem cs) throws RemoteException {
        if (cs == null) {
            return null;
        }
        if (cs instanceof RemoteProxy) {
            return (LocalCoordinateSystem) ((RemoteProxy)cs).getImplementation();
        }
        final LocalDatum datum = wrap(cs.getLocalDatum());
        final Unit[]     units = new Unit[cs.getDimension()];
        final AxisInfo[]  axes = new AxisInfo[units.length];
        for (int i=0; i<axes.length; i++) {
            axes [i] = wrap(cs.getAxis (i));
            units[i] = wrap(cs.getUnits(i));
        }
        try {
            return csFactory.createLocalCoordinateSystem(new InfoProperties.Adapter(cs),
                             datum, units, axes);
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns a geocentric coordinate system for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public GeocentricCoordinateSystem wrap(final CS_GeocentricCoordinateSystem cs)
            throws RemoteException
    {
        if (cs == null) {
            return null;
        }
        if (cs instanceof RemoteProxy) {
            return (GeocentricCoordinateSystem) ((RemoteProxy)cs).getImplementation();
        }
        checkDimension(cs, 3);
        final Unit              unit = wrap(cs.getLinearUnit());
        final HorizontalDatum  datum = wrap(cs.getHorizontalDatum());
        final PrimeMeridian meridian = wrap(cs.getPrimeMeridian());
        final AxisInfo[]        axes = new AxisInfo[cs.getDimension()];
        for (int i=0; i<axes.length; i++) {
            axes[i] = wrap(cs.getAxis(i));
            // Accept null value.
        }
        try {
            return csFactory.createGeocentricCoordinateSystem(new InfoProperties.Adapter(cs),
                             unit, datum, meridian, axes);
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns a vertical coordinate system for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public VerticalCoordinateSystem wrap(final CS_VerticalCoordinateSystem cs)
            throws RemoteException
    {
        if (cs == null) {
            return null;
        }
        if (cs instanceof RemoteProxy) {
            return (VerticalCoordinateSystem) ((RemoteProxy)cs).getImplementation();
        }
        checkDimension(cs, 1);
        final VerticalDatum datum = wrap(cs.getVerticalDatum());
        final Unit           unit = wrap(cs.getVerticalUnit());
        final AxisInfo       axis = wrap(cs.getAxis(0));
        try {
            return csFactory.createVerticalCoordinateSystem(new InfoProperties.Adapter(cs),
                             datum, unit, axis);
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns a horizontal coordinate system for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public HorizontalCoordinateSystem wrap(final CS_HorizontalCoordinateSystem cs)
            throws RemoteException
    {
        if (cs == null) {
            return null;
        }
        if (cs instanceof CS_GeographicCoordinateSystem) return wrap((CS_GeographicCoordinateSystem)cs);
        if (cs instanceof  CS_ProjectedCoordinateSystem) return wrap( (CS_ProjectedCoordinateSystem)cs);
        if (cs instanceof RemoteProxy) {
            return (HorizontalCoordinateSystem) ((RemoteProxy)cs).getImplementation();
        }
        // HorizontalCoordinateSystem is abstract
        throw new UnsupportedOperationException("Unknown CS not yet implemented");
    }
    
    /**
     * Returns a geographic coordinate system for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public GeographicCoordinateSystem wrap(final CS_GeographicCoordinateSystem cs)
            throws RemoteException
    {
        if (cs == null) {
            return null;
        }
        if (cs instanceof RemoteProxy) {
            return (GeographicCoordinateSystem) ((RemoteProxy)cs).getImplementation();
        }
        checkDimension(cs, 2);
        final Unit              unit = wrap(cs.getAngularUnit());
        final HorizontalDatum  datum = wrap(cs.getHorizontalDatum());
        final PrimeMeridian meridian = wrap(cs.getPrimeMeridian());
        final AxisInfo         axis0 = wrap(cs.getAxis(0));
        final AxisInfo         axis1 = wrap(cs.getAxis(1));
        try {
            return csFactory.createGeographicCoordinateSystem(new InfoProperties.Adapter(cs),
                             unit, datum, meridian, axis0, axis1);
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns a projected coordinate system for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public ProjectedCoordinateSystem wrap(final CS_ProjectedCoordinateSystem cs)
            throws RemoteException
    {
        if (cs == null) {
            return null;
        }
        if (cs instanceof RemoteProxy) {
            return (ProjectedCoordinateSystem) ((RemoteProxy)cs).getImplementation();
        }
        checkDimension(cs, 2);
        final GeographicCoordinateSystem gcs = wrap(cs.getGeographicCoordinateSystem());
        final Projection          projection = wrap(cs.getProjection());
        final Unit                      unit = wrap(cs.getLinearUnit());
        final AxisInfo                 axis0 = wrap(cs.getAxis(0));
        final AxisInfo                 axis1 = wrap(cs.getAxis(1));
        try {
            return csFactory.createProjectedCoordinateSystem(new InfoProperties.Adapter(cs),
                             gcs, projection, unit, axis0, axis1);
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns a projection for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public Projection wrap(final CS_Projection projection) throws RemoteException {
        if (projection == null) {
            return null;
        }
        if (projection instanceof RemoteProxy) {
            return (Projection) ((RemoteProxy)projection).getImplementation();
        }
        final CS_ProjectionParameter[] parameters = new CS_ProjectionParameter[projection.getNumParameters()];
        for (int i=0; i<parameters.length; i++) {
            parameters[i] = projection.getParameter(i);
        }
        try {
            return csFactory.createProjection(new InfoProperties.Adapter(projection), projection.getClassName(),
                             wrap(parameters));
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns a prime meridian for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public PrimeMeridian wrap(final CS_PrimeMeridian meridian) throws RemoteException {
        if (meridian == null) {
            return null;
        }
        if (meridian instanceof RemoteProxy) {
            return (PrimeMeridian) ((RemoteProxy)meridian).getImplementation();
        }
        try {
            return csFactory.createPrimeMeridian(new InfoProperties.Adapter(meridian),
                             wrap(meridian.getAngularUnit()), meridian.getLongitude());
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns an ellipsoid for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public Ellipsoid wrap(final CS_Ellipsoid ellipsoid) throws RemoteException {
        if (ellipsoid == null) {
            return null;
        }
        if (ellipsoid instanceof RemoteProxy) {
            return (Ellipsoid) ((RemoteProxy)ellipsoid).getImplementation();
        }
        final CharSequence        name = new InfoProperties.Adapter(ellipsoid);
        final double     semiMajorAxis = ellipsoid.getSemiMajorAxis();
        final double     semiMinorAxis = ellipsoid.getSemiMinorAxis();
        final double inverseFlattening = ellipsoid.getInverseFlattening();
        final boolean    ivfDefinitive = ellipsoid.isIvfDefinitive();
        final Unit               units = wrap(ellipsoid.getAxisUnit());
        try {
            if (ivfDefinitive) {
                return csFactory.createFlattenedSphere(name, semiMajorAxis, inverseFlattening, units);
            } else {
                return csFactory.createEllipsoid(name, semiMajorAxis, semiMinorAxis, units);
            }
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns a datum type for an OpenGIS enumeration.
     */
    public DatumType wrap(final CS_DatumType type) {
        return (type!=null) ? DatumType.getEnum(type.value) : null;
    }
    
    /**
     * Returns a datum for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public Datum wrap(final CS_Datum datum) throws RemoteException {
        if (datum == null) {
            return null;
        }
        if (datum instanceof CS_LocalDatum) {
            return wrap((CS_LocalDatum)datum);
        }
        if (datum instanceof CS_VerticalDatum) {
            return wrap((CS_VerticalDatum)datum);
        }
        if (datum instanceof CS_HorizontalDatum) {
            return wrap((CS_HorizontalDatum)datum);
        }
        if (datum instanceof RemoteProxy) {
            return (Datum) ((RemoteProxy)datum).getImplementation();
        }
        return new Datum(new InfoProperties.Adapter(datum), wrap(datum.getDatumType()));
    }
    
    /**
     * Returns a local datum for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public LocalDatum wrap(final CS_LocalDatum datum) throws RemoteException {
        if (datum == null) {
            return null;
        }
        if (datum instanceof RemoteProxy) {
            return (LocalDatum) ((RemoteProxy)datum).getImplementation();
        }
        try {
            return csFactory.createLocalDatum(new InfoProperties.Adapter(datum), 
                             (DatumType.Local) wrap(datum.getDatumType()));
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns a horizontal datum for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public HorizontalDatum wrap(final CS_HorizontalDatum datum) throws RemoteException {
        if (datum == null) {
            return null;
        }
        if (datum instanceof RemoteProxy) {
            return (HorizontalDatum) ((RemoteProxy)datum).getImplementation();
        }
        try {
            return csFactory.createHorizontalDatum(new InfoProperties.Adapter(datum),
                             (DatumType.Horizontal) wrap(datum.getDatumType()),
                             wrap(datum.getEllipsoid()),
                             wrap(datum.getWGS84Parameters()));
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns a vertical datum for an OpenGIS interface.
     * @throws RemoteException if a remote call fails.
     */
    public VerticalDatum wrap(final CS_VerticalDatum datum) throws RemoteException {
        if (datum == null) {
            return null;
        }
        if (datum instanceof RemoteProxy) {
            return (VerticalDatum) ((RemoteProxy)datum).getImplementation();
        }
        try {
            return csFactory.createVerticalDatum(new InfoProperties.Adapter(datum),
                             (DatumType.Vertical) wrap(datum.getDatumType()));
        } catch (FactoryException exception) {
            throw serverException(exception);
        }
    }
    
    /**
     * Returns an axis orientation for an OpenGIS enumeration.
     */
    public AxisOrientation wrap(final CS_AxisOrientationEnum orientation) {
        return (orientation!=null) ? AxisOrientation.getEnum(orientation.value) : null;
    }
    
    /**
     * Returns an axis info for an OpenGIS structure.
     */
    public AxisInfo wrap(final CS_AxisInfo axis) {
        return (axis!=null) ? new AxisInfo(axis.name, wrap(axis.orientation)) : null;
    }
    
    /**
     * Returns an axis array for an OpenGIS structure array.
     */
    final AxisInfo[] wrap(final CS_AxisInfo[] axis) {
        if (axis == null) {
            return null;
        }
        final AxisInfo[] a=new AxisInfo[axis.length];
        for (int i=0; i<axis.length; i++) {
            a[i] = wrap(axis[i]);
        }
        return a;
    }
    
    /**
     * Returns a parameter list for an array of OpenGIS structures.
     */
    public ParameterList wrap(final CS_ProjectionParameter[] parameters) {
        if (parameters == null) {
            return null;
        }
        int count=0;
        String[] paramNames   = new String[parameters.length];
        Class [] paramClasses = new Class [parameters.length];
        for (int i=0; i<parameters.length; i++) {
            final CS_ProjectionParameter param = parameters[i];
            if (param!=null) {
                paramNames  [count] = param.name;
                paramClasses[count] = Double.class;
                count++;
            }
        }
        paramNames   = (String[]) XArray.resize(paramNames,   count);
        paramClasses = (Class []) XArray.resize(paramClasses, count);
        final ParameterList list = new ParameterListImpl(new ParameterListDescriptorImpl(null,
                                                         paramNames, paramClasses, null, null));
        for (int i=0; i<paramNames.length; i++) {
            list.setParameter(paramNames[i], parameters[i].value);
        }
        return list;
    }
    
    /**
     * Returns conversion info for an OpenGIS structure.
     */
    public WGS84ConversionInfo wrap(final CS_WGS84ConversionInfo info) {
        if (info == null) {
            return null;
        }
        final WGS84ConversionInfo nf = new WGS84ConversionInfo();
        nf.dx = info.dx;
        nf.dy = info.dy;
        nf.dz = info.dz;
        nf.ex = info.ex;
        nf.ey = info.ey;
        nf.ez = info.ez;
        nf.ppm = info.ppm;
        nf.areaOfUse = info.areaOfUse;
        return nf;
    }
    
    /**
     * Returns a unit for an OpenGIS structure.
     * @throws RemoteException if a remote call fails.
     */
    public Unit wrap(final CS_Unit unit) throws RemoteException {
        if (unit == null) {
            return null;
        }
        if (unit instanceof CS_LinearUnit) {
            final double metersPerUnit = ((CS_LinearUnit)unit).getMetersPerUnit();
            return Unit.METRE.scale(metersPerUnit);
        }
        if (unit instanceof CS_AngularUnit) {
            final double radiansPerUnit = ((CS_AngularUnit)unit).getRadiansPerUnit();
            return Unit.RADIAN.scale(radiansPerUnit);
        }
        throw new UnsupportedOperationException("Only meters and degrees are currently implemented");
    }

    /**
     * Wrap a {@link FactoryException} into a {@link RemoteException}.
     */
    static RemoteException serverException(final FactoryException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof RemoteException) {
            return (RemoteException) cause;
        }
        return new ServerException("Can't create object", exception);
    }
}
