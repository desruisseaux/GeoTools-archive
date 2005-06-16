/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2005, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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
 */
package org.geotools.coverage.io;

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

// OpenGIS dependencies
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridRange;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

// Geotools dependencies
import org.geotools.resources.gcs.ResourceKeys;


/**
 * An implementation of {@link AbstractGridCoverageReader} using informations parsed by a
 * {@link MetadataBuilder} object. This reader is typically used for format that
 * stores pixel values and geographic metadata in separated files. For example,
 * pixel values may be stored as a PNG images ou a RAW binary file, and geographic
 * metadata (coordinate system, geographic location, etc.) may be stored in a separated
 * text file. The text file is parsed by a {@link MetadataBuilder} object, while the pixel
 * values are read by a {@link ImageReader} object.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ExoreferencedGridCoverageReader extends AbstractGridCoverageReader {
    /**
     * The object to use for parsing the meta-data.
     */
    protected MetadataBuilder metadata;
    
    /**
     * File extension (by default the same than format name).
     */
    private final String extension;
    
    /**
     * Construct a new <code>ExoreferencedGridCoverageReader</code>
     * using the specified {@link MetadataBuilder}.
     *
     * @param formatName The name for this format. This format name should be
     *        understood by {@link ImageIO#getImageReadersByFormatName(String)},
     *        unless {@link #getImageReaders} is overriden.
     * @param parser The {@link MetadataBuilder} to use for reading geographic metadata.
     */
    public ExoreferencedGridCoverageReader(final String formatName, final MetadataBuilder parser) {
        this(formatName, formatName, parser);
    }
    
    /**
     * Construct a new <code>ExoreferencedGridCoverageReader</code>
     * using the specified {@link MetadataBuilder}.
     *
     * @param formatName The name for this format. This format name should be
     *        understood by {@link ImageIO#getImageReadersByFormatName(String)},
     *        unless {@link #getImageReaders} is overriden.
     * @param extension Filename's extensions for file of this format.
     * @param parser The {@link MetadataBuilder} to use for reading geographic metadata.
     */
    public ExoreferencedGridCoverageReader(final String formatName,
                                           final String extension,
                                           final MetadataBuilder parser)
    {
        super(formatName);
        metadata = parser;
        if (parser==null) {
            throw new IllegalArgumentException();
        }
        this.extension = extension;
    }
    
    /**
     * Restores the <code>AbstractGridCoverageReader</code> to its initial state.
     *
     * @throws IOException if an error occurs while disposing resources.
     */
    public synchronized void reset() throws IOException {
        metadata.clear();
        super.reset();
    }
    
    /**
     * Sets the input source to the given object. The input must be
     * {@link File} or an {@link URL} object. The input source must
     * be the <em>metadata</em> file or URL. The image file or URL
     * will be derived from the metadata filename by a call to
     * {@link #toImageFileName}, which may be overriden.
     *
     * @param  input The {@link File} or {@link URL} to be read.
     * @param  seekForwardOnly if <code>true</code>, grid coverages
     *         and metadata may only be read in ascending order from
     *         the input source.
     * @throws IOException if an I/O operation failed.
     * @throws IllegalArgumentException if input is not an instance
     *         of a classe supported by this reader.
     */
    public synchronized void setInput(Object input, final boolean seekForwardOnly)
        throws IOException
    {
        if (input instanceof File) {
            final File file = (File) input;
            metadata.clear();
            metadata.load(file);
            input = new File(file.getParent(), toImageFileName(file.getName()));
        } else if (input instanceof URL) {
            final URL url = (URL) input;
            metadata.clear();
            metadata.load(url);
            // TODO: invokes rename(String) here and rebuild the URL.
            throw new UnsupportedOperationException("URL support not yet implemented");
        } else {
            throw new IllegalArgumentException(getString(ResourceKeys.ERROR_NO_IMAGE_READER));
        }
        super.setInput(input, seekForwardOnly);
    }
    
    /**
     * Returns the filename for image data. This method is invoked by
     * {@link #setInput} after {@link #metadata} has been loaded.
     * Default implementation just replace the file extension by the
     * <code>extension</code> argument specified to the constructor.
     *
     * @param  filename The filename part of metadata file. This
     *         is the filename part of the file supplied by users
     *         to {@link #setInput}.
     * @return The filename to use for for the image file. The
     *         directory is assumed to be the same than the metadata file.
     */
    protected String toImageFileName(String filename) {
        int ext = filename.lastIndexOf('.');
        if (ext<0) {
            ext=filename.length();
        }
        return filename.substring(0, ext)+'.'+extension;
    }
    
    /**
     * Returns the coordinate system for the {@link GridCoverage} to be read.
     * The default implementation invokes
     * <code>{@link #metadata}.{@link MetadataBuilder#getCoordinateSystem() getCoordinateSystem()}</code>.
     *
     * @param  index The index of the image to be queried.
     * @return The coordinate system for the {@link GridCoverage} at the specified index.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs reading the width information from
     *         the input source.
     */
    public synchronized CoordinateReferenceSystem getCoordinateReferenceSystem(final int index)
            throws IOException
    {
        checkImageIndex(index);
        return metadata.getCoordinateReferenceSystem();
    }
    
    /**
     * Returns the envelope for the {@link GridCoverage} to be read.
     * The default implementation invokes
     * <code>{@link #metadata}.{@link MetadataBuilder#getEnvelope() getEnvelope()}</code>.
     *
     * @param  index The index of the image to be queried.
     * @return The envelope for the {@link GridCoverage} at the specified index.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs reading the width information from
     *         the input source.
     */
    public synchronized Envelope getEnvelope(final int index) throws IOException {
        checkImageIndex(index);
        return metadata.getEnvelope();
    }
    
    /**
     * Returns the grid range for the {@link GridCoverage} to be read.
     * The default implementation try to invoke
     * <code>{@link #metadata}.{@link MetadataBuilder#getGridRange() getGridRange()}</code>,
     * and fallback to <code>super.getGridRange(index)</code> if the later fails.
     *
     * @param  index The index of the image to be queried.
     * @return The grid range for the {@link GridCoverage} at the specified index.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs reading the width information from
     *         the input source.
     */
    public synchronized GridRange getGridRange(final int index) throws IOException {
        checkImageIndex(index);
        return metadata.getGridRange();
    }
    
    /**
     * Returns the sample dimensions for each band of the {@link GridCoverage}
     * to be read. If sample dimensions are not known, then this method returns
     * <code>null</code>. The default implementation invokes
     * <code>{@link #metadata}.{@link MetadataBuilder#getSampleDimensions()
     *       getSampleDimensions()}</code>.
     *
     * @param  index The index of the image to be queried.
     * @return The category lists for the {@link GridCoverage} at the specified index.
     *         This array's length must be equals to the number of bands in {@link GridCoverage}.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs reading the width information from
     *         the input source.
     */
    public synchronized SampleDimension[] getSampleDimensions(final int index) throws IOException {
        checkImageIndex(index);
        return metadata.getSampleDimensions();
    }
    
    /**
     * Sets the current {@link Locale} of this <code>AbstractGridCoverageReader</code>
     * to the given value. A value of <code>null</code> removes any previous
     * setting, and indicates that the reader should localize as it sees fit.
     */
    public synchronized void setLocale(final Locale locale) {
        super.setLocale(locale);
        metadata.setUserLocale(locale);
    }
}
