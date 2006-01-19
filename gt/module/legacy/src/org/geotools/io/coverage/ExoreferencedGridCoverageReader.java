/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
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
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.io.coverage;
// Image input/output
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverage;
import org.geotools.gc.GridRange;
import org.geotools.pt.Envelope;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.cs.CoordinateSystem;


/**
 * An implementation of {@link GridCoverageReader} using informations parsed by a
 * {@link PropertyParser} object. This reader is typically used for format that
 * stores pixel values and geographic metadata in separated files. For example,
 * pixel values may be stored as a PNG images ou a RAW binary file, and geographic
 * metadata (coordinate system, geographic location, etc.) may be stored in a separated
 * text file. The text file is parsed by a {@link PropertyParser} object, while the pixel
 * values are read by a {@link ImageReader} object.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @deprecated Replaced by {@link org.geotools.coverage.io.ExoreferencedGridCoverageReader}.
 */
public class ExoreferencedGridCoverageReader extends GridCoverageReader {
    /**
     * The object to use for parsing the meta-data.
     */
    protected PropertyParser properties;
    
    /**
     * File extension (by default the same than format name).
     */
    private final String extension;
    
    /**
     * Construct a new <code>ExoreferencedGridCoverageReader</code>
     * using the specified {@link PropertyParser}.
     *
     * @param formatName The name for this format. This format name should be
     *        understood by {@link ImageIO#getImageReadersByFormatName(String)},
     *        unless {@link #getImageReaders} is overriden.
     * @param parser The {@link PropertyParser} to use for reading geographic metadata.
     */
    public ExoreferencedGridCoverageReader(final String formatName, final PropertyParser parser) {
        this(formatName, formatName, parser);
    }
    
    /**
     * Construct a new <code>ExoreferencedGridCoverageReader</code>
     * using the specified {@link PropertyParser}.
     *
     * @param formatName The name for this format. This format name should be
     *        understood by {@link ImageIO#getImageReadersByFormatName(String)},
     *        unless {@link #getImageReaders} is overriden.
     * @param extension Filename's extensions for file of this format.
     * @param parser The {@link PropertyParser} to use for reading geographic metadata.
     */
    public ExoreferencedGridCoverageReader(final String formatName,
                                           final String extension,
                                           final PropertyParser parser)
    {
        super(formatName);
        properties=parser;
        if (parser==null) {
            throw new IllegalArgumentException();
        }
        this.extension = extension;
    }
    
    /**
     * Restores the <code>GridCoverageReader</code> to its initial state.
     *
     * @throws IOException if an error occurs while disposing resources.
     */
    public synchronized void reset() throws IOException {
        properties.clear();
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
            properties.clear();
            properties.load(file);
            input = new File(file.getParent(), toImageFileName(file.getName()));
        } else if (input instanceof URL) {
            final URL url = (URL) input;
            properties.clear();
            properties.load(url);
            // TODO: invokes rename(String) here and rebuild the URL.
            throw new UnsupportedOperationException("URL support not yet implemented");
        } else {
            throw new IllegalArgumentException(getString(ErrorKeys.NO_IMAGE_READER));
        }
        super.setInput(input, seekForwardOnly);
    }
    
    /**
     * Returns the filename for image data. This method is invoked by
     * {@link #setInput} after {@link #properties} has been loaded.
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
     * <code>{@link #properties}.{@link PropertyParser#getCoordinateSystem() getCoordinateSystem()}</code>.
     *
     * @param  index The index of the image to be queried.
     * @return The coordinate system for the {@link GridCoverage} at the specified index.
     * @throws IllegalStateException if the input source has not been set.
     * @throws IndexOutOfBoundsException if the supplied index is out of bounds.
     * @throws IOException if an error occurs reading the width information from
     *         the input source.
     */
    public synchronized CoordinateSystem getCoordinateSystem(final int index) throws IOException {
        checkImageIndex(index);
        return properties.getCoordinateSystem();
    }
    
    /**
     * Returns the envelope for the {@link GridCoverage} to be read.
     * The default implementation invokes
     * <code>{@link #properties}.{@link PropertyParser#getEnvelope() getEnvelope()}</code>.
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
        return properties.getEnvelope();
    }
    
    /**
     * Returns the grid range for the {@link GridCoverage} to be read.
     * The default implementation try to invoke
     * <code>{@link #properties}.{@link PropertyParser#getGridRange() getGridRange()}</code>,
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
        return properties.getGridRange();
    }
    
    /**
     * Returns the sample dimensions for each band of the {@link GridCoverage}
     * to be read. If sample dimensions are not known, then this method returns
     * <code>null</code>. The default implementation invokes
     * <code>{@link #properties}.{@link PropertyParser#getSampleDimensions()
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
        return properties.getSampleDimensions();
    }
    
    /**
     * Sets the current {@link Locale} of this <code>GridCoverageReader</code>
     * to the given value. A value of <code>null</code> removes any previous
     * setting, and indicates that the reader should localize as it sees fit.
     */
    public synchronized void setLocale(final Locale locale) {
        super.setLocale(locale);
        properties.setUserLocale(locale);
    }
}
