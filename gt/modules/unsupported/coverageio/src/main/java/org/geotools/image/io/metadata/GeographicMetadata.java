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
     * Creates a default metadata instance. This constructor defines no standard or native format.
     * The only format defined is the {@linkplain GeographicMetadataFormat geographic} one.
     */
    public GeographicMetadata() {
        super(false, // Can not return or accept a DOM tree using the standard metadata format.
              null,  // There is no native metadata format.
              null,  // There is no native metadata format.
              new String[] {
                  GeographicMetadataFormat.FORMAT_NAME
              },
              new String[] {
                  "org.geotools.image.io.metadata.GeographicMetadataFormat"
              });
        reset();
    }

    /**
     * Returns {@code false} since this node support some write operations.
     */
    public boolean isReadOnly() {
        return false;
    }

    /**
     * Set the attribute to the specified value, or remove the attribute if the value is null.
     */
    private static void setAttribute(final Element node, final String name, String value, boolean isCodeList) {
        if (value == null) {
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
        setAttribute(crs, "name", name, false);
        setAttribute(crs, "type", type, false);
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
        setAttribute(datum, "name", name, false);
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
        setAttribute(cs, "name", name, false);
        setAttribute(cs, "type", type, false);
    }

    /**
     * Add an axis to the the coordinate system.
     *
     * @param name The axis name, or {@code null} if unknown.
     * @param direction The axis direction (usually {@code "east"}, {@code "weast"},
     *        {@code "north"}, {@code "south"}, {@code "up"} or {@code "down"}),
     *        or {@code null} if unknown.
     * @param units The axis units symbol, or {@code null} if unknown.
     *
     * @see org.opengis.referencing.cs.AxisDirection
     */
    public void addAxis(final String name, final String direction, final String units) {
        if (cs == null) {
            setCoordinateSystem(null, null);
        }
        final IIOMetadataNode axis = new IIOMetadataNode("Axis");
        setAttribute(axis, "name",      name,      false);
        setAttribute(axis, "direction", direction, true);
        setAttribute(axis, "units",     units,     false);
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
        setAttribute(gridGeometry, "pixelOrientation", pixelOrientation, true);
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
     * @param indexMin The minimal index value, inclusive. This is usually 0.
     * @param indexMax The maximal index value, <strong>inclusive</strong>.
     */
    private void addGridRange(final int indexMin, final int indexMax) {
        if (gridRange == null) {
            setGridRange();
        }
        final IIOMetadataNode range = new IIOMetadataNode("IndexRange");
        setAttribute(range, "minimum", Integer.toString(indexMin), false);
        setAttribute(range, "maximum", Integer.toString(indexMax), false);
        gridRange.appendChild(range);
    }

    /**
     * Adds the range of values for an envelope along a dimension. The ranges
     * should be added in the same order than {@linkplain #addAxis axis}.
     *
     * @param indexMin The minimal index value, inclusive. This is usually 0.
     * @param indexMax The maximal index value, <strong>inclusive</strong>.
     * @param valueMin The minimal coordinate value, inclusive.
     * @param valueMax The maximal coordinate value, <strong>inclusive</strong>.
     */
    public void addCoordinateRange(final int    indexMin, final int    indexMax,
                                   final double valueMin, final double valueMax)
    {
        addGridRange(indexMin, indexMax);
        if (envelope == null) {
            setEnvelope();
        }
        final IIOMetadataNode range = new IIOMetadataNode("CoordinateRange");
        setAttribute(range, "minimum", Double.toString(valueMin), false);
        setAttribute(range, "maximum", Double.toString(valueMax), false);
        envelope.appendChild(range);
    }

    /**
     * Adds coordinate values for an envelope along a dimension. This method may be invoked
     * in replacement of {@link #addCoordinateRange} when every cell coordinates need to be
     * specified explicitly.
     *
     * @param indexMin The minimal index value, inclusive. This is usually 0.
     * @param values The coordinate values.
     */
    public void addCoordinateValues(final int indexMin, final double[] values) {
        addGridRange(indexMin, indexMin + values.length);
        if (envelope == null) {
            setEnvelope();
        }
        final IIOMetadataNode cv = new IIOMetadataNode("CoordinateValues");
        cv.setUserObject(values);
        envelope.appendChild(cv);
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
