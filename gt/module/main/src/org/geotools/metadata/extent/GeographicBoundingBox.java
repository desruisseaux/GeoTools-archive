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
package org.geotools.metadata.extent;

// J2SE direct dependencies


/**
 * Geographic position of the dataset. This is only an approximate
 * so specifying the co-ordinate reference system is unnecessary.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public class GeographicBoundingBox extends GeographicExtent
       implements org.opengis.metadata.extent.GeographicBoundingBox
{
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -3278089380004172514L;

    /**
     * The western-most coordinate of the limit of the
     * dataset extent. The value is expressed in longitude in
     * decimal degrees (positive east).
     */
    private double westBoundLongitude;

    /**
     * The eastern-most coordinate of the limit of the
     * dataset extent. The value is expressed in longitude in
     * decimal degrees (positive east).
     */
    private double eastBoundLongitude;

    /**
     * The southern-most coordinate of the limit of the
     * dataset extent. The value is expressed in latitude in
     * decimal degrees (positive north).
     */
    private double southBoundLatitude;

    /**
     * The northern-most, coordinate of the limit of the
     * dataset extent. The value is expressed in latitude in
     * decimal degrees (positive north).
     */
    private double northBoundLatitude;

    /**
     * Construct an initially empty geographic bounding box.
     */
    public GeographicBoundingBox() {
    }

    /**
     * Creates a geographic bounding box initialized to the specified values.
     */
    public GeographicBoundingBox(final double westBoundLongitude,
                                 final double eastBoundLongitude,
                                 final double southBoundLatitude,
                                 final double northBoundLatitude)
    {
        setWestBoundLongitude( westBoundLongitude);
        setEastBoundLongitude( eastBoundLongitude);
        setSouthBoundLatitude(southBoundLatitude );
        setNorthBoundLatitude(northBoundLatitude );
    }
    
    /**
     * Returns the western-most coordinate of the limit of the
     * dataset extent. The value is expressed in longitude in
     * decimal degrees (positive east).
     *
     * @return The western-most longitude between -180 and +180°.
     */
    public double getWestBoundLongitude() {
        return westBoundLongitude;
    }
    
    /**
     * Set the western-most coordinate of the limit of the
     * dataset extent. The value is expressed in longitude in
     * decimal degrees (positive east).
     */
    public synchronized void setWestBoundLongitude(final double newValue) {
        checkWritePermission();
        westBoundLongitude = newValue;
    }

    /**
     * Returns the eastern-most coordinate of the limit of the
     * dataset extent. The value is expressed in longitude in
     * decimal degrees (positive east).
     *
     * @return The eastern-most longitude between -180 and +180°.
     */
    public double getEastBoundLongitude() {
        return eastBoundLongitude;
    }

    /**
     * Set the eastern-most coordinate of the limit of the
     * dataset extent. The value is expressed in longitude in
     * decimal degrees (positive east).
     */
    public synchronized void setEastBoundLongitude(final double newValue) {
        checkWritePermission();
        eastBoundLongitude = newValue;
    }

    /**
     * Returns the southern-most coordinate of the limit of the
     * dataset extent. The value is expressed in latitude in
     * decimal degrees (positive north).
     *
     * @return The southern-most latitude between -90 and +90°.
     */
    public double getSouthBoundLatitude()  {
        return southBoundLatitude;
    }

    /**
     * Set the southern-most coordinate of the limit of the
     * dataset extent. The value is expressed in latitude in
     * decimal degrees (positive north).
     */
    public synchronized void setSouthBoundLatitude(final double newValue) {
        checkWritePermission();
        southBoundLatitude = newValue;
    }

    /**
     * Returns the northern-most, coordinate of the limit of the
     * dataset extent. The value is expressed in latitude in
     * decimal degrees (positive north).
     *
     * @return The northern-most latitude between -90 and +90°.
     */
    public double getNorthBoundLatitude()   {
        return northBoundLatitude;
    }

    /**
     * Set the northern-most, coordinate of the limit of the
     * dataset extent. The value is expressed in latitude in
     * decimal degrees (positive north).
     */
    public synchronized void setNorthBoundLatitude(final double newValue) {
        checkWritePermission();
        northBoundLatitude = newValue;
    }
    
    /**
     * Declare this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
    }

    /**
     * Compare this GeographicBoundingBox with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final GeographicBoundingBox that = (GeographicBoundingBox) object;
            return Double.doubleToLongBits(this.southBoundLatitude) ==
                   Double.doubleToLongBits(that.southBoundLatitude) &&
                   Double.doubleToLongBits(this.northBoundLatitude) ==
                   Double.doubleToLongBits(that.northBoundLatitude) &&
                   Double.doubleToLongBits(this.eastBoundLongitude) ==
                   Double.doubleToLongBits(that.eastBoundLongitude) &&
                   Double.doubleToLongBits(this.westBoundLongitude) ==
                   Double.doubleToLongBits(that.westBoundLongitude);
        }
        return false;
    }

    /**
     * Returns a hash code value for this extent.
     */
    public synchronized int hashCode() {
        long code = serialVersionUID;
        code ^=           Double.doubleToLongBits(southBoundLatitude);
        code  = 37*code + Double.doubleToLongBits(northBoundLatitude);
        code  = 37*code + Double.doubleToLongBits(eastBoundLongitude);
        code  = 37*code + Double.doubleToLongBits(westBoundLongitude);
        return (int)code ^ (int)(code >>> 32);
    }

    /**
     * Returns a string representation of this extent.
     *
     * @todo Provides a more elaborated implementation.
     */
    public String toString() {
        return super.toString();
    }    
}
