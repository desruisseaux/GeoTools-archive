/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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
package org.geotools.resources;

// J2SE dependencies
import java.util.Locale;

// OpenGIS dependencies
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.CoordinateOperation;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.Envelope;

// Geotools dependencies
import org.geotools.factory.Hints;
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.measure.AngleFormat;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.TransformPathNotFoundException;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Provides convenience methods for {@linkplain GeographicBoundingBox geographic bounding boxes}.
 * This is mostly a helper class for {@link GeographicBoundingBoxImpl}; users should not use this
 * class directly.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 */
public final class BoundingBoxes {
    /**
     * A set of hints used in order to fetch lenient coordinate operation factory. We accept
     * lenient transforms because {@link GeographicBoundingBox} are usually for approximative
     * bounds (e.g. the area of validity of some CRS). If a user wants accurate bounds, he
     * should probably use an {@link Envelope} with the appropriate CRS.
     */
    private static final Hints LENIENT = new Hints(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);

    /**
     * Prevents the creation of instances of this class.
     */
    private BoundingBoxes() {
    }

    /**
     * Initialize a geographic bounding box from the specified envelope. If the envelope contains
     * a CRS, then the bounding box will be projected to the {@linkplain DefaultGeographicCRS#WGS84
     * WGS 84} CRS. Otherwise, the envelope is assumed already in WGS 84 CRS.
     */
    public static void copy(Envelope envelope, final GeographicBoundingBoxImpl box)
            throws TransformException
    {
        // TODO: use a more direct way if we add a 'getCRS()' method straight into Envelope.
        final CoordinateReferenceSystem crs = envelope.getLowerCorner().getCoordinateReferenceSystem();
        if (crs != null) {
            if (!startsWith(crs, DefaultGeographicCRS.WGS84) &&
                !startsWith(crs, DefaultGeographicCRS.WGS84_3D))
            {
                final CoordinateOperation operation;
                final CoordinateOperationFactory factory;
                factory = FactoryFinder.getCoordinateOperationFactory(LENIENT);
                try {
                    operation = factory.createOperation(crs, DefaultGeographicCRS.WGS84);
                } catch (FactoryException exception) {
                    throw new TransformPathNotFoundException(Errors.format(
                              ErrorKeys.CANT_TRANSFORM_ENVELOPE, exception));
                }
                envelope = CRSUtilities.transform(operation.getMathTransform(), envelope);
            }
            box.setWestBoundLongitude(envelope.getMinimum(0));
            box.setEastBoundLongitude(envelope.getMaximum(0));
            box.setSouthBoundLatitude(envelope.getMinimum(1));
            box.setNorthBoundLatitude(envelope.getMaximum(1));
        }
    }

    /**
     * Returns {@code true} if the specified {@code crs} starts with the specified {@code head}.
     */
    private static final boolean startsWith(final CoordinateReferenceSystem crs,
                                            final CoordinateReferenceSystem head)
    {
        final int dimension = head.getCoordinateSystem().getDimension();
        return crs.getCoordinateSystem().getDimension() >= dimension &&
               CRS.equalsIgnoreMetadata(CRSUtilities.getSubCRS(crs,0,dimension), head);
    }

    /**
     * Returns a string representation of the specified extent using the specified angle
     * pattern and locale. See {@link AngleFormat} for a description of angle patterns.
     *
     * @param box     The bounding box to format.
     * @param pattern The angle pattern (e.g. {@code DD°MM'SS.s"}.
     * @param locale  The locale, or {@code null} for the default one.
     */
    public static String toString(final GeographicBoundingBox box,
                                  final String pattern, final Locale locale)
    {
        final StringBuffer buffer = new StringBuffer();
        final AngleFormat  format;
        format = (locale!=null) ? new AngleFormat(pattern, locale) : new AngleFormat(pattern);
        buffer.append(format.format(new  Latitude(box.getNorthBoundLatitude())));
        buffer.append(", ");
        buffer.append(format.format(new Longitude(box.getWestBoundLongitude())));
        buffer.append(" - ");
        buffer.append(format.format(new  Latitude(box.getSouthBoundLatitude())));
        buffer.append(", ");
        buffer.append(format.format(new Longitude(box.getEastBoundLongitude())));
        return buffer.toString();
    }    
}
