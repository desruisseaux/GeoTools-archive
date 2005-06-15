/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2004, Geotools Project Managment Committee (PMC)
 * (C) 2004, Institut de Recherche pour le D�veloppement
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
package org.geotools.metadata.iso.extent;

// J2SE dependencies
import java.awt.geom.Rectangle2D;

// OpenGIS dependencies
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.measure.AngleFormat;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Geographic position of the dataset. This is only an approximate
 * so specifying the co-ordinate reference system is unnecessary.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Toura�vane
 *
 * @since 2.1
 */
public class GeographicBoundingBoxImpl extends GeographicExtentImpl
        implements GeographicBoundingBox
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
     * Constructs an initially empty geographic bounding box.
     */
    public GeographicBoundingBoxImpl() {
    }

    /**
     * Constructs a geographic bounding box from the specified envelope. If the envelope contains
     * a CRS, then the bounding box will be projected to the {@linkplain DefaultGeographicCRS#WGS84
     * WGS 84} CRS. Otherwise, the envelope is assumed already in WGS 84 CRS.
     *
     * @since 2.2
     */
    public GeographicBoundingBoxImpl(Envelope envelope) throws TransformException {
        super(true);
        // TODO: use a more direct way if we add a 'getCRS()' method straight into Envelope.
        final CoordinateReferenceSystem crs = envelope.getLowerCorner().getCoordinateReferenceSystem();
        if (crs != null) {
            if (!CRSUtilities.equalsIgnoreMetadata(CRSUtilities.getSubCRS(crs,0,2), DefaultGeographicCRS.WGS84) &&
                !CRSUtilities.equalsIgnoreMetadata(CRSUtilities.getSubCRS(crs,0,3), DefaultGeographicCRS.WGS84_3D))
            {
                final CoordinateOperationFactory factory = FactoryFinder.getCoordinateOperationFactory(null);
                final CoordinateOperation operation;
                try {
                    operation = factory.createOperation(crs, DefaultGeographicCRS.WGS84);
                } catch (FactoryException exception) {
                    throw new TransformException(Resources.format(
                              ResourceKeys.ERROR_CANT_TRANSFORM_ENVELOPE, exception));
                }
                envelope = CRSUtilities.transform(operation.getMathTransform(), envelope);
            }
            setWestBoundLongitude(envelope.getMinimum(0));
            setEastBoundLongitude(envelope.getMaximum(0));
            setSouthBoundLatitude(envelope.getMinimum(1));
            setNorthBoundLatitude(envelope.getMaximum(1));
        }
    }

    /**
     * Constructs a geographic bounding box from the specified rectangle.
     * The rectangle is assumed in {@linkplain DefaultGeographicCRS#WGS84 WGS 84} CRS.
     */
    public GeographicBoundingBoxImpl(final Rectangle2D bounds) {
        this(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY());
    }

    /**
     * Creates a geographic bounding box initialized to the specified values.
     */
    public GeographicBoundingBoxImpl(final double westBoundLongitude,
                                     final double eastBoundLongitude,
                                     final double southBoundLatitude,
                                     final double northBoundLatitude)
    {
        super(true);
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
     * @return The western-most longitude between -180 and +180�.
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
     * @return The eastern-most longitude between -180 and +180�.
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
     * @return The southern-most latitude between -90 and +90�.
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
     * @return The northern-most latitude between -90 and +90�.
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
     * Declares this metadata and all its attributes as unmodifiable.
     */
    protected void freeze() {
        super.freeze();
    }

    /**
     * Compares this geographic bounding box with the specified object for equality.
     */
    public synchronized boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final GeographicBoundingBoxImpl that = (GeographicBoundingBoxImpl) object;
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
     * Returns a string representation of this extent using a default angle pattern.
     */
    public String toString() {
        return toString("DD�MM'SS\"");
    }

    /**
     * Returns a string representation of this extent using the specified angle pattern.
     * See {@link AngleFormat} for a description of angle patterns.
     */
    public String toString(final String pattern) {
        final StringBuffer buffer = new StringBuffer();
        final AngleFormat  format = new AngleFormat(pattern);
        buffer.append(format.format(new  Latitude(getNorthBoundLatitude())));
        buffer.append(", ");
        buffer.append(format.format(new Longitude(getSouthBoundLatitude())));
        buffer.append(" - ");
        buffer.append(format.format(new  Latitude(getWestBoundLongitude())));
        buffer.append(", ");
        buffer.append(format.format(new Longitude(getEastBoundLongitude())));
        return buffer.toString();
    }    
}
