/* This file is part of the GeoWidgets project.
 * (C) 2005, Matthias Basler
 * This library is distributed under the GNU Lesser General Public License.
 * See http://www.opensource.org/licenses/lgpl-license.php or read
 * the license file for details.
 */
package org.geowidgets.crs.model;

import java.util.List;

import javax.units.Unit;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.*;
import org.opengis.referencing.datum.*;
import org.opengis.referencing.operation.Conversion;

/** An interface for models for the CRS assembly widgets. The implementations
 * of this interface care about tasks such as getting default objects, getting
 * the names of available objects and getting the objects for their name.
 * 
 * @author Matthias Basler
 */
public interface ICRSModel {
    /** @return a list of all known and supported object of the specified type.
     * @param cl the object's class
     * @param hint hints in cases where the object's class is not sufficient.
     * Example: "2D" and "3D" to distinguish between 2D and 3D objects.
     * @throws FactoryException if problems ocurred accessing the underlying database. */
    public List<EPSGEntry> getSupportedObjects(final Class<? extends IdentifiedObject> cl,
            final String hint) throws FactoryException;

    /** @return an EPSG entry describing default object of the specified type.
     * @param cl the object's class
     * @param hint hints in cases where the object's class is not sufficient
     * Example: "2D" and "3D" to distinguish between 2D and 3D geographic CRS. */
    public EPSGEntry getDefaultEntry(final Class<? extends IdentifiedObject> cl,
            final String hint);

    /** @param <T> Some CRS-related object type
     * @return the default object itself for the specified type.
     * @param cl the object's class
     * @param hint hints in cases where the object's class is not sufficient
     * Example: "2D" and "3D" to distinguish between 2D and 3D geographic CRS. 
     * @throws FactoryException if creating the object failed. */
    public <T extends IdentifiedObject> T getDefaultObject(final Class<T> cl,
            final String hint) throws FactoryException;
    
    /** @return a multiline description of the object given by the code.
     * @param cl the object's class, which is specified only to speed up the
     * object lookup and which is usually known.
     * @param objCode the name of the object of interest
     * @throws FactoryException if problems ocurred accessing the underlying database. */
    public String getFormattedDescription(final Class<? extends IdentifiedObject> cl,
            final String objCode) throws FactoryException;

    /** Returns the dropdown entry for any given crs (& Co.) object. <p>
     * Tries to fetch code and name from the object <ul>
     * <li> If both can be derived, return the correct EPSGEntry. </li>
     * <li> If no code is available, return an EPSGEntry with code <code>\"\"</code>
     * and the object's name. </li>
     * <li> If no name is available and the name cannot be derived from the code
     * or if neither code nor name are available, return <code>ENTRY_CUSTOM</code>.
     * This applies for all non-EPSG objects.  </li> </ul>
     * @return the dropdown entry for the object or code>ENTRY_CUSTOM</code>
     * in case of non-EPSG objects.
     * @param o some georeferencing-related object, such as CRS, CS, datums, ... */
    public EPSGEntry getEntryFor(final IdentifiedObject o);

    /** @return an ellipsoid created from its parameters. Convenience method that
     * accepts Strings and checks them. EITHER b OR invFlat must be specified.
     * @param name a name to give to the custom ellipsoid
     * @param a semimajor axis
     * @param b semiminor axis
     * @param invFlat inverse flattening
     * @param lUnit linear unit
     * @throws FactoryException if the construction of the object failed */
    public Ellipsoid createEllipsoid(final String name, final String a,
            final String b, final String invFlat, final Unit lUnit)
            throws FactoryException;

    /** @return an ellipsoid created from its parameters.
     * EITHER b OR invFlat must be specified (nun-null).
     * @param name a name to give to the custom ellipsoid
     * @param a semimajor axis
     * @param b semiminor axis
     * @param invFlat inverse flattening
     * @param lUnit  linear unit
     * @throws FactoryException if the construction of the object failed */
    public Ellipsoid createEllipsoid(final String name, final double a,
            final double b, final double invFlat, final Unit lUnit)
            throws FactoryException;

    /** @return an ellipsoid created from its object code
     * @param objCode the code, as defined by the authority (database). 
     * @throws FactoryException if the construction of the object failed */
    public Ellipsoid createEllipsoid(final String objCode)
            throws FactoryException;

    /** @return a prime meridian created from its parameters.
     * @param name a name to give to the custom ellipsoid
     * @param lon longitude of the prime meridian
     * @param aUnit angular unit
     * @throws FactoryException if the construction of the object failed */
    public PrimeMeridian createPrimeMeridian(final String name,
            final double lon, final Unit aUnit) throws FactoryException;

    /** @return an prime meridian created from its object code
     * @param objCode the code, as defined by the authority (database).
     * @throws FactoryException if the construction of the object failed */
    public PrimeMeridian createPrimeMeridian(final String objCode)
            throws FactoryException;

    /** @return a geodetic datum created from its parameters.
     * @param name a name to give to the custom ellipsoid
     * @param el the ellipsoid to use
     * @param pm the prime meridian to use
     * @throws FactoryException if the construction of the object failed */
    public GeodeticDatum createGeodeticDatum(final String name,
            final Ellipsoid el, final PrimeMeridian pm) throws FactoryException;

    /** @return a geodetic datum created from its object code
     * @param objCode the code, as defined by the authority (database).
     * @throws FactoryException if the construction of the object failed */
    public GeodeticDatum createGeodeticDatum(final String objCode)
            throws FactoryException;

    /** @return a coordinate axis created from its parameters.
     * @param name a name to give to the custom ellipsoid
     * @param abb abbreviation (such as "N" or "E")
     * @param direction the direction the axis is pointing to
     * @param unit the unit to use (linear/angular)
     * @throws FactoryException if the construction of the object failed */
    public CoordinateSystemAxis createCoordinateSystemAxis(final String name,
            final String abb, final AxisDirection direction, final Unit unit)
            throws FactoryException;

    //public CoordinateSystemAxis createCoordinateSystemAxis(final String epsgName) throws FactoryException;

    /** @return a 2D ellipsoidal coordinate system created from its parameters.
     * @param name a name to give to the custom ellipsoid
     * @param axis0 definition of the first axis
     * @param axis1 definition of the second axis
     * @throws FactoryException if the construction of the object failed */
    public EllipsoidalCS createEllipsoidalCS(final String name,
            final CoordinateSystemAxis axis0, final CoordinateSystemAxis axis1)
            throws FactoryException;

    /** @return a 3D ellipsoidal coordinate system created from its parameters.
     * @param name a name to give to the custom ellipsoid
     * @param axis0 definition of the first axis
     * @param axis1 definition of the second axis
     * @param axis2 definition of the thrid axis
     * @throws FactoryException if the construction of the object failed */
    public EllipsoidalCS createEllipsoidalCS(final String name,
            final CoordinateSystemAxis axis0, final CoordinateSystemAxis axis1,
            final CoordinateSystemAxis axis2) throws FactoryException;

    /** @return an ellipsoidal coordinate system created from its object code
     * @param objCode the code, as defined by the authority (database).
     * @throws FactoryException if the construction of the object failed */
    public EllipsoidalCS createEllipsoidalCS(final String objCode)
            throws FactoryException;

    /** @return a 2D cartesian coordinate system created from its parameters.
     * @param name a name to give to the custom ellipsoid
     * @param axis0 definition of the first axis
     * @param axis1 definition of the second axis
     * @throws FactoryException if the construction of the object failed */
    public CartesianCS createCartesianCS(final String name,
            final CoordinateSystemAxis axis0, final CoordinateSystemAxis axis1)
            throws FactoryException;

    /** @return a 3D cartesian coordinate system created from its parameters.
     * @param name a name to give to the custom ellipsoid
     * @param axis0 definition of the first axis
     * @param axis1 definition of the second axis
     * @param axis2 definition of the thrid axis
     * @throws FactoryException if the construction of the object failed */
    public CartesianCS createCartesianCS(final String name,
            final CoordinateSystemAxis axis0, final CoordinateSystemAxis axis1,
            final CoordinateSystemAxis axis2) throws FactoryException;

    /** @return an cartesian coordinate system created from its object code
     * @param objCode the code, as defined by the authority (database).
     * @throws FactoryException if the construction of the object failed */
    public CartesianCS createCartesianCS(final String objCode)
            throws FactoryException;

    /** @return a geographic CRS created from its parameters.
     * @param name a name to give to the custom ellipsoid
     * @param gd the geodetic datum to use
     * @param eCS the ellisoidal coordinate system to use
     * @throws FactoryException if the construction of the object failed */
    public GeographicCRS createGeographicCRS(final String name,
            final GeodeticDatum gd, final EllipsoidalCS eCS)
            throws FactoryException;

    /** @return a geographic CRS created from its object name
     * @param objCode the code, as defined by the authority (database).
     * @throws FactoryException if the construction of the object failed */
    public GeographicCRS createGeographicCRS(final String objCode)
            throws FactoryException;

    /** @return a conversion (in this case likely a projection) created from its object name
     * @param objCode the conversion's code, as defined by the authority (database).
     * @throws FactoryException if the construction of the object failed */
    public Conversion createConversion(final String objCode)
            throws FactoryException;

    /** @return a projected CRS created from its parameters.
     * @param name a name to give to the custom ellipsoid
     * @param gCRS the geographic CRS that serves as base for the projection
     * @param cCS the cartesian coordinate system to use
     * @param conv the projection to use
     * @throws FactoryException if the construction of the object failed */
    public ProjectedCRS createProjectedCRS(final String name,
            final GeographicCRS gCRS, final CartesianCS cCS,
            final Conversion conv) throws FactoryException;

    /** @return a projected CRS created from its object code
     * @param objCode the CRS's code, as defined by the authority (database).
     * @throws FactoryException if the construction of the object failed */
    public ProjectedCRS createProjectedCRS(final String objCode)
            throws FactoryException;
    
    /** @return an object created from its object code. This function can be
     * use if the EPSG object class is unknown or doesn't matter. However this
     * function is usually slower than the specific functions, such as 
     * {@linkplain #createEllipsoid(String)} or {@linkplain #createCartesianCS(String)}
     * @param objCode the object's code, as defined by the authority (database).
     * @throws FactoryException if the construction of the object failed */
    public IdentifiedObject createObject(String objCode) throws FactoryException;   
}
