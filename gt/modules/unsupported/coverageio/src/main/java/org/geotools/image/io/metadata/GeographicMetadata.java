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
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.metadata.IIOInvalidTreeException;
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
    private Node root;

    /**
     * The coordinate reference system node.
     * Will be created only when first needed.
     */
    private ImageReferencing referencing;

    /**
     * The grid geometry node.
     * Will be created only when first needed.
     */
    private ImageGeometry geometry;

    /**
     * The list of {@linkplain Band bands}.
     * Will be created only when first needed.
     */
    private ChildList/*<Bands>*/ bands;

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
    }

    /**
     * Wraps the specified metadata. This constructor defines no standard or native format.
     * The only format defined is the {@linkplain GeographicMetadataFormat geographic} one.
     */
    public GeographicMetadata(final IIOMetadata metadata) {
        this();
        root = metadata.getAsTree(GeographicMetadataFormat.FORMAT_NAME);
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
    public GeographicMetadata(final boolean  standardMetadataFormatSupported,
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
    }

    /**
     * Returns {@code false} since this node support some write operations.
     */
    public boolean isReadOnly() {
        return false;
    }

    /**
     * Returns the root of a tree of metadata contained within this object
     * according to the conventions defined by a given metadata format.
     */
    private Node getRoot() {
        if (root == null) {
            root = new IIOMetadataNode(GeographicMetadataFormat.FORMAT_NAME);
        }
        return root;
    }

    /**
     * Returns the grid geometry.
     */
    public ImageReferencing getReferencing() {
        if (referencing == null) {
            referencing = new ImageReferencing(getRoot());
        }
        return referencing;
    }

    /**
     * Returns the grid geometry.
     */
    public ImageGeometry getGeometry() {
        if (geometry == null) {
            geometry = new ImageGeometry(getRoot());
        }
        return geometry;
    }

    /**
     * Returns the list of all {@linkplain Band bands}.
     */
    final ChildList/*<Bands>*/ getBands() {
        if (bands == null) {
            bands = new ChildList.Bands(getRoot());
        }
        return bands;
    }

    /**
     * Returns the sample type (typically {@value GeographicMetadataFormat#GEOPHYSICS} or
     * {@value GeographicMetadataFormat#PACKED}), or {@code null} if none. This type applies
     * to all {@linkplain Band bands}.
     */
    public String getSampleType() {
        return getBands().getString("type");
    }

    /**
     * Set the sample type for all {@linkplain Band bands}. Valid types include
     * {@value GeographicMetadataFormat#GEOPHYSICS} and {@value GeographicMetadataFormat#PACKED}.
     *
     * @param type The sample type, or {@code null} if none.
     */
    public void setSampleType(final String type) {
        getBands().setEnum("type", type);
    }

    /**
     * Returns the number of {@linkplain Band bands} in the coverage.
     */
    public int getNumBands() {
        return getBands().childCount();
    }

    /**
     * Returns the band at the specified index.
     *
     * @param  bandIndex the band index, ranging from 0 inclusive to {@link #getNumBands} exclusive.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public Band getBand(final int bandIndex) throws IndexOutOfBoundsException {
        return (Band) getBands().getChild(bandIndex);
    }

    /**
     * Creates a new band and returns it.
     *
     * @param name The name for the new band.
     */
    public Band addBand(final String name) {
        final Band band = (Band) getBands().addChild();
        band.setName(name);
        return band;
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
        return getRoot();
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
        root        = null;
        referencing = null;
        geometry    = null;
    }

    /**
     * Returns a string representation of this metadata, mostly for debugging purpose.
     */
    public String toString() {
        return OptionalDependencies.toString(
                OptionalDependencies.xmlToSwing(getAsTree(GeographicMetadataFormat.FORMAT_NAME)));
    }
}
