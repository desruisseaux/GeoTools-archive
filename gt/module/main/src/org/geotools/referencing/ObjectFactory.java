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

// J2SE dependencies and extensions
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import javax.units.ConversionException;
import javax.units.Unit;

// OpenGIS dependencies
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
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
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.Matrix;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.referencing.wkt.Parser;
import org.geotools.referencing.wkt.Symbols;


/**
 * Builds up complex objects from simpler objects or values. This factory allows
 * applications to make {@linkplain org.geotools.referencing.cs.CoordinateSystem coordinate systems},
 * {@linkplain org.geotools.referencing.crs.CoordinateReferenceSystem coordinate reference systems} or
 * {@linkplain org.geotools.referencing.datum.Datum} that cannot be created by an {@link AuthorityFactory}.
 * This factory is very flexible, whereas the authority factory is easier to use. So
 * {@link AuthorityFactory} can be used to make "standard" object, and <code>ObjectFactory</code>
 * can be used to make "special" objects.
 * <br><br>
 * Most methods expect a {@link Map} argument. The map is often (but is not required to be) a
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
 *     <td nowrap>&nbsp;{@link org.geotools.referencing.IdentifiedObject#NAME_PROPERTY "name"}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link String} or {@link Identifier}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link IdentifiedObject#getName}</td>
 *   </tr>
 *   <tr>
 *     <td nowrap>&nbsp;{@link org.geotools.referencing.IdentifiedObject#REMARKS_PROPERTY "remarks"}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link String} or {@link InternationalString}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link IdentifiedObject#getRemarks}</td>
 *   </tr>
 *   <tr>
 *     <td nowrap>&nbsp;{@link org.geotools.referencing.Identifier#AUTHORITY_PROPERTY "authority"}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link String} or {@link Citation}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link Identifier#getAuthority} on the {@linkplain IdentifiedObject#getName name}</td>
 *   </tr>
 *   <tr>
 *     <td nowrap>&nbsp;{@link org.geotools.referencing.Identifier#VERSION_PROPERTY "version"}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link String}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link Identifier#getVersion} on the {@linkplain IdentifiedObject#getName name}</td>
 *   </tr>
 *   <tr>
 *     <td nowrap>&nbsp;{@link org.geotools.referencing.IdentifiedObject#IDENTIFIERS_PROPERTY "identifiers"}&nbsp;</td>
 *     <td nowrap>&nbsp;{@link Identifier} or <code>{@linkplain Identifier}[]</code>&nbsp;</td>
 *     <td nowrap>&nbsp;{@link IdentifiedObject#getIdentifiers}</td>
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
public class ObjectFactory extends Factory implements CSFactory, DatumFactory, CRSFactory {
    /**
     * The object to use for parsing <cite>Well-Known Text</cite> (WKT) strings.
     * Will be created only when first needed.
     */
    private transient Parser parser;

    /**
     * The math transform factory to use for creating the conversion of projected CRS.
     * Will be fetched only when first needed.
     */
    private transient MathTransformFactory mtFactory;

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
    public ObjectFactory() {
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
            datum = new org.geotools.referencing.datum.GeodeticDatum(
                        properties, ellipsoid, primeMeridian);
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
    public TimeCS createTimeCS(Map            properties,
                               CoordinateSystemAxis axis) throws FactoryException
    {
        TimeCS cs;
        try {
            cs = new org.geotools.referencing.cs.TimeCS(properties, axis);
        } catch (IllegalArgumentException exception) {
            throw new FactoryException(exception);
        }
        cs = (TimeCS) canonicalize(cs);
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
        cs = (LinearCS) canonicalize(cs);
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
        cs = (UserDefinedCS) canonicalize(cs);
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
        cs = (UserDefinedCS) canonicalize(cs);
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
        crs = (ImageCRS) canonicalize(crs);
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
     * Creates a derived coordinate reference system. If the transformation is an affine
     * map performing a rotation, then any mixed axes must have identical units.
     * For example, a (<var>lat_deg</var>, <var>lon_deg</var>, <var>height_feet</var>)
     * system can be rotated in the (<var>lat</var>, <var>lon</var>) plane, since both
     * affected axes are in degrees.  But you should not rotate this coordinate system
     * in any other plane.
     *
     * @param  properties Name and other properties to give to the new object.
     *         Properties for the {@link org.geotools.referencing.operation.Conversion} object to
     *         be created can be specified with the <code>"conversion."</code> prefix added in
     *         front of property names (example: <code>"conversion.name"</code>).
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
     *         Properties for the {@link org.geotools.referencing.operation.Conversion} object to
     *         be created can be specified with the <code>"conversion."</code> prefix added in
     *         front of property names (example: <code>"conversion.name"</code>).
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
     *         Properties for the {@link org.geotools.referencing.operation.Conversion} object to
     *         be created can be specified with the <code>"conversion."</code> prefix added in
     *         front of property names (example: <code>"conversion.name"</code>).
     * @param  geoCRS Geographic coordinate reference system to base projection on.
     * @param  classification The classification name for the projection to be created
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
                                           String              classification,
                                           GeneralParameterValue[] parameters,
                                           CartesianCS                     cs)
            throws FactoryException
    {
        final ParameterValueGroup group = getDefaultProjectionParameters(classification);
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
     * Creates a projected coordinate reference system from a set of parameters. The classification
     * name is inferred either from the {@linkplain ParameterDescriptorGroup#getName parameter
     * group name}, or any other implementation dependent way.
     * <br><br>
     * The client must supply at least the <code>"semi_major"</code> and <code>"semi_minor"</code>
     * parameters for cartographic projection transforms. Example:
     *
     * <blockquote><pre>
     * ParameterValueGroup parameters = factory.{@linkplain #getDefaultProjectionParameters getDefaultProjectionParameters}("Transverse_Mercator");
     * p.parameter("semi_major").setValue(6378137.000);
     * p.parameter("semi_minor").setValue(6356752.314);
     * ProjectedCRS crs = factory.createProjectedCRS(..., parameters, ...);
     * </pre></blockquote>
     *
     * @param  properties Name and other properties to give to the new object.
     *         Available properties are {@linkplain ObjectFactory listed there}.
     *         Properties for the {@link Projection} object to be created can be specified
     *         with the <code>"conversion."</code> prefix added in front of property names
     *         (example: <code>"conversion.name"</code>).
     * @param  geoCRS Geographic coordinate reference system to base projection on.
     * @param  parameters The parameter values to give to the projection.
     * @param  cs The coordinate system for the projected CRS.
     * @throws FactoryException if the object creation failed.
     *
     * @see #getDefaultProjectionParameters
     */
    public ProjectedCRS createProjectedCRS(Map                 properties,
                                           GeographicCRS           geoCRS,
                                           ParameterValueGroup parameters,
                                           CartesianCS                 cs)
            throws FactoryException
    {
        // TODO: remove cast once we will be allowed to compile for J2SE 1.5.
        final EllipsoidalCS geoCS = (EllipsoidalCS) geoCRS.getCoordinateSystem();
        final Matrix swap1, swap3;
        try {
            swap1 = org.geotools.referencing.cs.EllipsoidalCS.swapAndScaleAxis(geoCS,
                    org.geotools.referencing.cs.EllipsoidalCS.GEODETIC_2D);
            swap3 = org.geotools.referencing.cs.CartesianCS.swapAndScaleAxis(
                    org.geotools.referencing.cs.CartesianCS.PROJECTED, cs);
        } catch (IllegalArgumentException cause) {
            // User-specified axis don't match.
            throw new FactoryException(cause);
        } catch (ConversionException cause) {
            // A Unit conversion is non-linear.
            throw new FactoryException(cause);
        }
        if (mtFactory == null) {
            mtFactory = FactoryFinder.getMathTransformFactory();
        }
        final MathTransform step1 = mtFactory.createAffineTransform(swap1);
        final MathTransform step2 = mtFactory.createParameterizedTransform(parameters);
        final MathTransform step3 = mtFactory.createAffineTransform(swap3);
        final MathTransform mt    = mtFactory.createConcatenatedTransform(
                                    mtFactory.createConcatenatedTransform(step1, step2), step3);
        return createProjectedCRS(properties, geoCRS, mt, cs);
    }

    /**
     * Returns the default parameter values for a projection of the given classification.
     * The classification may be the name of any operation method returned by the
     * {@link #getAvailableProjections} method. A typical example is
     * <code>"<A HREF="http://www.remotesensing.org/geotiff/proj_list/transverse_mercator.html">Transverse_Mercator</A>"</code>).
     * <br><br>
     * This method creates new parameter instances at every call.
     * It is intented to be modified by the user before to be passed to
     * <code>{@linkplain #createProjectedCRS(Map,GeographicCRS,ParameterValueGroup,CartesianCS)
     * createProjectedCRS}(..., parameters, ...)</code>.
     *
     * @param  classification The case insensitive classification to search for.
     * @return The default parameter values.
     * @throws NoSuchIdentifierException if there is no projection registered for the specified
     *         classification.
     *
     * @see #getAvailableProjections
     * @see #createProjectedCRS(Map,GeographicCRS,ParameterValueGroup,CartesianCS)
     *
     * @todo Check if the classification is a projection operation:
     */
    public ParameterValueGroup getDefaultProjectionParameters(String classification)
            throws NoSuchIdentifierException
    {
        if (mtFactory == null) {
            mtFactory = FactoryFinder.getMathTransformFactory();
        }
        return mtFactory.getDefaultParameters(classification);
    }

    /**
     * Returns a set of all available {@linkplain Projection projection} methods. For each
     * element in this set, the {@linkplain OperationMethod#getName operation method name}
     * is a classification name to be recognized by the {@link #getDefaultProjectionParameters}
     * method.
     *
     * @return All {@linkplain Projection projection} methods available in this factory.
     *
     * @see #getDefaultProjectionParameters
     * @see #createProjectedCRS(Map,GeographicCRS,ParameterValueGroup,CartesianCS)
     *
     * @todo Not yet implemented. We need to ask to the math transform factory, and then to
     *       filter the returned set to keep only the projections.
     */
    public Set/*<OperationMethod>*/ getAvailableProjections() {
        throw new UnsupportedOperationException();        
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
            parser = new Parser(Symbols.DEFAULT, this, this, this,
                                FactoryFinder.getMathTransformFactory());
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

    /**
     * Returns a canonical instance of the specified object. This method is invoked
     * after the creation of each immutable object. It maintains a pool of previously
     * created objects through {@linkplain java.lang.ref.WeakReference weak references}.
     *
     * @param  info The object to canonicalize.
     * @return An unique instance of the specified object.
     */
    private static Object canonicalize(final org.opengis.referencing.IdentifiedObject info) {
        return org.geotools.referencing.Identifier.POOL.canonicalize(info);
    }
}
