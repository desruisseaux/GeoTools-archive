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
package org.geotools.referencing;

// J2SE dependencies
import java.util.Map;
import java.util.Date;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.referencing.cs.*;
import org.opengis.referencing.crs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.metadata.citation.Citation;


/**
 * Builds up complex objects from simpler objects or values. <code>Factory</code> allows
 * applications to make {@linkplain org.geotools.referencing.cs.CoordinateSystem coordinate systems},
 * {@linkplain org.geotools.referencing.crs.CoordinateReferenceSystem coordinate reference systems} or
 * {@linkplain org.geotools.referencing.datum.Datum} that cannot be created by an {@link AuthorityFactory}.
 * This factory is very flexible, whereas the authority factory is easier to use. So
 * {@link AuthorityFactory} can be used to make "standard" object, and <code>Factory</code>
 * can be used to make "special" objects.
 *
 * <P>Most methods expect a {@link Map} argument. The map is often (but is not required to be) a
 * {@link java.util.Properties} instance. The map shall contains at least a <code>"name"</code>
 * property. In the common case where the name is the only property, the map may be constructed with
 * <code>Collections.{@linkplain java.util.Collections#singletonMap singletonMap}("name",
 * <var>theName</var>)</code> where <var>theName</var> is an arbitrary name as free text.
 * The properties listed in the following table are also recongnized. Property names are
 * case-insensitive and trailing and leading spaces are ignored.
 *
 * <table border='1'>
 *   <tr bgcolor="#CCCCFF" class="TableHeadingColor">
 *     <th nowrap>Property name</th>
 *     <th nowrap>Value type</th>
 *     <th nowrap>Value given to</th>
 *   </tr>
 *   <tr>
 *     <td nowrap>&nbsp;<code>"name"</code>&nbsp;</td>
 *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link Info#getName}</td>
 *   </tr>
 *   <tr>
 *     <td nowrap>&nbsp;<code>"remarks"</code>&nbsp;</td>
 *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link Info#getRemarks}</td>
 *   </tr>
 *   <tr>
 *     <td nowrap>&nbsp;<code>"authority"</code>&nbsp;</td>
 *     <td nowrap>&nbsp;{@link String} or {@link org.opengis.metadata.citation.Citation}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link Identifier#getAuthority} on the first identifier</td>
 *   </tr>
 *   <tr>
 *     <td nowrap>&nbsp;<code>"code"</code>&nbsp;</td>
 *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link Identifier#getCode} on the first identifier</td>
 *   </tr>
 *   <tr>
 *     <td nowrap>&nbsp;<code>"codeSpace"</code>&nbsp;</td>
 *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link Identifier#getCodeSpace} on the first identifier</td>
 *   </tr>
 *   <tr>
 *     <td nowrap>&nbsp;<code>"version"</code>&nbsp;</td>
 *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link Identifier#getVersion} on the first identifier</td>
 *   </tr>
 *   <tr>
 *     <td nowrap>&nbsp;<code>"identifiers"</code>&nbsp;</td>
 *     <td nowrap>&nbsp;<code>{@linkplain Identifier}</code>[]&nbsp;</td>
 *     <td nowrap>&nbsp;{@link Info#getIdentifiers}</td>
 *   </tr>
 * </table>
 *
 * <P>The <code>"name"</code> property is mandatory. All others are optional. Additionally, all
 * localizable attributes like <code>"name"</code> and <code>"remarks"</code> may have a language
 * and country code suffix. For example the <code>"remarks_fr"</code> property stands for remarks
 * in {@linkplain java.util.Locale#FRENCH French} and the <code>"remarks_fr_CA"</code> property
 * stands for remarks in {@linkplain java.util.Locale#CANADA_FRENCH French Canadian}.</P>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Factory implements CSFactory, DatumFactory, CRSFactory {
    /**
     * The citation for Geotools 2.
     */
    private static final Citation VENDOR = new org.geotools.metadata.citation.Citation("Geotools 2");

    /**
     * Construct a default factory. This method is public in order to allows instantiations
     * from a {@linkplain javax.imageio.spi.ServiceRegistry service registry}. Users should
     * not instantiate this factory directly, but use one of the following lines instead:
     *
     * <blockquote><pre>
     * {@linkplain DatumFactory} factory = FactoryFinder.{@linkplain FactoryFinder#getDatumFactory getDatumFactory()};
     * {@linkplain CSFactory}    factory = FactoryFinder.{@linkplain FactoryFinder#getCSFactory    getCSFactory()};
     * </pre></blockquote>
     */
    public Factory() {
    }

    /**
     * Returns the vendor responsible for creating this factory implementation.
     * Many implementations may be available for the same factory interface.
     * The default implementation returns "Geotools 2".
     *
     * @return The vendor for this factory implementation.
     */
    public Citation getVendor() {
        return VENDOR;
    }

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
        axis = (CoordinateSystemAxis) canonicalize(axis);
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
        cs = (CartesianCS) canonicalize(cs);
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
        cs = (CartesianCS) canonicalize(cs);
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
        cs = (PolarCS) canonicalize(cs);
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
     */
    public CylindricalCS createCylindricalCS(Map            properties,
                                             PolarCS           polarCS,
                                             CoordinateSystemAxis axis) throws FactoryException
    {
        CylindricalCS cs;
        try {
            cs = new org.geotools.referencing.cs.CylindricalCS(properties, polarCS, axis);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (CylindricalCS) canonicalize(cs);
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
        cs = (SphericalCS) canonicalize(cs);
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
        cs = (EllipsoidalCS) canonicalize(cs);
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
        cs = (EllipsoidalCS) canonicalize(cs);
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
        cs = (VerticalCS) canonicalize(cs);
        return cs;
    }

    /**
     * Creates a temporal coordinate system.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  axis The axis.
     * @throws FactoryException if the object creation failed.
     */
    public TemporalCS createTemporalCS(Map            properties,
                                       CoordinateSystemAxis axis) throws FactoryException
    {
        TemporalCS cs;
        try {
            cs = new org.geotools.referencing.cs.TemporalCS(properties, axis);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (TemporalCS) canonicalize(cs);
        return cs;
    }

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
        ellipsoid = (Ellipsoid) canonicalize(ellipsoid);
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
        ellipsoid = (Ellipsoid) canonicalize(ellipsoid);
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
        meridian = (PrimeMeridian) canonicalize(meridian);
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
            datum = new org.geotools.referencing.datum.GeodeticDatum(properties, ellipsoid, primeMeridian);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        datum = (GeodeticDatum) canonicalize(datum);
        return datum;
    }

    /**
     * Creates a vertical datum from an enumerated type value.
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  type The type of this vertical datum (often “geoidal”).
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
        datum = (VerticalDatum) canonicalize(datum);
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
        datum = (TemporalDatum) canonicalize(datum);
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
        datum = (EngineeringDatum) canonicalize(datum);
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
        datum = (ImageDatum) canonicalize(datum);
        return datum;
    }

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
        crs = (CompoundCRS) canonicalize(crs);
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
        crs = (EngineeringCRS) canonicalize(crs);
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
        crs = (GeocentricCRS) canonicalize(crs);
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
        crs = (GeocentricCRS) canonicalize(crs);
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
        crs = (GeographicCRS) canonicalize(crs);
        return crs;
    }
    
    /**
     * Creates an image coordinate reference system. 
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  datum Image datum to use in created CRS.
     * @param  cs The Cartesian or Oblique Cartesian coordinate system for the created CRS.
     * @throws FactoryException if the object creation failed.
     *
     * @deprecated Provided for compatibility with GeoAPI 1.0, but will be removed in GeoAPI 1.1.
     */
    public ImageCRS createImageCRS(Map      properties,
                                   ImageDatum    datum,
                                   CoordinateSystem cs) throws FactoryException
    {
        return createImageCRS(properties, datum, (ObliqueCartesianCS) cs);
    }
    
    /**
     * Creates an image coordinate reference system. 
     *
     * @param  properties Name and other properties to give to the new object.
     * @param  datum Image datum to use in created CRS.
     * @param  cs The Cartesian or Oblique Cartesian coordinate system for the created CRS.
     * @throws FactoryException if the object creation failed.
     */
    public ImageCRS createImageCRS(Map        properties,
                                   ImageDatum      datum,
                                   ObliqueCartesianCS cs) throws FactoryException
    {
        ImageCRS crs;
        try {
            crs = new org.geotools.referencing.crs.ImageCRS(properties, datum, cs);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (ImageCRS) canonicalize(crs);
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
     *         Properties for the {@link Conversion} object to be created can be specified
     *         with the <code>"conversion."</code> prefix added in front of property names
     *         (example: <code>"conversion.name"</code>).
     * @param  base Coordinate reference system to base the derived CRS on.
     * @param  baseToDerived The transform from the base CRS to returned CRS.
     * @param  derivedCS The coordinate system for the derived CRS. The number
     *         of axes must match the target dimension of the transform
     *         <code>baseToDerived</code>.
     * @throws FactoryException if the object creation failed.
     */
    public DerivedCRS createDerivedCRS(Map                 properties,
                                       CoordinateReferenceSystem base,
                                       MathTransform    baseToDerived,
                                       CoordinateSystem     derivedCS) throws FactoryException
    {
        DerivedCRS crs;
        try {
            crs = new org.geotools.referencing.crs.DerivedCRS(properties, base, baseToDerived, derivedCS);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (DerivedCRS) canonicalize(crs);
        return crs;
    }
                                       
    
    /**
     * Creates a projected coordinate reference system from a transform.
     * 
     * @param  properties Name and other properties to give to the new object.
     *         Properties for the {@link Conversion} object to be created can be specified
     *         with the <code>"conversion."</code> prefix added in front of property names
     *         (example: <code>"conversion.name"</code>).
     * @param  geoCRS Geographic coordinate reference system to base projection on.
     * @param  toProjected The transform from the geographic to the projected CRS.
     * @param  cs The coordinate system for the projected CRS.
     * @throws FactoryException if the object creation failed.
     */
    public ProjectedCRS createProjectedCRS(Map            properties,
                                           GeographicCRS      geoCRS,
                                           MathTransform toProjected,
                                           CartesianCS            cs) throws FactoryException
    {
        ProjectedCRS crs;
        try {
            crs = new org.geotools.referencing.crs.ProjectedCRS(properties, geoCRS, toProjected, cs);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (ProjectedCRS) canonicalize(crs);
        return crs;
    }

    /**
     * Creates a projected coordinate reference system from a projection name.
     * 
     * @param  properties Name and other properties to give to the new object.
     *         Properties for the {@link Conversion} object to be created can be specified
     *         with the <code>"conversion."</code> prefix added in front of property names
     *         (example: <code>"conversion.name"</code>).
     * @param  geoCRS Geographic coordinate reference system to base projection on.
     * @param  projectionName The classification name for the projection to be created
     *         (e.g. "Transverse_Mercator", "Mercator_1SP", "Oblique_Stereographic", etc.).
     * @param  parameterValues The parameter value to give to the projection. Should includes
     *         "central_meridian", "latitude_of_origin", "scale_factor", "false_easting",
     *         "false_northing" and any other parameters specific to the projection.
     * @param  cs The coordinate system for the projected CRS.
     * @throws FactoryException if the object creation failed.
     */
    public ProjectedCRS createProjectedCRS(Map                          properties,
                                           GeographicCRS                    geoCRS,
                                           String                   projectionName,
                                           GeneralParameterValue[] parameterValues,
                                           CartesianCS                          cs) throws FactoryException
    {
        ProjectedCRS crs;
        try {
            crs = new org.geotools.referencing.crs.ProjectedCRS(properties, geoCRS, projectionName, parameterValues, cs);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (ProjectedCRS) canonicalize(crs);
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
                                         TemporalCS       cs) throws FactoryException
    {
        TemporalCRS crs;
        try {
            crs = new org.geotools.referencing.crs.TemporalCRS(properties, datum, cs);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        crs = (TemporalCRS) canonicalize(crs);
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
        crs = (VerticalCRS) canonicalize(crs);
        return crs;
    }

    /**
     * Creates a coordinate reference system object from a XML string.
     *
     * @param  xml Coordinate reference system encoded in XML format.
     * @throws FactoryException if the object creation failed.
     */
    public CoordinateReferenceSystem createFromXML(String xml) throws FactoryException {
        // TODO: Not yet implemented.
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
    public CoordinateReferenceSystem createFromWKT(String wkt) throws FactoryException {
        // TODO: Code not yet ported from legact CTS code.
        throw new FactoryException("Not yet implemented");
    }

    /**
     * Returns a canonical instance of the specified object. This method is invoked
     * after the creation of each immutable object. It maintains a pool of previously
     * created objects through {@linkplain java.lang.ref.WeakReference weak references}.
     *
     * @param  info The object to canonicalize.
     * @return An unique instance of the specified object.
     */
    private static Object canonicalize(final org.opengis.referencing.Info info) {
        return Identifier.POOL.canonicalize(info);
    }
}
