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
import java.text.Format;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Iterator;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.event.IIOReadWarningListener;
import org.w3c.dom.Node;

// Geotools dependencies
import org.geotools.util.LoggedFormat;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.OptionalDependencies;


/**
 * Geographic informations encoded in image as metadata. This class provides various methods for
 * reading and writting attribute values in {@link IIOMetadataNode} according the {@linkplain
 * GeographicMetadataFormat geographic metadata format}. If some inconsistency are found while
 * reading (for example if the coordinate system dimension doesn't match the envelope dimension),
 * then the default implementation {@linkplain #warningOccurred logs a warning}. We do not throw
 * an exception because minor errors are not uncommon in geographic data, and we want to process
 * the data on a "<cite>best effort</cite>" basis. However because every warnings are logged
 * through the {@link #warningOccurred} method, subclasses can override this method if they want
 * treat some warnings as fatal errors.
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
     * The locale for error messages.
     */
    private Locale locale = Locale.getDefault();

    /**
     * The standard date format. Will be created only when first needed.
     */
    private transient LoggedFormat/*<Date>*/ dateFormat;

    /**
     * Registered warning listeners, or {@code null} if none.
     */
    private transient Collection/*<IIOReadWarningListener>*/ warningListeners;

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
    final Node getRootNode() {
        if (root == null) {
            root = new IIOMetadataNode(GeographicMetadataFormat.FORMAT_NAME);
        }
        return root;
    }

    /**
     * Returns the grid referencing.
     */
    public ImageReferencing getReferencing() {
        if (referencing == null) {
            referencing = new ImageReferencing(this);
        }
        return referencing;
    }

    /**
     * Returns the grid geometry.
     */
    public ImageGeometry getGeometry() {
        if (geometry == null) {
            geometry = new ImageGeometry(this);
        }
        return geometry;
    }

    /**
     * Returns the list of all {@linkplain Band bands}.
     */
    final ChildList/*<Bands>*/ getBands() {
        if (bands == null) {
            bands = new ChildList.Bands(this);
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
        getBands().setEnum("type", type, GeographicMetadataFormat.SAMPLE_TYPES);
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
            throw new IllegalArgumentException(Errors.getResources(getWarningLocale()).getString(
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
        return getRootNode();
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
        bands       = null;
    }

    /**
     * Returns the language to use when {@linkplain warningOccurred logging a warning}.
     */
    public Locale getWarningLocale() {
        return locale;
    }

    /**
     * Sets the language to use when {@linkplain warningOccurred logging a warning}.
     */
    public void setWarningLocale(final Locale locale) {
        this.locale = locale;
    }

    /**
     * Sets the warning listeners, or {@code null} if none. If a non-null collection
     * is given, then calls to {@link #warningOccurred} will delegate to
     * {@link IIOReadWarningListener#warningOccurred} instead of
     * {@linkplain Logger#log(LogRecord) logging} a warning.
     */
    public void setWarningListeners(final Collection/*<IIOReadWarningListener>*/ warningListeners) {
        this.warningListeners = warningListeners;
    }

    /**
     * Invoked when a warning occured. This method is invoked when some inconsistency has been
     * detected in the geographic metadata. The default implementation make the following choice:
     *
     * <ul>
     *   <li>If a collection of {@linkplain IIOReadWarningListener warning listeners}
     *       has been {@linkplain #setWarningListeners specified}, then the
     *       {@link IIOReadWarningListener#warningOccurred warningOccurred} method is
     *       invoked for each of them and the log record is <strong>not</strong> logged.</li>
     *
     *   <li>Otherwise, the log record is logged without futher processing.</li>
     * </ul>
     *
     * Subclasses may override this method if more processing is wanted, or for
     * throwing exception if some warnings should be considered as fatal errors.
     */
    protected boolean warningOccurred(final LogRecord record) {
        if (warningListeners == null) {
            Logger.getLogger("org.geotools.image.io.metadata").log(record);
            return false;
        }
        final String message = record.getMessage();
        for (final Iterator it=warningListeners.iterator(); it.hasNext();) {
            final IIOReadWarningListener listener = (IIOReadWarningListener) it.next();
            listener.warningOccurred(null, message);
        }
        return true;
    }

    /**
     * Wraps the specified format in order to either parse fully a string, or log a warning.
     *
     * @param format The format to use for parsing and formatting.
     * @param type   The expected type of parsed values.
     */
    protected /*<T>*/ LoggedFormat createLoggedFormat(final Format format, final Class/*<T>*/ type) {
        return new LoggedFormat/*<T>*/(format, type) {
            //@Override
            protected Locale getWarningLocale() {
                return GeographicMetadata.this.getWarningLocale();
            }

            //@Override
            protected void logWarning(final LogRecord warning) {
                GeographicMetadata.this.warningOccurred(warning);
            }
        };
    }

    /**
     * Returns a standard date format to be shared by {@link MetadataAccessor}.
     */
    final LoggedFormat/*<Date>*/ dateFormat() {
        if (dateFormat == null) {
            final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateFormat = createLoggedFormat(format, Date.class);
            dateFormat.setLogger("org.geotools.image.io.metadata");
            dateFormat.setCaller(MetadataAccessor.class, "getDate");
        }
        return dateFormat;
    }

    /**
     * Returns a string representation of this metadata, mostly for debugging purpose.
     */
    public String toString() {
        return OptionalDependencies.toString(
                OptionalDependencies.xmlToSwing(getAsTree(GeographicMetadataFormat.FORMAT_NAME)));
    }
}
