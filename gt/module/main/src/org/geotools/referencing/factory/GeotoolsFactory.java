/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le Développement
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
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.referencing.factory;

// J2SE dependencies and extensions
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ObjectFactory;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CompoundCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.DerivedCRS;
import org.opengis.referencing.crs.EngineeringCRS;
import org.opengis.referencing.crs.GeocentricCRS;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ImageCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.cs.AffineCS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.CylindricalCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.cs.LinearCS;
import org.opengis.referencing.cs.PolarCS;
import org.opengis.referencing.cs.SphericalCS;
import org.opengis.referencing.cs.TimeCS;
import org.opengis.referencing.cs.UserDefinedCS;
import org.opengis.referencing.cs.VerticalCS;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.ImageDatum;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.datum.TemporalDatum;
import org.opengis.referencing.datum.VerticalDatum;
import org.opengis.referencing.datum.VerticalDatumType;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.wkt.Parser;
import org.geotools.referencing.wkt.Symbols;
import org.geotools.util.WeakHashSet;



/**
 * Builds Geotools implementations of {@linkplain CoordinateReferenceSystem CRS},
 * {@linkplain CoordinateSystem CS} and {@linkplain Datum datum} objects. Most factory methods
 * expect properties given through a {@link Map} argument. The content of this map is described
 * in the {@link ObjectFactory} interface.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeotoolsFactory extends AbstractFactory
            implements CSFactory, DatumFactory, CRSFactory
{
    /**
     * The object to use for parsing <cite>Well-Known Text</cite> (WKT) strings.
     * Will be created only when first needed.
     */
    private transient Parser parser;

    /**
     * Set of weak references to existing objects (identifiers, CRS, Datum, whatever).
     * This set is used in order to return a pre-existing object instead of creating a
     * new one.
     */
    private final WeakHashSet pool = new WeakHashSet();

    /**
     * Construct a default factory. This method is public in order to allows instantiations
     * from a {@linkplain javax.imageio.spi.ServiceRegistry service registry}. Users should
     * not instantiate this factory directly, but use one of the following lines instead:
     *
     * <blockquote><pre>
     * {@linkplain DatumFactory} factory = FactoryFinder.{@linkplain FactoryFinder#getDatumFactory getDatumFactory()};
     * {@linkplain CSFactory}    factory = FactoryFinder.{@linkplain FactoryFinder#getCSFactory    getCSFactory()};
     * {@linkplain CRSFactory}   factory = FactoryFinder.{@linkplain FactoryFinder#getCRSFactory   getCRSFactory()};
     * </pre></blockquote>
     */
    public GeotoolsFactory() {
    }



    /////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                         ////////
    ////////                        D A T U M   F A C T O R Y                        ////////
    ////////                                                                         ////////
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an ellipsoid from radius values.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  semiMajorAxis Equatorial radius in supplied linear units.
     * @param  semiMinorAxis Polar radius in supplied linear units.
     * @param  unit Linear units of ellipsoid axes.
     * @throws FactoryException if the object creation failed.
     */
    public Ellipsoid createEllipsoid(Map    properties,
                                     double semiMajorAxis,
                                     double semiMinorAxis,
                                     Unit   unit) throws FactoryException
    {
        Ellipsoid ellipsoid;
        try {
            ellipsoid = org.geotools.referencing.datum.Ellipsoid.createEllipsoid(
                        properties, semiMajorAxis, semiMinorAxis, unit);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        ellipsoid = (Ellipsoid) pool.canonicalize(ellipsoid);
        return ellipsoid;
    }

    /**
     * Creates an ellipsoid from an major radius, and inverse flattening.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  semiMajorAxis Equatorial radius in supplied linear units.
     * @param  inverseFlattening Eccentricity of ellipsoid.
     * @param  unit Linear units of major axis.
     * @throws FactoryException if the object creation failed.
     */
    public Ellipsoid createFlattenedSphere(Map    properties,
                                           double semiMajorAxis,
                                           double inverseFlattening,
                                           Unit   unit) throws FactoryException
    {
        Ellipsoid ellipsoid;
        try {
            ellipsoid = org.geotools.referencing.datum.Ellipsoid.createFlattenedSphere(
                        properties, semiMajorAxis, inverseFlattening, unit);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        ellipsoid = (Ellipsoid) pool.canonicalize(ellipsoid);
        return ellipsoid;
    }

    /**
     * Creates a prime meridian, relative to Greenwich. 
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  longitude Longitude of prime meridian in supplied angular units East of Greenwich.
     * @param  angularUnit Angular units of longitude.
     * @throws FactoryException if the object creation failed.
     */
    public PrimeMeridian createPrimeMeridian(Map    properties,
                                             double longitude,
                                             Unit   angularUnit) throws FactoryException
    {
        PrimeMeridian meridian;
        try {
            meridian = new org.geotools.referencing.datum.PrimeMeridian(properties, longitude, angularUnit);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        meridian = (PrimeMeridian) pool.canonicalize(meridian);
        return meridian;
    }

    /**
     * Creates geodetic datum from ellipsoid and (optionaly) Bursa-Wolf parameters. 
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  ellipsoid Ellipsoid to use in new geodetic datum.
     * @param  primeMeridian Prime meridian to use in new geodetic datum.
     * @throws FactoryException if the object creation failed.
     */
    public GeodeticDatum createGeodeticDatum(Map           properties,
                                             Ellipsoid     ellipsoid,
                                             PrimeMeridian primeMeridian) throws FactoryException
    {
        GeodeticDatum datum;
        try {
            datum = new org.geotools.referencing.datum.GeodeticDatum(
                        properties, ellipsoid, primeMeridian);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        datum = (GeodeticDatum) pool.canonicalize(datum);
        return datum;
    }

    /**
     * Creates a vertical datum from an enumerated type value.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  type The type of this vertical datum (often geoidal).
     * @throws FactoryException if the object creation failed.
     */
    public VerticalDatum createVerticalDatum(Map         properties,
                                             VerticalDatumType type) throws FactoryException
    {
        VerticalDatum datum;
        try {
            datum = new org.geotools.referencing.datum.VerticalDatum(properties, type);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        datum = (VerticalDatum) pool.canonicalize(datum);
        return datum;
    }

    /**
     * Creates a temporal datum from an enumerated type value.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  origin The date and time origin of this temporal datum.
     * @throws FactoryException if the object creation failed.
     */
    public TemporalDatum createTemporalDatum(Map properties,
                                             Date origin) throws FactoryException
    {
        TemporalDatum datum;
        try {
            datum = new org.geotools.referencing.datum.TemporalDatum(properties, origin);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        datum = (TemporalDatum) pool.canonicalize(datum);
        return datum;
    }

    /**
     * Creates an engineering datum.
     *
     * @param  properties Name and other properties to give to the new object.
     * @throws FactoryException if the object creation failed.
     */
    public EngineeringDatum createEngineeringDatum(Map properties) throws FactoryException
    {
        EngineeringDatum datum;
        try {
            datum = new org.geotools.referencing.datum.EngineeringDatum(properties);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        datum = (EngineeringDatum) pool.canonicalize(datum);
        return datum;
    }

    /**
     * Creates an image datum.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  pixelInCell Specification of the way the image grid is associated
     *         with the image data attributes.
     * @throws FactoryException if the object creation failed.
     */
    public ImageDatum createImageDatum(Map         properties,
                                       PixelInCell pixelInCell) throws FactoryException
    {
        ImageDatum datum;
        try {
            datum = new org.geotools.referencing.datum.ImageDatum(properties, pixelInCell);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        datum = (ImageDatum) pool.canonicalize(datum);
        return datum;
    }



    /////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                         ////////
    ////////            C O O R D I N A T E   S Y S T E M   F A C T O R Y            ////////
    ////////                                                                         ////////
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a coordinate system axis from an abbreviation and a unit.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  abbreviation The coordinate axis abbreviation.
     * @param  direction The axis direction.
     * @param  unit The coordinate axis unit.
     * @throws FactoryException if the object creation failed.
     */
    public CoordinateSystemAxis createCoordinateSystemAxis(Map           properties,
                                                           String        abbreviation,
                                                           AxisDirection direction,
                                                           Unit          unit) throws FactoryException
    {
        CoordinateSystemAxis axis;
        try {
            axis = new org.geotools.referencing.cs.CoordinateSystemAxis(properties, abbreviation, direction, unit);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        axis = (CoordinateSystemAxis) pool.canonicalize(axis);
        return axis;
    }

    /**
     * Creates a two dimensional cartesian coordinate system from the given pair of axis.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis0 The first  axis.
     * @param  axis1 The second axis.
     * @throws FactoryException if the object creation failed.
     */
    public CartesianCS createCartesianCS(Map             properties,
                                         CoordinateSystemAxis axis0,
                                         CoordinateSystemAxis axis1) throws FactoryException
    {
        CartesianCS cs;
        try {
            cs = new org.geotools.referencing.cs.CartesianCS(properties, axis0, axis1);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (CartesianCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates a three dimensional cartesian coordinate system from the given set of axis.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis0 The first  axis.
     * @param  axis1 The second axis.
     * @param  axis2 The third  axis.
     * @throws FactoryException if the object creation failed.
     */
    public CartesianCS createCartesianCS(Map             properties,
                                         CoordinateSystemAxis axis0,
                                         CoordinateSystemAxis axis1,
                                         CoordinateSystemAxis axis2) throws FactoryException
    {
        CartesianCS cs;
        try {
            cs = new org.geotools.referencing.cs.CartesianCS(properties, axis0, axis1, axis2);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (CartesianCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates a two dimensional coordinate system from the given pair of axis.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis0 The first  axis.
     * @param  axis1 The second axis.
     * @throws FactoryException if the object creation failed.
     */
    public AffineCS createAffineCS(Map             properties,
                                   CoordinateSystemAxis axis0,
                                   CoordinateSystemAxis axis1) throws FactoryException
    {
        AffineCS cs;
        try {
            cs = new org.geotools.referencing.cs.AffineCS(properties, axis0, axis1);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (AffineCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates a three dimensional coordinate system from the given set of axis.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis0 The first  axis.
     * @param  axis1 The second axis.
     * @param  axis2 The third  axis.
     * @throws FactoryException if the object creation failed.
     */
    public AffineCS createAffineCS(Map             properties,
                                   CoordinateSystemAxis axis0,
                                   CoordinateSystemAxis axis1,
                                   CoordinateSystemAxis axis2) throws FactoryException
    {
        AffineCS cs;
        try {
            cs = new org.geotools.referencing.cs.AffineCS(properties, axis0, axis1, axis2);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (AffineCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates a polar coordinate system from the given pair of axis.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis0 The first  axis.
     * @param  axis1 The second axis.
     * @throws FactoryException if the object creation failed.
     */
    public PolarCS createPolarCS(Map             properties,
                                 CoordinateSystemAxis axis0,
                                 CoordinateSystemAxis axis1) throws FactoryException
    {
        PolarCS cs;
        try {
            cs = new org.geotools.referencing.cs.PolarCS(properties, axis0, axis1);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (PolarCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates a cylindrical coordinate system from the given polar CS and
     * perpendicular axis.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  polarCS The polar coordinate system.
     * @param  axis The perpendicular axis.
     * @throws FactoryException if the object creation failed.
     *
     * @deprecated Uses the method expecting 3 axis instead.
     */
    public CylindricalCS createCylindricalCS(Map            properties,
                                             PolarCS           polarCS,
                                             CoordinateSystemAxis axis) throws FactoryException
    {
        return createCylindricalCS(properties, polarCS.getAxis(0), polarCS.getAxis(1), axis);
    }

    /**
     * Creates a cylindrical coordinate system from the given set of axis.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis0 The first  axis.
     * @param  axis1 The second axis.
     * @param  axis2 The third  axis.
     * @throws FactoryException if the object creation failed.
     */
    public CylindricalCS createCylindricalCS(Map            properties,
                                             CoordinateSystemAxis axis0,
                                             CoordinateSystemAxis axis1,
                                             CoordinateSystemAxis axis2) throws FactoryException
    {
        CylindricalCS cs;
        try {
            cs = new org.geotools.referencing.cs.CylindricalCS(properties, axis0, axis1, axis2);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (CylindricalCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates a spherical coordinate system from the given set of axis.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis0 The first  axis.
     * @param  axis1 The second axis.
     * @param  axis2 The third  axis.
     * @throws FactoryException if the object creation failed.
     */
    public SphericalCS createSphericalCS(Map             properties,
                                         CoordinateSystemAxis axis0,
                                         CoordinateSystemAxis axis1,
                                         CoordinateSystemAxis axis2) throws FactoryException
    {
        SphericalCS cs;
        try {
            cs = new org.geotools.referencing.cs.SphericalCS(properties, axis0, axis1, axis2);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (SphericalCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates an ellipsoidal coordinate system without ellipsoidal height.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis0 The first  axis.
     * @param  axis1 The second axis.
     * @throws FactoryException if the object creation failed.
     */
    public EllipsoidalCS createEllipsoidalCS(Map             properties,
                                             CoordinateSystemAxis axis0,
                                             CoordinateSystemAxis axis1) throws FactoryException
    {
        EllipsoidalCS cs;
        try {
            cs = new org.geotools.referencing.cs.EllipsoidalCS(properties, axis0, axis1);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (EllipsoidalCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates an ellipsoidal coordinate system with ellipsoidal height.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis0 The first  axis.
     * @param  axis1 The second axis.
     * @param  axis2 The third  axis.
     * @throws FactoryException if the object creation failed.
     */
    public EllipsoidalCS createEllipsoidalCS(Map             properties,
                                             CoordinateSystemAxis axis0,
                                             CoordinateSystemAxis axis1,
                                             CoordinateSystemAxis axis2) throws FactoryException
    {
        EllipsoidalCS cs;
        try {
            cs = new org.geotools.referencing.cs.EllipsoidalCS(properties, axis0, axis1, axis2);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (EllipsoidalCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates a vertical coordinate system.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis The axis.
     * @throws FactoryException if the object creation failed.
     */
    public VerticalCS createVerticalCS(Map            properties,
                                       CoordinateSystemAxis axis) throws FactoryException
    {
        VerticalCS cs;
        try {
            cs = new org.geotools.referencing.cs.VerticalCS(properties, axis);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (VerticalCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates a temporal coordinate system.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis The axis.
     * @throws FactoryException if the object creation failed.
     */
    public TimeCS createTimeCS(Map            properties,
                               CoordinateSystemAxis axis) throws FactoryException
    {
        TimeCS cs;
        try {
            cs = new org.geotools.referencing.cs.TimeCS(properties, axis);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (TimeCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates a linear coordinate system.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis The axis.
     * @throws FactoryException if the object creation failed.
     */
    public LinearCS createLinearCS(Map            properties,
                                   CoordinateSystemAxis axis) throws FactoryException
    {
        LinearCS cs;
        try {
            cs = new org.geotools.referencing.cs.LinearCS(properties, axis);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (LinearCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates a two dimensional user defined coordinate system from the given pair of axis.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis0 The first  axis.
     * @param  axis1 The second axis.
     * @throws FactoryException if the object creation failed.
     */
    public UserDefinedCS createUserDefinedCS(Map             properties,
                                             CoordinateSystemAxis axis0,
                                             CoordinateSystemAxis axis1) throws FactoryException
    {
        UserDefinedCS cs;
        try {
            cs = new org.geotools.referencing.cs.UserDefinedCS(properties, axis0, axis1);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (UserDefinedCS) pool.canonicalize(cs);
        return cs;
    }

    /**
     * Creates a three dimensional user defined coordinate system from the given set of axis.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis0 The first  axis.
     * @param  axis1 The second axis.
     * @param  axis2 The third  axis.
     * @throws FactoryException if the object creation failed.
     */
    public UserDefinedCS createUserDefinedCS(Map             properties,
                                             CoordinateSystemAxis axis0,
                                             CoordinateSystemAxis axis1,
                                             CoordinateSystemAxis axis2) throws FactoryException
    {
        UserDefinedCS cs;
        try {
            cs = new org.geotools.referencing.cs.UserDefinedCS(properties, axis0, axis1, axis2);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (UserDefinedCS) pool.canonicalize(cs);
        return cs;
    }



    /////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                         ////////
    ////////  C O O R D I N A T E   R E F E R E N C E   S Y S T E M   F A C T O R Y  ////////
    ////////                                                                         ////////
    /////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Creates a compound coordinate reference system from an ordered
     * list of <code>CoordinateReferenceSystem</code> objects.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  elements ordered array of <code>CoordinateReferenceSystem</code> objects.
     * @throws FactoryException if the object creation failed.
     */
    public CompoundCRS createCompoundCRS(Map                       properties,
                                         CoordinateReferenceSystem[] elements) throws FactoryException
    {
        CompoundCRS crs;
        try {
            crs = new org.geotools.referencing.crs.CompoundCRS(properties, elements);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (CompoundCRS) pool.canonicalize(crs);
        return crs;
    }

    /**
     * Creates a engineering coordinate reference system. 
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  datum Engineering datum to use in created CRS.
     * @param  cs The coordinate system for the created CRS.
     * @throws FactoryException if the object creation failed.
     */
    public EngineeringCRS createEngineeringCRS(Map         properties,
                                               EngineeringDatum datum,
                                               CoordinateSystem    cs) throws FactoryException
    {
        EngineeringCRS crs;
        try {
            crs = new org.geotools.referencing.crs.EngineeringCRS(properties, datum, cs);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (EngineeringCRS) pool.canonicalize(crs);
        return crs;
    }
    
    /**
     * Creates an image coordinate reference system. 
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  datum Image datum to use in created CRS.
     * @param  cs The Cartesian or Oblique Cartesian coordinate system for the created CRS.
     * @throws FactoryException if the object creation failed.
     */
    public ImageCRS createImageCRS(Map    properties,
                                   ImageDatum  datum,
                                   AffineCS       cs) throws FactoryException
    {
        ImageCRS crs;
        try {
            crs = new org.geotools.referencing.crs.ImageCRS(properties, datum, cs);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (ImageCRS) pool.canonicalize(crs);
        return crs;
    }

    /**
     * Creates a temporal coordinate reference system. 
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  datum Temporal datum to use in created CRS.
     * @param  cs The Temporal coordinate system for the created CRS.
     * @throws FactoryException if the object creation failed.
     */
    public TemporalCRS createTemporalCRS(Map      properties,
                                         TemporalDatum datum,
                                         TimeCS           cs) throws FactoryException
    {
        TemporalCRS crs;
        try {
            crs = new org.geotools.referencing.crs.TemporalCRS(properties, datum, cs);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (TemporalCRS) pool.canonicalize(crs);
        return crs;
    }

    /**
     * Creates a vertical coordinate reference system. 
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  datum Vertical datum to use in created CRS.
     * @param  cs The Vertical coordinate system for the created CRS.
     * @throws FactoryException if the object creation failed.
     */
    public VerticalCRS createVerticalCRS(Map     properties,
                                         VerticalDatum datum,
                                         VerticalCS       cs) throws FactoryException
    {
        VerticalCRS crs;
        try {
            crs = new org.geotools.referencing.crs.VerticalCRS(properties, datum, cs);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (VerticalCRS) pool.canonicalize(crs);
        return crs;
    }

    /**
     * Creates a geocentric coordinate reference system from a {@linkplain CartesianCS
     * cartesian coordinate system}.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  datum Geodetic datum to use in created CRS.
     * @param  cs The cartesian coordinate system for the created CRS.
     * @throws FactoryException if the object creation failed.
     */
    public GeocentricCRS createGeocentricCRS(Map      properties,
                                             GeodeticDatum datum,
                                             CartesianCS      cs) throws FactoryException
    {
        GeocentricCRS crs;
        try {
            crs = new org.geotools.referencing.crs.GeocentricCRS(properties, datum, cs);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (GeocentricCRS) pool.canonicalize(crs);
        return crs;
    }

    /**
     * Creates a geocentric coordinate reference system from a {@linkplain SphericalCS
     * spherical coordinate system}.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  datum Geodetic datum to use in created CRS.
     * @param  cs The spherical coordinate system for the created CRS.
     * @throws FactoryException if the object creation failed.
     */
    public GeocentricCRS createGeocentricCRS(Map      properties,
                                             GeodeticDatum datum,
                                             SphericalCS      cs) throws FactoryException
    {
        GeocentricCRS crs;
        try {
            crs = new org.geotools.referencing.crs.GeocentricCRS(properties, datum, cs);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (GeocentricCRS) pool.canonicalize(crs);
        return crs;
    }

    /**
     * Creates a geographic coordinate reference system.
     * It could be <var>Latitude</var>/<var>Longitude</var> or
     * <var>Longitude</var>/<var>Latitude</var>.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  datum Geodetic datum to use in created CRS.
     * @param  cs The ellipsoidal coordinate system for the created CRS.
     * @throws FactoryException if the object creation failed.
     */
    public GeographicCRS createGeographicCRS(Map      properties,
                                             GeodeticDatum datum,
                                             EllipsoidalCS    cs) throws FactoryException
    {
        GeographicCRS crs;
        try {
            crs = new org.geotools.referencing.crs.GeographicCRS(properties, datum, cs);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (GeographicCRS) pool.canonicalize(crs);
        return crs;
    }

    /**
     * Creates a derived coordinate reference system. If the transformation is an affine
     * map performing a rotation, then any mixed axes must have identical units.
     * For example, a (<var>lat_deg</var>, <var>lon_deg</var>, <var>height_feet</var>)
     * system can be rotated in the (<var>lat</var>, <var>lon</var>) plane, since both
     * affected axes are in degrees.  But you should not rotate this coordinate system
     * in any other plane.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  base Coordinate reference system to base the derived CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the derived CRS.
     * @throws FactoryException if the object creation failed.
     *
     * @deprecated Use the method with an {@link OperationMethod} argument instead.
     */
    public DerivedCRS createDerivedCRS(Map                 properties,
                                       CoordinateReferenceSystem base,
                                       MathTransform    baseToDerived,
                                       CoordinateSystem     derivedCS) throws FactoryException
    {
        return createDerivedCRS(properties,
                    new org.geotools.referencing.operation.OperationMethod(baseToDerived),
                    base, baseToDerived, derivedCS);
    }

    /**
     * Creates a derived coordinate reference system. If the transformation is an affine
     * map performing a rotation, then any mixed axes must have identical units.
     * For example, a (<var>lat_deg</var>, <var>lon_deg</var>, <var>height_feet</var>)
     * system can be rotated in the (<var>lat</var>, <var>lon</var>) plane, since both
     * affected axes are in degrees.  But you should not rotate this coordinate system
     * in any other plane.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  method A description of the {@linkplain Conversion#getMethod method for the
     *         conversion}.
     * @param  base Coordinate reference system to base the derived CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the derived CRS.
     * @throws FactoryException if the object creation failed.
     */
    public DerivedCRS createDerivedCRS(Map                 properties,
                                       OperationMethod         method,
                                       CoordinateReferenceSystem base,
                                       MathTransform    baseToDerived,
                                       CoordinateSystem     derivedCS) throws FactoryException
    {
        DerivedCRS crs;
        try {
            crs = new org.geotools.referencing.crs.DerivedCRS(properties, method,
                                                              base, baseToDerived, derivedCS);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (DerivedCRS) pool.canonicalize(crs);
        return crs;
    }
    
    /**
     * Creates a projected coordinate reference system from a transform.
     * 
     * @param  properties Name and other properties to give to the new object.
     * @param  geoCRS Geographic coordinate reference system to base projection on.
     * @param  toProjected The transform from the geographic to the projected CRS.
     * @param  cs The coordinate system for the projected CRS.
     * @throws FactoryException if the object creation failed.
     *
     * @deprecated Use the method with an {@link OperationMethod} argument instead.
     */
    public ProjectedCRS createProjectedCRS(Map            properties,
                                           GeographicCRS      geoCRS,
                                           MathTransform toProjected,
                                           CartesianCS            cs) throws FactoryException
    {
        return createProjectedCRS(properties,
                    new org.geotools.referencing.operation.OperationMethod(toProjected),
                    geoCRS, toProjected, cs);
    }
    
    /**
     * Creates a projected coordinate reference system from a transform.
     * 
     * @param  properties Name and other properties to give to the new object.
     * @param  method A description of the {@linkplain Conversion#getMethod method for the
     *         projection}.
     * @param  base Geographic coordinate reference system to base projection on.
     * @param  baseToDerived The transform from the geographic to the projected CRS.
     * @param  derivedCS The coordinate system for the projected CRS.
     * @throws FactoryException if the object creation failed.
     */
    public ProjectedCRS createProjectedCRS(Map              properties,
                                           OperationMethod      method,
                                           GeographicCRS          base,
                                           MathTransform baseToDerived,
                                           CartesianCS       derivedCS) throws FactoryException
    {
        ProjectedCRS crs;
        try {
            crs = new org.geotools.referencing.crs.ProjectedCRS(properties, method,
                                                                base, baseToDerived, derivedCS);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (ProjectedCRS) pool.canonicalize(crs);
        return crs;
    }

    /**
     * Creates a projected coordinate reference system from a projection name.
     * 
     * @param  properties Name and other properties to give to the new object.
     * @param  geoCRS Geographic coordinate reference system to base projection on.
     * @param  method The method name for the projection to be created
     *         (e.g. "Transverse_Mercator", "Mercator_1SP", "Oblique_Stereographic", etc.).
     * @param  parameters The parameter values to give to the projection. May includes
     *         "central_meridian", "latitude_of_origin", "scale_factor", "false_easting",
     *         "false_northing" and any other parameters specific to the projection.
     * @param  cs The coordinate system for the projected CRS.
     * @throws FactoryException if the object creation failed.
     *
     * @deprecated Replaced by {@link #createProjectedCRS(Map,GeographicCRS,ParameterValueGroup,CartesianCS)}
     *             for concistency with the rest of the API, which work with {@link ParameterValueGroup}
     *             rather than an array of {@link GeneralParameterValue}.
     */
    public ProjectedCRS createProjectedCRS(Map                     properties,
                                           GeographicCRS               geoCRS,
                                           String                      method,
                                           GeneralParameterValue[] parameters,
                                           CartesianCS                     cs)
            throws FactoryException
    {
        final ParameterValueGroup group = getDefaultParameters(method);
        for (int i=0; i<parameters.length; i++) {
            final GeneralParameterValue gp = parameters[i];
            if (gp instanceof ParameterValue) {
                final ParameterValue p = (ParameterValue) gp;
                group.parameter(p.getDescriptor().getName().getCode()).setValue(p.getValue());
            } else {
                throw new UnsupportedOperationException();        
            }
        }
        return createProjectedCRS(properties, geoCRS, group, cs);
    }

    /**
     * Creates a projected coordinate reference system from a set of parameters.
     * The client must supply at least the <code>"semi_major"</code> and <code>"semi_minor"</code>
     * parameters for cartographic projection.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  base Geographic coordinate reference system to base projection on.
     * @param  parameters The parameter values to give to the projection.
     * @param  derivedCS The coordinate system for the projected CRS.
     * @throws FactoryException if the object creation failed.
     *
     * @see #getDefaultParameters
     *
     * @deprecated Use {@link FactoryGroup#createProjectedCRS} instead.
     */
    public ProjectedCRS createProjectedCRS(Map                 properties,
                                           GeographicCRS             base,
                                           ParameterValueGroup parameters,
                                           CartesianCS          derivedCS)
            throws FactoryException
    {
        return new FactoryGroup(this, this, this, null).createProjectedCRS(
                                properties, base, null, parameters, derivedCS);
    }

    /**
     * Returns the default parameter values for a derived or projected CRS using the given method.
     *
     * @param  method The case insensitive name of the method to search for.
     * @return The default parameter values.
     * @throws NoSuchIdentifierException if there is no operation registered for the specified method.
     *
     * @deprecated This method will be removed.
     */
    public ParameterValueGroup getDefaultParameters(final String method)
            throws NoSuchIdentifierException
    {
        return FactoryFinder.getMathTransformFactory().getDefaultParameters(method);
    }

    /**
     * Creates a coordinate reference system object from a XML string.
     *
     * @param  xml Coordinate reference system encoded in XML format.
     * @throws FactoryException if the object creation failed.
     *
     * @todo Not yet implemented.
     */
    public CoordinateReferenceSystem createFromXML(String xml) throws FactoryException {
        throw new FactoryException("Not yet implemented");
    }

    /**
     * Creates a coordinate reference system object from a string.
     * The <A HREF="../doc-files/WKT.html">definition for WKT</A>
     * is shown using Extended Backus Naur Form (EBNF).
     *
     * @param  wkt Coordinate system encoded in Well-Known Text format.
     * @throws FactoryException if the object creation failed.
     */
    public synchronized CoordinateReferenceSystem createFromWKT(final String wkt)
            throws FactoryException
    {
        // Note: while this factory is thread safe, the WKT parser is not.
        //       Since we share a single instance of this parser, we must
        //       synchronize.
        if (parser == null) {
            final Hints hints = new Hints(getImplementationHints());
            parser = new Parser(Symbols.DEFAULT, FactoryFinder.getDatumFactory(hints), this, this,
                                                 FactoryFinder.getMathTransformFactory(hints));
        }
        try {
            return parser.parseCoordinateReferenceSystem(wkt);
        } catch (ParseException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof FactoryException) {
                throw (FactoryException) cause;
            }
            throw new FactoryException(exception);
        }
    }
}
