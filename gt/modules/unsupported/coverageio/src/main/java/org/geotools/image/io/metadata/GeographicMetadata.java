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
import java.lang.reflect.Array;
import java.text.Format;
import java.util.Date;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.OptionalDependencies;


/**
 * Geographic informations encoded in image as metadata.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GeographicMetadata extends IIOMetadata {
    /**
     * The root node to be returned by {@link #getAsTree}.
     */
    private IIOMetadataNode root;

    /**
     * The coordinate reference system node.
     * Will be created only when first needed.
     */
    private IIOMetadataNode crs;

    /**
     * The coordinate system node.
     * Will be created only when first needed.
     */
    private IIOMetadataNode cs;

    /**
     * The datum node.
     * Will be created only when first needed.
     */
    private IIOMetadataNode datum;

    /**
     * The grid geometry node.
     * Will be created only when first needed.
     */
    private IIOMetadataNode gridGeometry;

    /**
     * The grid geometry node.
     * Will be created only when first needed.
     */
    private IIOMetadataNode gridRange;

    /**
     * The envelope node.
     * Will be created only when first needed.
     */
    private IIOMetadataNode envelope;

    /**
     * The sample dimension node.
     * Will be created only when first needed.
     */
    private IIOMetadataNode sampleDimensions;

    /**
     * Creates a default metadata instance. This constructor defines no standard or native format.
     * The only format defined is the {@linkplain GeographicMetadataFormat geographic} one.
     */
    protected GeographicMetadata() {
        this(false, // Can not return or accept a DOM tree using the standard metadata format.
              null,  // There is no native metadata format.
              null,  // There is no native metadata format.
              new String[] {
                  GeographicMetadataFormat.FORMAT_NAME
              },
              new String[] {
                  "org.geotools.image.io.metadata.GeographicMetadataFormat"
              });
    }

    /**
     * Constructs a geographic metadata instance with the given format names and format class names.
     * This constructor passes the arguments to the {@linkplain IIOMetadata#IIOMetadata(boolean,
     * String, String, String[], String[]) super-class constructor} unchanged.
     *
     * @param standardMetadataFormatSupported {@code true} if this object can return or accept
     *        a DOM tree using the standard metadata format.
     * @param nativeMetadataFormatName The name of the native metadata, or {@code null} if none.
     * @param nativeMetadataFormatClassName The name of the class of the native metadata format,
     *        or {@code null} if none.
     * @param extraMetadataFormatNames Additional formats supported by this object,
     *        or {@code null} if none.
     * @param extraMetadataFormatClassNames The class names of any additional formats
     *        supported by this object, or {@code null} if none.
     */
    protected GeographicMetadata(final boolean  standardMetadataFormatSupported,
                                 final String   nativeMetadataFormatName,
                                 final String   nativeMetadataFormatClassName,
                                 final String[] extraMetadataFormatNames,
                                 final String[] extraMetadataFormatClassNames)
    {
        super(standardMetadataFormatSupported,
              nativeMetadataFormatName,
              nativeMetadataFormatClassName,
              extraMetadataFormatNames,
              extraMetadataFormatClassNames);
        reset();
    }

    /**
     * Returns {@code false} since this node support some write operations.
     */
    public boolean isReadOnly() {
        return false;
    }

    /**
     * Set the coordinate reference system to the specified value.
     *
     * @param name The coordinate reference system name, or {@code null} if unknown.
     * @param type The coordinate reference system type (usually
     *             {@value GeographicMetadataFormat#GEOGRAPHIC} or
     *             {@value GeographicMetadataFormat#PROJECTED}), or {@code null} if unknown.
     */
    public void setCoordinateReferenceSystem(final String name, final String type) {
        if (crs == null) {
            crs = new IIOMetadataNode("CoordinateReferenceSystem");
            root.appendChild(crs);
        }
        setAttribute(crs, "name", name);
        setAttribute(crs, "type", type);
    }

    /**
     * Set the datum to the specified value.
     *
     * @param name The datum name, or {@code null} if unknown.
     */
    public void setDatum(final String name) {
        if (crs == null) {
            setCoordinateReferenceSystem(null, null);
        }
        if (datum == null) {
            datum = new IIOMetadataNode("Datum");
            crs.appendChild(datum);
        }
        setAttribute(datum, "name", name);
    }

    /**
     * Set the coordinate system to the specified value.
     *
     * @param name The coordinate system name, or {@code null} if unknown.
     * @param type The coordinate system type (usually
     *             {@value GeographicMetadataFormat#ELLIPSOIDAL} or
     *             {@value GeographicMetadataFormat#CARTESIAN}), or {@code null} if unknown.
     */
    public void setCoordinateSystem(final String name, final String type) {
        if (crs == null) {
            setCoordinateReferenceSystem(null, null);
        }
        if (cs == null) {
            cs = new IIOMetadataNode("CoordinateSystem");
            crs.appendChild(cs);
        }
        setAttribute(cs, "name", name);
        setAttribute(cs, "type", type);
    }

    /**
     * Adds an axis to the the coordinate system.
     *
     * @param name The axis name, or {@code null} if unknown.
     * @param direction The axis direction (usually {@code "east"}, {@code "weast"},
     *        {@code "north"}, {@code "south"}, {@code "up"} or {@code "down"}),
     *        or {@code null} if unknown.
     * @param units The axis units symbol, or {@code null} if unknown.
     *
     * @see org.opengis.referencing.cs.AxisDirection
     */
    public void addAxis(final String name,  final String direction, final String units) {
        addAxis(name, direction, units, null);        
    }

    /**
     * Adds a time axis to the the coordinate system.
     *
     * @param name The axis name, or {@code null} if unknown.
     * @param direction The axis direction, or {@code null} if unknown.
     * @param units The axis units symbol, or {@code null} if unknown.
     * @param origin The {@linkplain org.opengis.referencing.datum.TemporalDatum#getOrigin epoch},
     *        or {@code null} if unknown.
     *
     * @see org.opengis.referencing.cs.AxisDirection
     */
    public void addTimeAxis(final String name,  final String direction,
                            final String units, final Date   origin)
    {
        addAxis(name, direction, units, origin);
    }

    /**
     * Implementation of {@link #addAxis} and {@link #addTimeAxis}. Provided as a separated private
     * method in order to not have the {@link #addAxis} method invoking {@link #addTimeAxis}, which
     * would be unexpected.
     */
    private void addAxis(final String name,  final String direction,
                         final String units, final Date   origin)
    {
        if (cs == null) {
            setCoordinateSystem(null, null);
        }
        final IIOMetadataNode axis = new IIOMetadataNode("Axis");
        setAttribute(axis, "name",      name);
        setEnum     (axis, "direction", direction);
        setAttribute(axis, "units",     units);
        setAttribute(axis, "origin",    origin);
        cs.appendChild(axis);
    }

    /**
     * Set the grid geometry to the specified value. The pixel orientation gives the point in
     * a pixel corresponding to the Earth location of the pixel. In the JAI framework, this is
     * typically the {@linkplain org.opengis.metadata.spatial.PixelOrientation#UPPER_LEFT
     * upper left} corner. In some OGC specifications, this is often the pixel
     * {@linkplain org.opengis.metadata.spatial.PixelOrientation#CENTER center}.
     *
     * @param pixelOrientation The pixel orientation (usually {@code "center"},
     *        {@code "lower left"}, {@code "lower right"}, {@code "upper right"}
     *        or {@code "upper left"}), or {@code null} if unknown.
     *
     * @see org.opengis.metadata.spatial.PixelOrientation
     */
    public void setGridGeometry(final String pixelOrientation) {
        if (gridGeometry == null) {
            gridGeometry = new IIOMetadataNode("GridGeometry");
            root.appendChild(gridGeometry);
        }
        setEnum(gridGeometry, "pixelOrientation", pixelOrientation);
    }

    /**
     * Set the grid range to the specified value.
     *
     * This method is not yet public because there is no parameter yet.
     */
    private void setGridRange() {
        if (gridGeometry == null) {
            setGridGeometry(null);
        }
        if (gridRange == null) {
            gridRange = new IIOMetadataNode("GridRange");
            gridGeometry.appendChild(gridRange);
        }
    }

    /**
     * Set the envelope to the specified value.
     *
     * This method is not yet public because there is no parameter yet.
     */
    private void setEnvelope() {
        if (gridGeometry == null) {
            setGridGeometry(null);
        }
        if (envelope == null) {
            envelope = new IIOMetadataNode("Envelope");
            gridGeometry.appendChild(envelope);
        }
    }

    /**
     * Adds the range of index along a dimension. The ranges
     * should be added in the same order than {@linkplain #addAxis axis}.
     *
     * @param minIndex The minimal index value, inclusive. This is usually 0.
     * @param maxIndex The maximal index value, <strong>inclusive</strong>.
     */
    private void addGridRange(final int minIndex, final int maxIndex) {
        if (gridRange == null) {
            setGridRange();
        }
        final IIOMetadataNode range = new IIOMetadataNode("IndexRange");
        setAttribute(range, "minimum", minIndex);
        setAttribute(range, "maximum", maxIndex);
        gridRange.appendChild(range);
    }

    /**
     * Adds the range of values for an envelope along a dimension. The ranges
     * should be added in the same order than {@linkplain #addAxis axis}.
     *
     * @param minIndex The minimal index value, inclusive. This is usually 0.
     * @param maxIndex The maximal index value, <strong>inclusive</strong>.
     * @param minValue The minimal coordinate value, inclusive.
     * @param maxValue The maximal coordinate value, <strong>inclusive</strong>.
     */
    public void addCoordinateRange(final int    minIndex, final int    maxIndex,
                                   final double minValue, final double maxValue)
    {
        addGridRange(minIndex, maxIndex);
        if (envelope == null) {
            setEnvelope();
        }
        final IIOMetadataNode range = new IIOMetadataNode("CoordinateRange");
        setAttribute(range, "minimum", minValue);
        setAttribute(range, "maximum", maxValue);
        envelope.appendChild(range);
    }

    /**
     * Adds coordinate values for an envelope along a dimension. This method may be invoked
     * in replacement of {@link #addCoordinateRange} when every cell coordinates need to be
     * specified explicitly.
     *
     * @param minIndex The minimal index value, inclusive. This is usually 0.
     * @param values The coordinate values.
     */
    public void addCoordinateValues(final int minIndex, final double[] values) {
        addGridRange(minIndex, minIndex + values.length);
        if (envelope == null) {
            setEnvelope();
        }
        final IIOMetadataNode cv = new IIOMetadataNode("CoordinateValues");
        cv.setUserObject(values);
        envelope.appendChild(cv);
    }

    /**
     * Set the sample dimensions to the specified value.
     *
     * @param type The type for all sample dimensions (usually
     *             {@value GeographicMetadataFormat#GEOPHYSICS} or
     *             {@value GeographicMetadataFormat#PACKED}), or {@code null} if unknown.
     */
    public void setSampleDimensions(final String type) {
        if (sampleDimensions == null) {
            sampleDimensions = new IIOMetadataNode("SampleDimensions");
            root.appendChild(sampleDimensions);
        }
        setEnum(sampleDimensions, "type", type);
    }

    /**
     * Adds a sample dimension.
     *
     * @param name       The sample dimension name, or {@code null} if none.
     * @param scale      The scale from packed to geophysics values, or {@code 1} if none.
     * @param offset     The offset from packed to geophysics values, or {@code 0} if none.
     * @param minValue   The minimal valid <em>packed</em> value,
     *                   or {@link Double#NEGATIVE_INFINITY} if none.
     * @param maxValue   The maximal valid <em>packed</em> value,
     *                   or {@link Double#POSITIVE_INFINITY} if none.
     * @param fillValues The packed values used for missing data, or {@code null} if none.
     */
    public void addSampleDimension(final String name,
                                   final double scale,    final double offset,
                                   final double minValue, final double maxValue,
                                   final double[] fillValues)
    {
        if (sampleDimensions == null) {
            setSampleDimensions(null);
        }
        final IIOMetadataNode band = new IIOMetadataNode("SampleDimension");
        setAttribute(band, "name",     name  );
        setAttribute(band, "scale",    scale );
        setAttribute(band, "offset",   offset);
        final int minIndex = (int) minValue;
        final int maxIndex = (int) maxValue;
        if (minIndex == minValue && maxIndex == maxValue) {
            /*
             * Values should be integers most of the time since they are packed values
             * (often index in a color palette). But we will allow floating point values
             * in the 'else' section if they are not.
             */
            setAttribute(band, "minValue", minIndex);
            setAttribute(band, "maxValue", maxIndex);
        } else {
            setAttribute(band, "minValue", minValue);
            setAttribute(band, "maxValue", maxValue);
        }
        /*
         * Formats all fill values as integers, or all as floating points. We expect integer
         * values for the same reason than "minValue" and "maxValue" above, but are tolerant
         * to floating point values. We apply a "all or nothing" rule for consistency.
         */
        if (fillValues != null) {
            int[] asIntegers = new int[fillValues.length];
            for (int i=0; i<fillValues.length; i++) {
                final double value = fillValues[i];
                if ((asIntegers[i] = (int) value) != value) {
                    asIntegers = null; // Not integers; stop the check.
                    break;
                }
            }
            if (asIntegers != null) {
                setAttribute(band, "fillValues", asIntegers);
            } else {
                setAttribute(band, "fillValues", fillValues);
            }
        }
        sampleDimensions.appendChild(band);
    }

    /**
     * Set the attribute to the specified enumeration value,
     * or remove the attribute if the value is null.
     *
     * @param node  The node on which to set the attribute.
     * @param name  The attribute name.
     * @param value The attribute value.
     */
    protected static void setEnum(final Element node, final String name, final String value) {
        setAttribute(node, name, value, true);
    }

    /**
     * Set the attribute to the specified value,
     * or remove the attribute if the value is null.
     *
     * @param node  The node on which to set the attribute.
     * @param name  The attribute name.
     * @param value The attribute value.
     */
    protected static void setAttribute(final Element node, final String name, final String value) {
        setAttribute(node, name, value, false);
    }

    /**
     * Set the attribute to the specified value,
     * or remove the attribute if the value is null.
     *
     * @param node       The node on which to set the attribute.
     * @param name       The attribute name.
     * @param value      The attribute value.
     * @param isCodeList Reformat the value if it is a code list.
     */
    private static void setAttribute(final Element node, final String name, String value, boolean isCodeList) {
        if (value == null || (value=value.trim()).length() == 0) {
            if (node.hasAttribute(name)) {
                node.removeAttribute(name);
            }
        } else {
            if (isCodeList) {
                value = value.replace('_', ' ').trim().toLowerCase();
            }
            node.setAttribute(name, value);
        }
    }

    /**
     * Set the attribute to the specified integer value.
     *
     * @param node  The node on which to set the attribute.
     * @param name  The attribute name.
     * @param value The attribute value.
     */
    protected static void setAttribute(final Element node, final String name, final int value) {
        node.setAttribute(name, Integer.toString(value));
    }

    /**
     * Set the attribute to the specified array of values,
     * or remove the attribute if the array is {@code null}.
     *
     * @param node  The node on which to set the attributes.
     * @param name  The attribute name.
     * @param value The attribute values.
     */
    protected static void setAttribute(final Element node, final String name, final int[] values) {
        setAttribute(node, name, toSequence(values));
    }

    /**
     * Set the attribute to the specified floating point value,
     * or remove the attribute if the value is NaN.
     *
     * @param node  The node on which to set the attribute.
     * @param name  The attribute name.
     * @param value The attribute value.
     */
    protected static void setAttribute(final Element node, final String name, final double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            if (node.hasAttribute(name)) {
                node.removeAttribute(name);
            }
        } else {
            node.setAttribute(name, Double.toString(value));
        }
    }

    /**
     * Set the attribute to the specified array of values,
     * or remove the attribute if the array is {@code null}.
     *
     * @param node  The node on which to set the attributes.
     * @param name  The attribute name.
     * @param value The attribute values.
     */
    protected static void setAttribute(final Element node, final String name, final double[] values) {
        setAttribute(node, name, toSequence(values));
    }

    /**
     * Set the attribute to the specified value, or remove the attribute if the value is NaN.
     *
     * @param node  The node on which to set the attribute.
     * @param name  The attribute name.
     * @param value The attribute value.
     */
    protected static void setAttribute(final Element node, final String name, final Date value) {
        String asText = null;
        if (value != null) {
            final Format format = (Format) GeographicMetadataParser.dateFormat.get();
            asText = format.format(value);
        }
        setAttribute(node, name, asText);
    }

    /**
     * Returns the specified array as a sequence.
     */
    private static String toSequence(final Object array) {
        if (array == null) {
            return null;
        }
        final StringBuffer buffer = new StringBuffer();
        final int length = Array.getLength(array);
        for (int i=0; i<length; i++) {
            if (i != 0) {
                buffer.append(' ');
            }
            buffer.append(Array.get(array, i));
        }
        return buffer.toString();
    }

    /**
     * Checks the format name.
     */
    private void checkFormatName(final String formatName) throws IllegalArgumentException {
        if (!GeographicMetadataFormat.FORMAT_NAME.equals(formatName)) {
            throw new IllegalArgumentException(Errors.format(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "formatName", formatName));
        }
    }

    /**
     * Returns the root of a tree of metadata contained within this object
     * according to the conventions defined by a given metadata format.
     *
     * @param formatName the desired metadata format.
     * @return The node forming the root of metadata tree.
     * @throws IllegalArgumentException if the format name is {@code null} or is not
     *         one of the names returned by {@link #getMetadataFormatNames()
     *         getMetadataFormatNames()}.
     */
    public Node getAsTree(final String formatName) throws IllegalArgumentException {
        checkFormatName(formatName);
        return root;
    }

    /**
     * Alters the internal state of this metadata from a tree whose syntax is defined by
     * the given metadata format.
     *
     * @todo This method is not yet implemented.
     */
    public void mergeTree(final String formatName, final Node root) throws IIOInvalidTreeException {
        checkFormatName(formatName);
        throw new IllegalStateException();
    }

    /**
     * Resets all the data stored in this object to default values.
     */
    public void reset() {
        root         = new IIOMetadataNode(GeographicMetadataFormat.FORMAT_NAME);
        crs          = null;
        cs           = null;
        datum        = null;
        gridGeometry = null;
        envelope     = null;
    }

    /**
     * Returns a string representation of this metadata, mostly for debugging purpose.
     */
    public String toString() {
        return OptionalDependencies.toString(
                OptionalDependencies.xmlToSwing(getAsTree(GeographicMetadataFormat.FORMAT_NAME)));
    }
}
