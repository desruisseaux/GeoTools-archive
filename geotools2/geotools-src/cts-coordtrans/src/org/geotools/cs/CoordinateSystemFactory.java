/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
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
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.cs;

// OpenGIS dependencies
import org.opengis.cs.CS_Unit;
import org.opengis.cs.CS_AxisInfo;
import org.opengis.cs.CS_DatumType;
import org.opengis.cs.CS_Ellipsoid;
import org.opengis.cs.CS_LinearUnit;
import org.opengis.cs.CS_AngularUnit;
import org.opengis.cs.CS_PrimeMeridian;
import org.opengis.cs.CS_VerticalDatum;
import org.opengis.cs.CS_HorizontalDatum;
import org.opengis.cs.CS_CoordinateSystem;
import org.opengis.cs.CS_CoordinateSystemFactory;
import org.opengis.cs.CS_CompoundCoordinateSystem;
import org.opengis.cs.CS_VerticalCoordinateSystem;
import org.opengis.cs.CS_HorizontalCoordinateSystem;
import org.opengis.cs.CS_GeocentricCoordinateSystem;
import org.opengis.cs.CS_GeographicCoordinateSystem;
import org.opengis.cs.CS_ProjectedCoordinateSystem;
import org.opengis.cs.CS_FittedCoordinateSystem;
import org.opengis.cs.CS_LocalCoordinateSystem;
import org.opengis.cs.CS_LocalDatum;
import org.opengis.cs.CS_Projection;
import org.opengis.cs.CS_ProjectionParameter;
import org.opengis.cs.CS_WGS84ConversionInfo;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.util.WeakHashSet;

// J2SE and JAI dependencies
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.server.RemoteObject;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListImpl;


/**
 * Builds up complex objects from simpler objects or values.
 * <code>CoordinateSystemFactory</code> allows applications to make coordinate
 * systems that cannot be created by a {@link CoordinateSystemAuthorityFactory}.
 * This factory is very flexible, whereas the authority factory is easier to
 * use.
 *
 * So {@link CoordinateSystemAuthorityFactory} can be used to make 'standard'
 * coordinate systems, and <code>CoordinateSystemFactory</code> can be used to
 * make "special" coordinate systems.
 *
 * For example, the EPSG authority has codes for USA state plane coordinate
 * systems using the NAD83 datum, but these coordinate systems always use
 * meters.  EPSG does not have codes for NAD83 state plane coordinate systems
 * that use feet units.  This factory lets an application create such a hybrid
 * coordinate system.
 *
 * @version $Id: CoordinateSystemFactory.java,v 1.4 2002/07/11 23:56:38 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_CoordinateSystemFactory
 */
public class CoordinateSystemFactory {
    /**
     * Default coordinate system factory.
     * Will be constructed only when first needed.
     */
    private static CoordinateSystemFactory DEFAULT;
    
    /**
     * Set of weak references to existing coordinate systems.
     * This set is used in order to return a pre-existing object
     * instead of creating a new one.
     */
    private final WeakHashSet pool;
    
    /**
     * Constructs a new factory with the specified pool.
     */
    private CoordinateSystemFactory(final WeakHashSet pool) {
        this.pool = pool;
    }
    
    /**
     * Default constructor.
     */
    protected CoordinateSystemFactory() {
        this(new WeakHashSet());
    }
    
    /**
     * Returns the default coordinate system factory.
     */
    public static synchronized CoordinateSystemFactory getDefault() {
        if (DEFAULT==null) {
            DEFAULT = new CoordinateSystemFactory(Info.pool);
        }
        return DEFAULT;
    }
    
    /**
     * Creates a geographic coordinate system.  This coordinate system will use
     * <var>longitude</var>/<var>latitude</var> ordinates with longitude values
     * increasing east and latitude values increasing north.  Angular units are
     * degrees and prime meridian is Greenwich.
     *
     * @param name Name to give new object.
     * @param datum Horizontal datum for created coordinate system.
     * @return The coordinate system.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     */
    public GeographicCoordinateSystem createGeographicCoordinateSystem(
                                        final CharSequence    name,
                                        final HorizontalDatum datum)
        throws FactoryException
    {
        return createGeographicCoordinateSystem(name, Unit.DEGREE, datum,
                                                PrimeMeridian.GREENWICH,
                                                AxisInfo.LONGITUDE,
                                                AxisInfo.LATITUDE);
    }
    
    /**
     * Creates a geographic coordinate system, which could be
     * <var>latitude</var>/<var>longiude</var> or
     * <var>longitude</var>/<var>latitude</var>.
     *
     * @param name      Name to give new object.
     * @param unit      Angular units for created coordinate system.
     * @param datum     Horizontal datum for created coordinate system.
     * @param meridian  Prime Meridian for created coordinate system.
     * @param axis0     Details of 0th ordinates.
     * @param axis1     Details of 1st ordinates.
     *
     * @return The coordinate system.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createGeographicCoordinateSystem
     */
    public GeographicCoordinateSystem createGeographicCoordinateSystem(
                                        final CharSequence    name,
                                        final Unit            unit,
                                        final HorizontalDatum datum,
                                        final PrimeMeridian   meridian,
                                        final AxisInfo        axis0,
                                        final AxisInfo        axis1)
        throws FactoryException
    {
        return (GeographicCoordinateSystem) pool.canonicalize(
                new GeographicCoordinateSystem(name, unit, datum, meridian, axis0, axis1));}
    
    /**
     * Creates a projected coordinate system using the specified geographic
     * system. Projected coordinates will be in meters, <var>x</var> values
     * increasing east and <var>y</var> values increasing north.
     *
     * @param  name Name to give new object.
     * @param  gcs Geographic coordinate system to base projection on.
     * @param  projection Projection from geographic to projected coordinate system.
     * @return The coordinate system.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     */
    public ProjectedCoordinateSystem createProjectedCoordinateSystem(
                                        final CharSequence               name,
                                        final GeographicCoordinateSystem gcs,
                                        final Projection                 projection)
        throws FactoryException
    {
        return createProjectedCoordinateSystem(name, gcs, projection,
                                               Unit.METRE, AxisInfo.X, AxisInfo.Y);
    }
    
    /**
     * Creates a projected coordinate system using a projection object.
     *
     * @param  name Name to give new object.
     * @param  gcs Geographic coordinate system to base projection on.
     * @param  projection Projection from geographic to projected coordinate system.
     * @param  unit Linear units of returned PCS.
     * @param  axis0 Details of 0th ordinates in returned PCS coordinates.
     * @param  axis1 Details of 1st ordinates in returned PCS coordinates.
     * @return The coordinate system.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createProjectedCoordinateSystem
     */
    public ProjectedCoordinateSystem createProjectedCoordinateSystem(
                                        final CharSequence               name,
                                        final GeographicCoordinateSystem gcs,
                                        final Projection                 projection,
                                        final Unit                       unit,
                                        final AxisInfo                   axis0,
                                        final AxisInfo                   axis1)
        throws FactoryException
    {
        return (ProjectedCoordinateSystem) pool.canonicalize(
                new ProjectedCoordinateSystem(name, gcs, projection, unit, axis0, axis1));
    }
    
    /**
     * Creates a vertical coordinate system from a datum. Units
     * will be metres and values will be increasing upward.
     *
     * @param name  Name to give new object.
     * @param datum Datum to use for new coordinate system.
     * @return The coordinate system.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     */
    public VerticalCoordinateSystem createVerticalCoordinateSystem(
                                        final CharSequence  name,
                                        final VerticalDatum datum)
        throws FactoryException
    {
        return createVerticalCoordinateSystem(name, datum, Unit.METRE, AxisInfo.ALTITUDE);
    }
    
    /**
     * Creates a vertical coordinate system from a datum and linear units.
     *
     * @param name  Name to give new object.
     * @param datum Datum to use for new coordinate system.
     * @param unit  Units to use for new coordinate system.
     * @param axis  Axis to use for new coordinate system.
     * @return The coordinate system.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createVerticalCoordinateSystem
     */
    public VerticalCoordinateSystem createVerticalCoordinateSystem(
                                        final CharSequence  name,
                                        final VerticalDatum datum,
                                        final Unit          unit,
                                        final AxisInfo      axis)
        throws FactoryException
    {
        return (VerticalCoordinateSystem) pool.canonicalize(
                new VerticalCoordinateSystem(name, datum, unit, axis));
    }
    
    /**
     * Creates a compound coordinate system.
     *
     * @param name Name to give new object.
     * @param head Coordinate system to use for earlier ordinates.
     * @param tail Coordinate system to use for later ordinates.
     * @return The coordinate system.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createCompoundCoordinateSystem
     */
    public CompoundCoordinateSystem createCompoundCoordinateSystem(
                                        final CharSequence     name,
                                        final CoordinateSystem head,
                                        final CoordinateSystem tail)
        throws FactoryException
    {
        return (CompoundCoordinateSystem) pool.canonicalize(
                new CompoundCoordinateSystem(name, head, tail));
    }
    
    /**
     * Creates a local coordinate system.
     * The dimension of the local coordinate system is determined by the size
     * of the axis array. All the axes will have the same units. If you want
     * to make a coordinate system with mixed units, then you can make a
     * compound coordinate system from different local coordinate systems.
     *
     * @param name  Name to give new object.
     * @param datum Local datum to use in created CS.
     * @param unit  Units to use for all axes in created CS.
     * @param axes  Axes to use in created CS.
     * @return The coordinate system.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createLocalCoordinateSystem
     */
    public LocalCoordinateSystem createLocalCoordinateSystem(
                                        final CharSequence name,
                                        final LocalDatum   datum,
                                        final Unit         unit,
                                        final AxisInfo[]   axes)
        throws FactoryException
    {
        return (LocalCoordinateSystem) pool.canonicalize(
                new LocalCoordinateSystem(name, datum, unit, axes));
    }
    
    /**
     * Creates an ellipsoid from radius values.
     *
     * @param name          Name to give new object.
     * @param semiMajorAxis Equatorial radius in supplied linear units.
     * @param semiMinorAxis Polar radius in supplied linear units.
     * @param unit          Linear units of ellipsoid axes.
     * @return The ellipsoid.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createEllipsoid
     */
    public Ellipsoid createEllipsoid(final CharSequence name,
                                     final double       semiMajorAxis,
                                     final double       semiMinorAxis,
                                     final Unit         unit)
        throws FactoryException
    {
        return (Ellipsoid) pool.canonicalize(
                Ellipsoid.createEllipsoid(name, semiMajorAxis, semiMinorAxis, unit));
    }
    
    /**
     * Creates an ellipsoid from an major radius, and inverse flattening.
     *
     * @param name              Name to give new object.
     * @param semiMajorAxis     Equatorial radius in supplied linear units.
     * @param inverseFlattening Eccentricity of ellipsoid.
     * @param unit              Linear units of major axis.
     * @return The ellipsoid.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createFlattenedSphere
     */
    public Ellipsoid createFlattenedSphere(final CharSequence name,
                                           final double       semiMajorAxis,
                                           final double       inverseFlattening,
                                           final Unit         unit)
        throws FactoryException
    {
        return (Ellipsoid) pool.canonicalize(
                Ellipsoid.createFlattenedSphere(name, semiMajorAxis, inverseFlattening, unit));
    }
    
    /**
     * Creates a prime meridian, relative to Greenwich.
     *
     * @param name      Name to give new object.
     * @param unit      Angular units of longitude.
     * @param longitude Longitude of prime meridian in supplied angular units
     *        East of Greenwich.
     * @return The prime meridian.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createPrimeMeridian
     */
    public PrimeMeridian createPrimeMeridian(final CharSequence name,
                                             final Unit         unit,
                                             final double       longitude)
        throws FactoryException
    {
        return (PrimeMeridian) pool.canonicalize(
                new PrimeMeridian(name, unit, longitude));
    }
    
    /**
     * Creates a projection. The client must ensure that all the linear
     * parameters are expressed in meters, and all the angular parameters
     * are expressed in degrees.  Also, they must supply
     * <code>"semi_major"</code> and <code>"semi_minor"</code> parameters.
     * The set of legal parameters and their default values can be queried
     * using {@link #createProjectionParameterList}. Example:
     *
     * <blockquote><pre>
     * &nbsp;{link ParameterList} param = {@link #createProjectionParameterList createProjectionParameterList}("Transverse_Mercator")
     * &nbsp;                                    .setParameter("semi_major", 6378206.4)
     * &nbsp;                                    .setParameter("semi_minor", 6356583.8);
     * &nbsp;{@link Projection} proj = createProjection("My projection", "Transverse_Mercator", param);
     * </pre></blockquote>
     *
     * @param name           Name to give new object.
     * @param classification Classification string for projection (e.g. "Transverse_Mercator").
     * @param parameters     Parameters to use for projection. A default set of parameters can
     *                       be constructed using <code>{@link #createProjectionParameterList
     *                       createProjectionParameterList}(classification)</code> and initialized
     *                       using a chain of <code>setParameter(...)</code> calls.
     * @return The projection.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createProjection
     */
    public Projection createProjection(final CharSequence  name,
                                       final String        classification,
                                       final ParameterList parameters)
        throws FactoryException
    {
        return (Projection) pool.canonicalize(
                new Projection(name, classification, parameters));
    }
    
    /**
     * Convenience method for constructing a projection using the specified
     * ellipsoid.
     *
     * @param name           Name to give new object.
     * @param classification Classification string for projection (e.g. "Transverse_Mercator").
     * @param ellipsoid      Ellipsoid parameter. If non-null, then <code>"semi_major"</code>
     *                       and <code>"semi_minor"</code> parameters will be set accordingly.
     * @param centre         Central meridian and latitude of origin, in degrees. If non-null, then
     *                       <code>"central_meridian"</code> and <code>"latitude_of_origin"</code>
     *                       will be set accordingly.
     * @param translation    False easting and northing, in metres. If non-null, then
     *                       <code>"false_easting"</code> and <code>"false_northing"</code>
     *                       will be set accordingly.
     * @return The projection.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     */
    public Projection createProjection(final CharSequence name,
                                       final String       classification,
                                       final Ellipsoid    ellipsoid,
                                       final Point2D      centre,
                                       final Point2D      translation)
        throws FactoryException
    {
        ParameterList param = createProjectionParameterList(classification);
        param = Projection.init(param, ellipsoid, centre, translation);
        return createProjection(name, classification, param);
    }
    
    /**
     * Returns a default parameter list for the specified projection.
     *
     * @param  classification Classification string for projection (e.g. "Transverse_Mercator").
     * @return A default parameter list for the supplied projection class.
     *
     * @see #createProjection(CharSequence, String, ParameterList)
     */
    public ParameterList createProjectionParameterList(final String classification) {
        return Projection.getParameterList(classification);
    }
    
    /**
     * Creates horizontal datum from ellipsoid and Bursa-Wolf parameters.
     * Since this method contains a set of Bursa-Wolf parameters, the created
     * datum will always have a relationship to WGS84. If you wish to create
     * a horizontal datum that has no relationship with WGS84, then you can
     * either specify {@link DatumType.Horizontal#OTHER} as the
     * horizontalDatumType, or create it via WKT.
     *
     * @param name      Name to give new object.
     * @param type      Type of horizontal datum to create.
     * @param ellipsoid Ellipsoid to use in new horizontal datum.
     * @param toWGS84   Suggested approximate conversion from new datum to WGS84.
     * @return The datum.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createHorizontalDatum
     */
    public HorizontalDatum createHorizontalDatum(final CharSequence         name,
                                                 final DatumType.Horizontal type,
                                                 final Ellipsoid            ellipsoid,
                                                 final WGS84ConversionInfo  toWGS84)
        throws FactoryException
    {
        return (HorizontalDatum) pool.canonicalize(
                new HorizontalDatum(name, type, ellipsoid, toWGS84));
    }
    
    /**
     * Creates horizontal datum from an ellipsoid.
     * The datum type will be {@link DatumType.Horizontal#OTHER}.
     *
     * @param name      Name to give new object.
     * @param ellipsoid Ellipsoid to use in new horizontal datum.
     * @return The datum.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     */
    public HorizontalDatum createHorizontalDatum(final CharSequence name,
                                                 final Ellipsoid    ellipsoid)
        throws FactoryException
    {
        return createHorizontalDatum(name, DatumType.Horizontal.OTHER, ellipsoid, null);
    }
    
    /**
     * Creates a vertical datum from an enumerated type value.
     *
     * @param name Name to give new object.
     * @param type Type of vertical datum to create.
     * @return The datum.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createVerticalDatum
     */
    public VerticalDatum createVerticalDatum(final CharSequence       name,
                                             final DatumType.Vertical type)
        throws FactoryException
    {
        return (VerticalDatum) pool.canonicalize(new VerticalDatum(name, type));
    }
    
    /**
     * Creates a local datum.
     *
     * @param name Name to give new object.
     * @param localDatumType Type of local datum to create.
     * @return The datum.
     * @throws IllegalArgumentException if an argument is <code>null</code>
     *         or incompatible with the object to be created.
     * @throws FactoryException if an error occurred during the object creation.
     *         It may be, for example, a network error or a failure on the
     *         server side.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createLocalDatum
     */
    public LocalDatum createLocalDatum(final CharSequence    name,
                                       final DatumType.Local type)
        throws FactoryException
    {
        return (LocalDatum) pool.canonicalize(new LocalDatum(name, type));
    }
    
    /**
     * Returns an OpenGIS interface for this info.
     * The returned object is suitable for RMI use.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid premature class loading of OpenGIS interface.
     */
    final Object toOpenGIS(final Object adapters) {
        return new Export(adapters);
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wrap an {@link Info} object for use with OpenGIS. This wrapper is a
     * good place to check for non-implemented OpenGIS methods (just check
     * for methods throwing {@link UnsupportedOperationException}). This
     * class is suitable for RMI use.
     */
    private final class Export extends RemoteObject implements CS_CoordinateSystemFactory {
        /**
         * The originating adapter.
         */
        protected final Adapters adapters;
        
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) {
            this.adapters = (Adapters)adapters;
        }
        
        /**
         * Returns the underlying implementation.
         */
        public CoordinateSystemFactory unwrap() {
            return CoordinateSystemFactory.this;
        }
        
        /**
         * Creates a coordinate system object from an XML string.
         */
        public CS_CoordinateSystem createFromXML(final String xml)
            throws RemoteException
        {
            throw new UnsupportedOperationException("XML parsing not yet implemented");
        }
        
        /**
         * Creates a coordinate system object from a Well-Known Text string.
         */
        public CS_CoordinateSystem createFromWKT(String wellKnownText)
            throws RemoteException
        {
            throw new UnsupportedOperationException("WKT parsing not yet implemented");
        }
        
        /**
         * Creates a compound coordinate system.
         */
        public CS_CompoundCoordinateSystem createCompoundCoordinateSystem(
                                        final String              name,
                                        final CS_CoordinateSystem head,
                                        final CS_CoordinateSystem tail)
            throws RemoteException 
        {
            try {
                return adapters.export(CoordinateSystemFactory.this.createCompoundCoordinateSystem(
                        name, adapters.wrap(head), adapters.wrap(tail)));
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    throw (RemoteException) cause;
                }
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
        
        /**
         * Creates a fitted coordinate system.
         */
        public CS_FittedCoordinateSystem createFittedCoordinateSystem(
                                        final String              name,
                                        final CS_CoordinateSystem base,
                                        final String              toBaseWKT,
                                        final CS_AxisInfo[]       arAxes)
            throws RemoteException
        {
            throw new UnsupportedOperationException("Fitted CS not yet implemented");
        }
        
        /**
         * Creates a local coordinate system.
         */
        public CS_LocalCoordinateSystem createLocalCoordinateSystem(
                                        final String        name,
                                        final CS_LocalDatum datum,
                                        final CS_Unit       unit,
                                        final CS_AxisInfo[] arAxes)
            throws RemoteException
        {
            try {
                return adapters.export(CoordinateSystemFactory.this.createLocalCoordinateSystem(
                        name, adapters.wrap(datum), adapters.wrap(unit), adapters.wrap(arAxes)));
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    throw (RemoteException) cause;
                }
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
        
        /**
         * Creates an ellipsoid from radius values.
         */
        public CS_Ellipsoid createEllipsoid(final String name,
                                            final double semiMajorAxis,
                                            final double semiMinorAxis,
                                            final CS_LinearUnit linearUnit)
            throws RemoteException
        {
            try {
                return adapters.export(CoordinateSystemFactory.this.createEllipsoid(
                        name, semiMajorAxis, semiMinorAxis, adapters.wrap(linearUnit)));
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    throw (RemoteException) cause;
                }
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
        
        /**
         * Creates an ellipsoid from a major radius, and inverse flattening.
         */
        public CS_Ellipsoid createFlattenedSphere(final String name,
                                                  final double semiMajorAxis,
                                                  final double inverseFlattening,
                                                  final CS_LinearUnit linearUnit)
            throws RemoteException
        {
            try {
                return adapters.export(CoordinateSystemFactory.this.createFlattenedSphere(
                        name, semiMajorAxis, inverseFlattening, adapters.wrap(linearUnit)));
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    throw (RemoteException) cause;
                }
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
        
        /**
         * Creates a projected coordinate system using a projection object.
         */
        public CS_ProjectedCoordinateSystem createProjectedCoordinateSystem(
                                        final String                       name,
                                        final CS_GeographicCoordinateSystem gcs,
                                        final CS_Projection          projection,
                                        final CS_LinearUnit          linearUnit,
                                        final CS_AxisInfo                 axis0,
                                        final CS_AxisInfo                 axis1)
            throws RemoteException
        {
            try {
                return adapters.export(CoordinateSystemFactory.this.createProjectedCoordinateSystem(
                        name, adapters.wrap(gcs), adapters.wrap(projection), adapters.wrap(linearUnit),
                        adapters.wrap(axis0), adapters.wrap(axis1)));
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    throw (RemoteException) cause;
                }
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
        
        /**
         * Creates a projection.
         */
        public CS_Projection createProjection(final String name,
                                              final String wktProjectionClass,
                                              final CS_ProjectionParameter[] parameters)
            throws RemoteException
        {
            try {
                return adapters.export(CoordinateSystemFactory.this.createProjection(
                        name, wktProjectionClass, adapters.wrap(parameters)));
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    throw (RemoteException) cause;
                }
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
        
        /**
         * Creates horizontal datum from ellipsoid and Bursa-Wolf parameters.
         */
        public CS_HorizontalDatum createHorizontalDatum(final String       name,
                                                        final CS_DatumType horizontalDatumType,
                                                        final CS_Ellipsoid ellipsoid,
                                                        final CS_WGS84ConversionInfo toWGS84)
            throws RemoteException
        {
            try {
                return adapters.export(CoordinateSystemFactory.this.createHorizontalDatum(
                        name, (DatumType.Horizontal)adapters.wrap(horizontalDatumType),
                        adapters.wrap(ellipsoid), adapters.wrap(toWGS84)));
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    throw (RemoteException) cause;
                }
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
        
        /**
         * Creates a prime meridian, relative to Greenwich.
         */
        public CS_PrimeMeridian createPrimeMeridian(final String         name,
                                                    final CS_AngularUnit angularUnit,
                                                    final double         longitude)
            throws RemoteException
        {
            try {
                return adapters.export(CoordinateSystemFactory.this.createPrimeMeridian(
                        name, adapters.wrap(angularUnit), longitude));
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    throw (RemoteException) cause;
                }
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
        
        /**
         * Creates a GCS, which could be Lat/Lon or Lon/Lat.
         */
        public CS_GeographicCoordinateSystem createGeographicCoordinateSystem(
                                        final String             name,
                                        final CS_AngularUnit     angularUnit,
                                        final CS_HorizontalDatum horizontalDatum,
                                        final CS_PrimeMeridian   primeMeridian,
                                        final CS_AxisInfo        axis0,
                                        final CS_AxisInfo        axis1)
            throws RemoteException
        {
            try {
                return adapters.export(CoordinateSystemFactory.this.createGeographicCoordinateSystem(name, adapters.wrap(angularUnit), adapters.wrap(horizontalDatum), adapters.wrap(primeMeridian), adapters.wrap(axis0), adapters.wrap(axis1)));
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    throw (RemoteException) cause;
                }
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
        
        /**
         * Creates a local datum.
         */
        public CS_LocalDatum createLocalDatum(final String       name,
                                              final CS_DatumType localDatumType)
            throws RemoteException
        {
            try {
                return adapters.export(CoordinateSystemFactory.this.createLocalDatum(
                        name, (DatumType.Local)adapters.wrap(localDatumType)));
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    throw (RemoteException) cause;
                }
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
        
        /**
         * Creates a vertical datum from an enumerated type value.
         */
        public CS_VerticalDatum createVerticalDatum(final String       name,
                                                    final CS_DatumType verticalDatumType)
            throws RemoteException
        {
            try {
                return adapters.export(CoordinateSystemFactory.this.createVerticalDatum(
                        name, (DatumType.Vertical)adapters.wrap(verticalDatumType)));
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    throw (RemoteException) cause;
                }
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
        
        /**
         * Creates a vertical coordinate system from a datum and linear units.
         */
        public CS_VerticalCoordinateSystem createVerticalCoordinateSystem(
                                        final String           name,
                                        final CS_VerticalDatum verticalDatum,
                                        final CS_LinearUnit    verticalUnit,
                                        final CS_AxisInfo      axis)
            throws RemoteException
        {
            try {
                return adapters.export(CoordinateSystemFactory.this.createVerticalCoordinateSystem(
                        name, adapters.wrap(verticalDatum),
                        adapters.wrap(verticalUnit), adapters.wrap(axis)));
            } catch (FactoryException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    throw (RemoteException) cause;
                }
                throw new ServerException(exception.getLocalizedMessage(), exception);
            }
        }
    }
}
