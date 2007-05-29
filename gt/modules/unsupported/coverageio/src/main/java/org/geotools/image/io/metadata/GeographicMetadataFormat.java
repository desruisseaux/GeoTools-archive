/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
package org.geotools.image.io.metadata;

// J2SE dependencies
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;

// OpenGIS dependencies
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.operation.MathTransform;


/**
 * Describes the structure of {@linkplain GeographicMetadata geographic metadata}.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeographicMetadataFormat extends IIOMetadataFormatImpl {
    /**
     * The metadata format name.
     */
    public static final String FORMAT_NAME = "org.geotools";

    /**
     * The maximum number of dimension allowed for the image coordinate system. Images must be
     * at least two-dimensional. Some plugins consider the set of bands as the third dimension
     * (for example slices at different depths). An additional "1 pixel large" temporal dimension
     * is sometime used for storing the image timestamp.
     */
    private static final int MAXIMUM_DIMENSIONS = 4;

    /**
     * The maximum number of bands allowed. This is a somewhat arbitrary value since there is
     * no reason (except memory or disk space constraints) to restrict the number of bands in
     * the image stream. The number of bands actually read is usually much smaller.
     */
    private static final int MAXIMUM_BANDS = Short.MAX_VALUE;

    /**
     * Enumeration of valid coordinate reference system types.
     */
    private static final List/*<String>*/ CRS_TYPES = Arrays.asList(new String[] {
        "geographic", "projected"
    });

    /**
     * Enumeration of valid coordinate system types.
     */
    private static final List/*<String>*/ CS_TYPES = Arrays.asList(new String[] {
        "ellipsoidal", "cartesian"
    });

    /**
     * Enumeration of valid axis directions.
     */
    private static final List/*<String>*/ DIRECTIONS = Arrays.asList(new String[] {
        "north", "east", "south", "west", "up", "down"
    });

    /**
     * The default instance. Will be created only when first needed.
     *
     * @see #getInstance
     */
    private static GeographicMetadataFormat DEFAULT;

    /**
     * Creates a default metadata format.
     */
    private GeographicMetadataFormat() {
        this(FORMAT_NAME, MAXIMUM_DIMENSIONS, MAXIMUM_BANDS);
    }

    /**
     * Creates a metadata format of the given name. Subclasses should invoke the various
     * {@link #addElement(String,String,int) addElement} or {@link #addAttribute addAttribute}
     * methods for adding new elements compared to the {@linkplain #getInstance default instance}.
     *
     * @param rootName the name of the root element.
     * @param maximumDimensions The maximum number of dimensions allowed for coordinate systems.
     * @param maximumBands The maximum number of sample dimensions allowed for images.
     */
    protected GeographicMetadataFormat(final String rootName,
            final int maximumDimensions, final int maximumBands)
    {
        super(rootName, CHILD_POLICY_SOME);
        /*
         * root
         *   +-- CoordinateReferenceSystem (name, type)
         *   |     +-- Datum (name)
         *   |     +-- CoordinateSystem (name, type)
         *   |           +-- Axis[0] (name, direction, units)
         *   |           +-- Axis[1] (name, direction, units)
         *   |           +-- ...etc...
         *   +-- GridGeometry
         *         +-- Envelope
         *         |     +-- CoordinateValues[0]
         *         |     +-- CoordinateRange[0] (minCoordinate, maxCoordinate)
         *         |     +-- CoordinateValues[1]
         *         |     +-- ...etc...
         *         +-- AffineTransform (elements[6..?])
         */
        addElement  ("CoordinateReferenceSystem", rootName,    CHILD_POLICY_SOME);
        addAttribute("CoordinateReferenceSystem", "name",      DATATYPE_STRING);
        addAttribute("CoordinateReferenceSystem", "type",      DATATYPE_STRING, false, null, CRS_TYPES);
        addElement  ("Datum", "CoordinateReferenceSystem",     CHILD_POLICY_EMPTY);
        addAttribute("Datum",             "name",              DATATYPE_STRING, true, null);
        addElement  ("CoordinateSystem",  "CoordinateReferenceSystem", 2, maximumDimensions);
        addAttribute("CoordinateSystem",  "name",              DATATYPE_STRING);
        addAttribute("CoordinateSystem",  "type",              DATATYPE_STRING, false, null, CS_TYPES);
        addElement  ("Axis",              "CoordinateSystem",  CHILD_POLICY_EMPTY);
        addAttribute("Axis",              "name",              DATATYPE_STRING);
        addAttribute("Axis",              "direction",         DATATYPE_STRING, false, null, DIRECTIONS);
        addAttribute("Axis",              "units",             DATATYPE_STRING);
        addElement  ("GridGeometry",       rootName,           CHILD_POLICY_CHOICE);
        addElement  ("Envelope",          "GridGeometry",      CHILD_POLICY_SEQUENCE);
        addElement  ("CoordinateValues",  "Envelope",          CHILD_POLICY_EMPTY);
        addElement  ("CoordinateRange",   "Envelope",          CHILD_POLICY_EMPTY);
        addAttribute("CoordinateRange",   "minimum",           DATATYPE_DOUBLE, true, null);
        addAttribute("CoordinateRange",   "maximum",           DATATYPE_DOUBLE, true, null);
        addElement  ("AffineTransform",   "GridGeometry",      CHILD_POLICY_EMPTY);
        addAttribute("AffineTransform",   "elements",          DATATYPE_DOUBLE, true,
                6, maximumDimensions * (maximumDimensions - 1));
        /*
         * root
         *   +-- SampleDimensions
         *         +-- SampleDimension[0] (scale, offset, minValue, maxValue, fillValue)
         *         +-- SampleDimension[1] (scale, offset, minValue, maxValue, fillValue)
         *         +-- ...etc...
         */
        addElement  ("SampleDimensions", rootName,          1, maximumBands);
        addElement  ("SampleDimension", "SampleDimensions", CHILD_POLICY_SOME);
        addAttribute("SampleDimension", "scale",            DATATYPE_DOUBLE);
        addAttribute("SampleDimension", "offset",           DATATYPE_DOUBLE);
        addAttribute("SampleDimension", "minValue",         DATATYPE_DOUBLE);
        addAttribute("SampleDimension", "maxValue",         DATATYPE_DOUBLE);
        addAttribute("SampleDimension", "fillValue",        DATATYPE_DOUBLE);
        /*
         * Allow users to specify fully-constructed GeoAPI objects.
         */
        addObjectValue("CoordinateReferenceSystem", CoordinateReferenceSystem.class);
        addObjectValue("Datum",                     Datum.class);
        addObjectValue("CoordinateSystem",          CoordinateSystem.class);
        addObjectValue("Axis",                      CoordinateSystemAxis.class);
        addObjectValue("GridGeometry",              GridGeometry.class);
        addObjectValue("Envelope",                  Envelope.class);
        addObjectValue("AffineTransform",           MathTransform.class);
        addObjectValue("SampleDimension",           SampleDimension.class);
    }

    /**
     * Adds an optional attribute of the specified data type.
     */
    private void addAttribute(final String elementName, final String attrName, final int dataType) {
        addAttribute(elementName, attrName, dataType, false, null);
    }

    /**
     * Adds an optional object value of the specified class.
     */
    private void addObjectValue(final String elementName, final Class/*<*>*/ classType) {
        addObjectValue(elementName, classType, false, null);
    }

    /**
     * Returns {@code true} if the element (and the subtree below it) is allowed to appear
     * in a metadata document for an image of the given type. The default implementation
     * always returns {@code true}.
     */
    public boolean canNodeAppear(final String elementName, final ImageTypeSpecifier imageType) {
        return true;
    }

    /**
     * Returns the default geographic metadata format instance.
     */
    public static synchronized GeographicMetadataFormat getInstance() {
        if (DEFAULT == null) {
            DEFAULT = new GeographicMetadataFormat();
        }
        return DEFAULT;
    }
}
