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
    private static void setAttribute(final Element node, final String name, final String value) {
        if (value == null) {
            if (node.hasAttribute(name)) {
                node.removeAttribute(name);
            }
        } else {
            node.setAttribute(name, value);
        }
    }

    /**
     * Set the coordinate reference system to the specified value.
     *
     * @param name The coordinate reference system name, or {@code null} if unknown.
     * @param type The coordinate reference system type (usually {@code "geographic"}
     *             or {@code "projected"}), or {@code null} if unknown.
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
     * @param type The coordinate system type (usually {@code "ellipsoidal"}
     *             or {@code "cartesian"}), or {@code null} if unknown.
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
     * Add an axis to the the coordinate system.
     *
     * @param name The axis name, or {@code null} if unknown.
     * @param direction The axis direction (usually {@code "east"}, {@code "weast"},
     *             {@code "north"}, {@code "south"}, {@code "up"} or {@code "down"}),
     *             or {@code null} if unknown.
     * @param units The axis units symbol, or {@code null} if unknown.
     */
    public void addAxis(final String name, final String direction, final String units) {
        if (cs == null) {
            setCoordinateSystem(null, null);
        }
        final IIOMetadataNode axis = new IIOMetadataNode("Axis");
        setAttribute(axis, "name",      name);
        setAttribute(axis, "direction", direction);
        setAttribute(axis, "units",     units);
        cs.appendChild(axis);
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
        root  = new IIOMetadataNode(GeographicMetadataFormat.FORMAT_NAME);
        crs   = null;
        cs    = null;
        datum = null;
    }

    /**
     * Returns a string representation of this metadata, mostly for debugging purpose.
     */
    public String toString() {
        return OptionalDependencies.toString(
                OptionalDependencies.xmlToSwing(getAsTree(GeographicMetadataFormat.FORMAT_NAME)));
    }
}
