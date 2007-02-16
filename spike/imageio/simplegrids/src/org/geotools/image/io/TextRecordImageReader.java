/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
 */
package org.geotools.image.io;

// J2SE and JAI dependencies
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;
import javax.media.jai.util.Range;

// Geotools dependencies
import org.geotools.io.LineFormat;
import org.geotools.util.NumberRange;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Descriptions;
import org.geotools.resources.i18n.DescriptionKeys;


/**
 * Image decoder for text files storing pixel values as records.
 * Such text files use one line (record) by pixel. Each line contains
 * at least 3 columns (in arbitrary order):
 *
 * <ul>
 *   <li>Pixel's <var>x</var> coordinate.</li>
 *   <li>Pixel's <var>y</var> coordinate.</li>
 *   <li>An arbitrary number of pixel values.</li>
 * </ul>
 *
 * For example, some Sea Level Anomaly (SLA) files contains rows of longitude
 * (degrees), latitude (degrees), SLA (cm), East/West current (cm/s) and
 * North/South current (cm/s), as below:
 *
 * <blockquote><pre>
 * 45.1250 -29.8750    -7.28     10.3483     -0.3164
 * 45.1250 -29.6250    -4.97     11.8847      3.6192
 * 45.1250 -29.3750    -2.91      3.7900      3.0858
 * 45.1250 -29.1250    -3.48     -5.1833     -5.0759
 * 45.1250 -28.8750    -4.36     -1.8129    -16.3689
 * 45.1250 -28.6250    -3.91      7.5577    -24.6801
 * </pre>(...etc...)
 * </blockquote>
 *
 * From this decoder point of view, the two first columns (longitude and latitude)
 * are pixel's logical coordinate (<var>x</var>,<var>y</var>), while the three last
 * columns are three image's bands. The whole file contains only one image (unless
 * {@link #getNumImages} has been overridden). All (<var>x</var>,<var>y</var>)
 * coordinates belong to pixel's center. This decoder will automatically translate
 * (<var>x</var>,<var>y</var>) coordinates from logical space to pixel space. The
 * {@link #getTransform} method provides a convenient {@link AffineTransform} for
 * performing coordinate transformations between pixel and logical spaces.
 * <p>
 * By default, {@code TextRecordImageReader} assumes that <var>x</var> and
 * <var>y</var> coordinates appear in column #0 and 1 respectively. It also assumes
 * that numeric values are encoded using current defaults {@link java.nio.charset.Charset}
 * and {@link java.util.Locale}, and that there is no pad value. The easiest way to change
 * the default setting is to create a {@link Spi} subclass. There is no need to subclass
 * {@code TextRecordImageReader}, unless you want more control on the decoding process.
 *
 * @since 2.1
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/coverage/src/main/java/org/geotools/image/io/TextRecordImageReader.java $
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class TextRecordImageReader extends TextImageReader {
    /**
     * Petit facteur de tolérance servant à tenir compte des erreurs d'arrondissement.
     */
    private static final float EPS = 1E-5f;
    
    /**
     * Intervalle (en nombre d'octets) entre les rapports de progrès.
     */
    private static final int PROGRESS_INTERVAL = 4096;
    
    /**
     * Lorsque la lecture se fait par-dessus une image {@link BufferedReader} existante,
     * indique s'il faut effacer la région dans laquelle sera placée l'image avant de la
     * lire. La valeur {@code false} permettra de conserver les anciens pixels dans
     * les régions ou le fichier ne définit pas de nouvelles valeurs.
     */
    private static final boolean CLEAR = true;

    /**
     * An empty array, used as a flag for comment lines.
     */
    private static final double[] EMPTY = new double[0];
    
    /**
     * Numéro de colonne des <var>x</var>, compté à partir de 0.
     * Ce champ n'existe que pour des raisons de performances; il
     * n'est utilisé que par {@link #parseLine} pendant la lecture
     * d'une image. Dans tous les autres cas, on utilisera plutôt
     * {@link #getColumnX}.
     */
    private transient int xColumn = 0;
    
    /**
     * Numéro de colonne des <var>y</var>, compté à partir de 0.
     * Ce champ n'existe que pour des raisons de performances; il
     * n'est utilisé que par {@link #parseLine} pendant la lecture
     * d'une image. Dans tous les autres cas, on utilisera plutôt
     * {@link #getColumnY}.
     */
    private transient int yColumn = 1;
    
    /**
     * Valeur représentant les données manquantes,   ou {@link Double#NaN} s'il n'y en
     * a pas. Ce champ n'existe que pour des raisons de performances; il n'est utilisé
     * que par {@link #parseLine} pendant la lecture d'une image. Dans tous les autres
     * cas, on utilisera plutôt {@link #getPadValue}.
     */
    private transient double padValue = Double.NaN;
    
    /**
     * Objet à utiliser pour lire chacune des lignes de fichier. Ce champ n'existe que
     * pour des raisons de performances; il n'est utilisé que par {@link #parseLine}
     * pendant la lecture d'une image. Dans tous les autres cas, on utilisera plutôt
     * {@link #getLineFormat}.
     */
    private transient LineFormat lineFormat;
    
    /**
     * Données des images, ou {@code null} si aucune lecture n'a encore été
     * faite. Chaque élément contient les données de l'image à l'index correspondant
     * (i.e. l'élément {@code data[0]} contient les données de l'image #0,
     * {@code data[1]} contient les données de l'image #1, etc.). Des éléments
     * de ce tableau peuvent être nuls si les données des images correspondantes
     * ne sont pas retenues après chaque lecture (c'est-à-dire si
     * <code>{@link #seekForwardOnly}==true</code>).
     */
    private RecordList[] data;
    
    /**
     * Index de la prochaine image à lire. Cet index n'est pas nécessairement
     * égal à la longueur du tableau {@link #data}. Il peut être aussi bien
     * plus petit que plus grand.
     */
    private int nextImageIndex;
    
    /**
     * Nombre moyen de caractères par données (incluant les espaces et les codes
     * de fin de ligne). Cette information n'est qu'à titre indicative, mais son
     * exactitude peut aider à accelerer la lecture et rendre les rapport des
     * progrès plus précis. Elle sera automatiquement mise à jour en fonction
     * des lignes lues.
     */
    private float expectedDatumLength = 10.4f;
    
    /**
     * Constructs a new decoder for images of type {@link DataBuffer#TYPE_FLOAT}.
     *
     * @param provider The provider which created this decoder.
     */
    public TextRecordImageReader(final ImageReaderSpi provider) {
        super(provider, DataBuffer.TYPE_FLOAT);
    }
    
    /**
     * Constructs a new decoder for images of the specified type.
     *
     * @param provider The provider which created this decoder.
     * @param rawImageType The image type, as one of {@link DataBuffer} constants. Most useful
     *        types are {@link DataBuffer#TYPE_INT} and {@link DataBuffer#TYPE_FLOAT}. Type
     *        {@link DataBuffer#TYPE_DOUBLE} is legal but deprecated, since the current
     *        implementation will cast the values to type {@code float} anyway.
     */
    public TextRecordImageReader(final ImageReaderSpi provider, final int rawImageType) {
        super(provider, rawImageType);
        clear();
        if (rawImageType == DataBuffer.TYPE_DOUBLE) {
            Logger.getLogger("org.geotools.image.io").warning("Type double is deprecated.");
        }
    }
    
    /**
     * Returns the grid tolerance (epsilon) value.
     */
    private float getGridTolerance() {
        return (originatingProvider instanceof Spi) ? ((Spi)originatingProvider).gridTolerance : EPS;
    }
    
    /**
     * Returns the column number for <var>x</var> values. The default implementation returns
     * the value specified to the {@linkplain TextRecordImageReader.Spi provider constructor}.
     * Subclasses should override this method if they want to fetch this information from an
     * other source.
     *
     * @param  imageIndex The index of the image to be queried.
     * @throws IOException If an error occurs reading the from the input source.
     */
    public int getColumnX(final int imageIndex) throws IOException {
        return (originatingProvider instanceof Spi) ? ((Spi)originatingProvider).xColumn : 0;
    }
    
    /**
     * Returns the column number for <var>y</var> values. The default implementation returns
     * the value specified to the {@linkplain TextRecordImageReader.Spi provider constructor}.
     * Subclasses should override this method if they want to fetch this information from an
     * other source.
     *
     * @param  imageIndex The index of the image to be queried.
     * @throws IOException If an error occurs reading the from the input source.
     */
    public int getColumnY(final int imageIndex) throws IOException {
        return (originatingProvider instanceof Spi) ? ((Spi)originatingProvider).yColumn : 1;
    }
    
    /**
     * Retourne le numéro de colonne dans laquelle se trouvent les données de la
     * bande spécifiée. L'implémentation par défaut retourne {@code band}+1
     * ou 2 si la bande est plus grand ou égal à {@link #getColumnX} et/ou
     * {@link #getColumnY}. Cette implémentation devrait convenir pour des données
     * se trouvant aussi bien avant qu'après les colonnes <var>x</var>
     * et <var>y</var>, même si ces dernières ne sont pas consécutives.
     *
     * @param  imageIndex Index de l'image à lire.
     * @param  band Bande de l'image à lire.
     * @return Numéro de colonne des données de l'image.
     * @throws IOException si l'opération nécessitait une lecture du fichier (par exemple
     *         des informations inscrites dans un en-tête) et que cette lecture a échouée.
     */
    private int getColumn(final int imageIndex, int band) throws IOException {
        final int xColumn = getColumnX(imageIndex);
        final int yColumn = getColumnY(imageIndex);
        if (band >= Math.min(xColumn, yColumn)) band++;
        if (band >= Math.max(xColumn, yColumn)) band++;
        return band;
    }
    
    /**
     * Set the input source. It should be one of the following object, in preference order:
     * {@link java.io.File}, {@link java.net.URL}, {@link java.io.BufferedReader}.
     * {@link java.io.Reader}, {@link java.io.InputStream} or
     * {@link javax.imageio.stream.ImageInputStream}.
     */
    public void setInput(final Object  input,
                         final boolean seekForwardOnly,
                         final boolean ignoreMetadata)
    {
        clear();
        super.setInput(input, seekForwardOnly, ignoreMetadata);
    }
    
    /**
     * Returns the number of bands available for the specified image.
     *
     * @param  imageIndex  The image index.
     * @throws IOException if an error occurs reading the information from the input source.
     */
    public int getNumBands(final int imageIndex) throws IOException {
        return getRecords(imageIndex).getColumnCount() -
                (getColumnX(imageIndex)==getColumnY(imageIndex) ? 1 : 2);
    }
    
    /**
     * Returns the width in pixels of the given image within the input source.
     *
     * @param  imageIndex the index of the image to be queried.
     * @return Image width.
     * @throws IOException If an error occurs reading the width information from the input source.
     */
    public int getWidth(final int imageIndex) throws IOException {
        return getRecords(imageIndex).getPointCount(getColumnX(imageIndex), getGridTolerance());
    }
    
    /**
     * Returns the height in pixels of the given image within the input source.
     *
     * @param  imageIndex the index of the image to be queried.
     * @return Image height.
     * @throws IOException If an error occurs reading the height information from the input source.
     */
    public int getHeight(final int imageIndex) throws IOException {
        return getRecords(imageIndex).getPointCount(getColumnY(imageIndex), getGridTolerance());
    }
    
    /**
     * Returns the smallest bounding box containing the full image in user coordinates.
     * The default implementation search for minimum and maximum values in <var>x</var>
     * and <var>y</var> columns (as returned by {@link #getColumnX} and {@link #getColumnY})
     * and returns a rectangle containing <code>(xmin-dx/2,&nbsp;ymin-dy/2)</code>) and
     * <code>(xmax+dx/2,&nbsp;ymax+dy/2)</code>) points, where <var>dx</var> and
     * <var>dy</var> are grid cell width and height (i.e. the smallest interval between
     * <var>x</var> and <var>y</var> values).
     *
     * @param  imageIndex the index of the image to be queried.
     * @return Image bounds in user coordinates.
     * @throws IOException If an error occurs reading the width information from the input source.
     */
    public Rectangle2D getLogicalBounds(final int imageIndex) throws IOException {
        final float    tolerance = getGridTolerance();
        final RecordList records = getRecords(imageIndex);
        final int xColumn        = getColumnX(imageIndex);
        final int yColumn        = getColumnY(imageIndex);
        final double xmin        = records.getMinimum(xColumn);
        final double ymin        = records.getMinimum(yColumn);
        final double width       = records.getMaximum(xColumn)-xmin;
        final double height      = records.getMaximum(yColumn)-ymin;
        final double dx          = width /(records.getPointCount(xColumn, tolerance)-1);
        final double dy          = height/(records.getPointCount(yColumn, tolerance)-1);
        return new Rectangle2D.Double(xmin-0.5*dx, ymin-0.5*dy, width+dx, height+dy);
    }
    
    /**
     * Returns an {@link AffineTransform} for transforming pixel coordinates to logical coordinates.
     * Pixel coordinates are usually integer values with (0,0) at the image's upper-left corner,
     * while logical coordinates are floating point values at the pixel's upper-left corner. The
     * later is consistent with <a href="http://java.sun.com/products/java-media/jai/">Java
     * Advanced Imaging</a> convention. In order to get logical values at the pixel center,
     * a translation must be apply once as below:
     *
     * <blockquote><pre>
     * AffineTransform tr = getTransform(imageIndex);
     * tr.translate(0.5, 0.5);
     * </pre></blockquote>
     *
     * The default implementation computes the affine transform from {@link #getLogicalBounds},
     * {@link #getWidth} and {@link #getHeight}.
     *
     * @param  imageIndex the index of the image to be queried.
     * @return A transform mapping pixel coordinates to logical coordinates.
     * @throws IOException if an I/O operation failed.
     */
    public AffineTransform getTransform(final int imageIndex) throws IOException {
        final Rectangle2D bounds = getLogicalBounds(imageIndex);
        final int          width = getWidth        (imageIndex);
        final int         height = getHeight       (imageIndex);
        final double  pixelWidth = bounds.getWidth()/ (width-1);
        final double pixelHeight = bounds.getHeight()/(height-1);
        return new AffineTransform(pixelWidth, 0, 0, -pixelHeight,
                                   bounds.getMinX()-0.5*pixelWidth,
                                   bounds.getMaxY()+0.5*pixelHeight);
    }
    
    /**
     * Returns the range of values for the specified band.
     *
     * @param  imageIndex The image index.
     * @param  band       The band index. Valid index goes from {@code 0} inclusive
     *         to {@code getNumBands(imageIndex)} exclusive. Index are independent
     *         of any {@link ImageReadParam#setSourceBands} setting.
     * @return The expected range of values, or {@code null} if unknow.
     * @throws IOException If an error occurs reading the data information from the input source.
     */
    public Range getExpectedRange(final int imageIndex, final int band) throws IOException {
        final int         column = getColumn(imageIndex, band);
        final RecordList records = getRecords(imageIndex);
        return new NumberRange(records.getMinimum(column), records.getMaximum(column));
    }
    
    /**
     * Converts a line from the {@linkplain #input input} stream to numerical values. This method
     * is invoked automatically for each line to be read. The default implementation parse the line
     * using the object created by {@link #getLineFormat}, and replace all occurences of the
     * {@linkplain #getPadValue pad value} by {@link Double#NaN} in every columns except the ones
     * for {@linkplain #getColumnX x} and {@linkplain #getColumnY y} values.
     * <p>
     * <strong>TIP:</strong> This method is a convenient place where to fix rounding error in
     * latitude and longitude coordinates. For example if longitude have a step 1/6° but are
     * written with only 3 decimal digits, we get {@linkplain #getColumnX x} values like
     * {@code 10.000}, {@code 10.167}, {@code 10.333}, <cite>etc</cite>, which can leads to an
     * error of 0.001° in longitude. This error may cause {@code TextRecordImageReader} to fails
     * validation tests and throws an {@link javax.imageio.IIOException}: "<cite>Points dont seem
     * to be distributed on a regular grid</cite>". A work around is to multiply the <var>x</var>
     * and <var>y</var> coordinates by 6, round to the nearest integer and divide them by 6.
     *
     * @param line   The line to parse.
     * @param values The destination buffer where to stores the values, or {@code null} if none.
     * @return       The parsed values, or {@code null} if {@code line} was null (which usually
     *               means end-of-stream). By convention, an array of length 0 means "no data"
     *               (e.g. a comment line).
     * @throws ParseException If the line can't be parsed.
     */
    protected double[] parseLine(final String line, double[] values) throws ParseException {
        if (line == null) {
            return null;
        }
        if (isComment(line) || lineFormat.setLine(line) == 0) {
            return EMPTY;
        }
        values = lineFormat.getValues(values);
        for (int i=0; i<values.length; i++) {
            if (i!=xColumn && i!=yColumn && values[i]==padValue) {
                values[i] = Double.NaN;
            }
        }
        return values;
    }
    
    /**
     * Retourne les données de l'image à l'index spécifié. Si cette image avait déjà été lue, ses
     * données seront retournées immédiatement.  Sinon, cette image sera lue ainsi que toutes les
     * images qui précèdent {@code imageIndex} et qui n'avaient pas encore été lues. Que ces
     * images précédentes soient mémorisées ou oubliées dépend de {@link #seekForwardOnly}.
     *
     * @param  imageIndex Index de l'image à lire.
     * @return Les données de l'image. Cette méthode ne retourne jamais {@code null}.
     * @throws IOException si une erreur est survenue lors de la lecture du flot,
     *         ou si des nombres n'étaient pas correctement formatés dans le flot.
     * @throws IndexOutOfBoundsException si l'index spécifié est en dehors des
     *         limites permises ou si aucune image n'a été conservée à cet index.
     */
    private RecordList getRecords(final int imageIndex) throws IOException {
        clearAbortRequest();
        checkImageIndex(imageIndex);
        if (imageIndex >= nextImageIndex) {
            processImageStarted(imageIndex);
            final BufferedReader reader = getReader();
            final long          origine = getStreamPosition(reader);
            final long           length = getStreamLength(nextImageIndex, imageIndex+1);
            long   nextProgressPosition = (origine>=0 && length>0) ? 0 : Long.MAX_VALUE;
            for (; nextImageIndex<=imageIndex; nextImageIndex++) {
                /*
                 * Réduit la consommation de mémoire des images précédentes. On ne réduit
                 * pas celle de l'image courante,  puisque la plupart du temps le tableau
                 * sera bientôt détruit de toute façon.
                 */
                if (seekForwardOnly) {
                    minIndex=nextImageIndex;
                }
                if (nextImageIndex!=0 && data!=null) {
                    final RecordList records = data[nextImageIndex-1];
                    if (records != null) {
                        if (seekForwardOnly) {
                            data[nextImageIndex-1]=null;
                        } else {
                            records.trimToSize();
                        }
                    }
                }
                /*
                 * Procède à la lecture de chacune des lignes de données. Que ces lignes
                 * soient mémorisées ou pas dépend de l'image que l'on est en train de
                 * décoder ainsi que de la valeur de {@link #seekForwardOnly}.
                 */
                double[]    values = null;
                RecordList records = null;
                final boolean  keep = (nextImageIndex==imageIndex) || !seekForwardOnly;
                // Initializes temporary fields used by 'parseLine'.
                this.xColumn    = getColumnX   (nextImageIndex);
                this.yColumn    = getColumnY   (nextImageIndex);
                this.padValue   = getPadValue  (nextImageIndex);
                this.lineFormat = getLineFormat(nextImageIndex);
                try {
                    String line;
                    while ((line=reader.readLine()) != null) {
                        final double[] candidate = parseLine(line, values);
                        if (candidate == null) {
                            break;
                        }
                        if (candidate.length == 0) {
                            continue;
                        }
                        values = candidate;
                        if (keep) {
                            if (records == null) {
                                final int expectedLineCount = Math.max(8, Math.min(65536,
                                        Math.round(length / (expectedDatumLength*values.length))));
                                records = new RecordList(values.length, expectedLineCount);
                            }
                            records.add(values);
                        }
                        final long position = getStreamPosition(reader)-origine;
                        if (position >= nextProgressPosition) {
                            processImageProgress(position * (100f/length));
                            nextProgressPosition = position + PROGRESS_INTERVAL;
                            if (abortRequested()) {
                                processReadAborted();
                                return records;
                            }
                        }
                    }
                } catch (ParseException exception) {
                    throw new IIOException(getPositionString(exception.getLocalizedMessage()), exception);
                }
                /*
                 * Après la lecture d'une image, vérifie s'il y avait un nombre suffisant de lignes.
                 * Une exception sera lancée si l'image ne contenait pas au moins deux lignes. On
                 * ajustera ensuite le nombre moyens de caractères par données.
                 */
                if (records!=null) {
                    final int lineCount = records.getLineCount();
                    if (lineCount<2) {
                        throw new IIOException(getPositionString(Errors.format(
                                               ErrorKeys.FILE_HAS_TOO_FEW_DATA)));
                    }
                    if (data==null) {
                        data = new RecordList[imageIndex+1];
                    } else if (data.length <= imageIndex) {
                        data = (RecordList[]) XArray.resize(data, imageIndex+1);
                    }
                    data[nextImageIndex] = records;
                    final float meanDatumLength = (getStreamPosition(reader)-origine) / (float)records.getDataCount();
                    if (meanDatumLength>0) expectedDatumLength = meanDatumLength;
                }
            }
            processImageComplete();
        }
        /*
         * Une fois les lectures terminées, retourne les données de l'image
         * demandée. Une exception sera lancée si ces données n'ont pas été
         * conservées.
         */
        if (data!=null && imageIndex<data.length) {
            final RecordList records = data[imageIndex];
            if (records!=null) {
                return records;
            }
        }
        throw new IndexOutOfBoundsException(String.valueOf(imageIndex));
    }
    
    /**
     * Reads the image indexed by {@code imageIndex} and returns it as a complete buffered image.
     *
     * @param  imageIndex the index of the image to be retrieved.
     * @param  param Parameters used to control the reading process, or {@code null}.
     * @return the desired portion of the image.
     * @throws IOException if an error occurs during reading.
     */
    public BufferedImage read(final int imageIndex, final ImageReadParam param) throws IOException {
        final float        tolerance = getGridTolerance();
        final int            xColumn = getColumnX(imageIndex);
        final int            yColumn = getColumnY(imageIndex);
        final RecordList     records = getRecords(imageIndex);
        final int              width = records.getPointCount(xColumn, tolerance);
        final int             height = records.getPointCount(yColumn, tolerance);
        final int        numSrcBands = records.getColumnCount() - (xColumn==yColumn ? 1 : 2);
        /*
         * Extract user's parameters
         */
        final int[]         srcBands;
        final int[]         dstBands;
        final int sourceXSubsampling;
        final int sourceYSubsampling;
        final int subsamplingXOffset;
        final int subsamplingYOffset;
        final int destinationXOffset;
        final int destinationYOffset;
        if (param != null) {
            srcBands           = param.getSourceBands();
            dstBands           = param.getDestinationBands();
            final Point offset = param.getDestinationOffset();
            sourceXSubsampling = param.getSourceXSubsampling();
            sourceYSubsampling = param.getSourceYSubsampling();
            subsamplingXOffset = param.getSubsamplingXOffset();
            subsamplingYOffset = param.getSubsamplingYOffset();
            destinationXOffset = offset.x;
            destinationYOffset = offset.y;
        } else {
            srcBands    = null;
            dstBands    = null;
            sourceXSubsampling = 1;
            sourceYSubsampling = 1;
            subsamplingXOffset = 0;
            subsamplingYOffset = 0;
            destinationXOffset = 0;
            destinationYOffset = 0;
        }
        /*
         * Initialize...
         */
        final int numDstBands = (dstBands!=null) ? dstBands.length :
                                (srcBands!=null) ? srcBands.length : numSrcBands;
        final BufferedImage image = getDestination(param,
                                    getImageTypes(imageIndex, numDstBands), width, height);
        checkReadParamBandSettings(param, numSrcBands, image.getSampleModel().getNumBands());
        
        final Rectangle    srcRegion = new Rectangle();
        final Rectangle    dstRegion = new Rectangle();
        computeRegions(param, width, height, image, srcRegion, dstRegion);
        final int         sourceXMin = srcRegion.x;
        final int         sourceYMin = srcRegion.y;
        final int         sourceXMax = srcRegion.width  + sourceXMin;
        final int         sourceYMax = srcRegion.height + sourceYMin;
        
        final WritableRaster  raster = image.getRaster();
        final int        rasterWidth = raster.getWidth();
        final int       rasterHeigth = raster.getHeight();
        final int        columnCount = records.getColumnCount();
        final int          dataCount = records.getDataCount();
        final float[]           data = records.getData();
        final double            xmin = records.getMinimum(xColumn);
        final double            ymin = records.getMinimum(yColumn);
        final double            xmax = records.getMaximum(xColumn);
        final double            ymax = records.getMaximum(yColumn);
        final double          scaleX = (width -1)/(xmax-xmin);
        final double          scaleY = (height-1)/(ymax-ymin);
        /*
         * Clear the image area. All values are set to NaN.
         */
        if (CLEAR) {
            final int minX = dstRegion.x;
            final int minY = dstRegion.y;
            final int maxX = dstRegion.width  + minX;
            final int maxY = dstRegion.height + minY;
            for (int b=(dstBands!=null) ? dstBands.length : numDstBands; --b>=0;) {
                final int band = (dstBands!=null) ? dstBands[b] : b;
                for (int y=minY; y<maxY; y++) {
                    for (int x=minX; x<maxX; x++) {
                        raster.setSample(x, y, band, Float.NaN);
                    }
                }
            }
        }
        /*
         * Compute column numbers corresponding to source bands,
         * and start storing values into the image.
         */
        final int[] columns = new int[(srcBands!=null) ? srcBands.length : numDstBands];
        for (int i=0; i<columns.length; i++) {
            columns[i] = getColumn(imageIndex, srcBands!=null ? srcBands[i] : i);
        }
        for (int i=0; i<dataCount; i+=columnCount) {
            /*
             * On convertit maintenant la coordonnée (x,y) logique en coordonnée pixel. Cette
             * coordonnée pixel se réfère à l'image "source";  elle ne se réfère pas encore à
             * l'image destination. Elle doit obligatoirement être entière. Plus loin, nous
             * tiendrons compte du "subsampling".
             */
            final double fx = (data[i+xColumn]-xmin)*scaleX; // (fx,fy) may be NaN: Use
            final double fy = (ymax-data[i+yColumn])*scaleY; // "!(abs(...)<=tolerance)".
            int           x = (int)Math.round(fx); // This conversion is not the same than
            int           y = (int)Math.round(fy); // getTransform(), but it should be ok.
            if (!(Math.abs(x-fx)<=tolerance)) {fireBadCoordinate(data[i+xColumn]); continue;}
            if (!(Math.abs(y-fy)<=tolerance)) {fireBadCoordinate(data[i+yColumn]); continue;}
            if (x>=sourceXMin && x<sourceXMax && y>=sourceYMin && y<sourceYMax) {
                x -= subsamplingXOffset;
                y -= subsamplingYOffset;
                if ((x % sourceXSubsampling)==0 && (y % sourceYSubsampling)==0) {
                    x = x/sourceXSubsampling + (destinationXOffset-sourceXMin);
                    y = y/sourceYSubsampling + (destinationYOffset-sourceYMin);
                    if (x<rasterWidth && y<rasterHeigth) {
                        for (int j=0; j<columns.length; j++) {
                            raster.setSample(x, y, (dstBands!=null ? dstBands[j] : j), data[i+columns[j]]);
                        }
                    }
                }
            }
        }
        return image;
    }
    
    /**
     * Retourne quelques types d'images qui pourront contenir les données.
     * Le premier type retourné sera celui qui se rapprochera le plus du
     * type des données à lire.
     *
     * @param  imageIndex Index de l'image dont on veut les types.
     * @param  numBanfd Nombre de bandes.
     * @return Itérateur balayant les types de l'image.
     * @throws IndexOutOfBoundsException si {@code imageIndex} est invalide.
     * @throws IllegalStateException si aucune source n'a été spécifiée avec {@link #setInput}.
     * @throws IIOException si l'opération a échoué pour une autre raison.
     */
    private Iterator getImageTypes(final int imageIndex, final int numBands) throws IOException {
        final List list = new ArrayList();
        list.add(getRawImageType(imageIndex, numBands));
        for (final Iterator it=getImageTypes(imageIndex); it.hasNext();) {
            list.add((ImageTypeSpecifier) it.next());
        }
        return list.iterator();
    }
    
    /**
     * Prévient qu'une coordonnée est mauvaise. Cette méthode est appelée lors de la lecture
     * s'il a été détecté qu'une coordonnée est en dehors des limites prévues, ou qu'elle ne
     * correspond pas à des coordonnées pixels entières.
     */
    private void fireBadCoordinate(final float coordinate) {
        processWarningOccurred(getPositionString(Errors.format(
                ErrorKeys.BAD_COORDINATE_$1, new Float(coordinate))));
    }
    
    /**
     * Supprime les données de toutes les images
     * qui avait été conservées en mémoire.
     */
    private void clear() {
        data                = null;
        lineFormat          = null;
        nextImageIndex      = 0;
        expectedDatumLength = 10.4f;
        if (originatingProvider instanceof Spi) {
            final Spi provider = (Spi) originatingProvider;
            xColumn  = provider.xColumn;
            yColumn  = provider.yColumn;
            padValue = provider.padValue;
        } else {
            xColumn  = 0;
            yColumn  = 1;
            padValue = Double.NaN;
        }
    }
    
    /**
     * Restores the {@code TextRecordImageReader} to its initial state.
     */
    public void reset() {
        clear();
        super.reset();
    }
    
    
    
    
    /**
     * Service provider interface (SPI) for {@link TextRecordImageReader}s.
     * This SPI provides all necessary implementations for creating default
     * {@link TextRecordImageReader}. Subclasses only have to set some fields
     * at construction time, e.g.:
     *
     * <blockquote><pre>
     * public final class CLSImageReaderSpi extends TextRecordImageReader.Spi
     * {
     *     public CLSImageReaderSpi()
     *     {
     *         super("CLS", "text/x-grid-CLS");
     *         {@link #vendorName vendorName} = "Institut de Recherche pour le Développement";
     *         {@link #version    version}    = "1.0";
     *         {@link #locale     locale}     = Locale.US;
     *         {@link #charset    charset}    = Charset.forName("ISO-LATIN-1");
     *         {@link #padValue   padValue}   = 9999;
     *     }
     * }
     * </pre></blockquote>
     *
     * (Note: fields {@code vendorName} and {@code version} are only informatives).
     * There is no need to override any method in this example. However, developers
     * can gain more control by creating subclasses of {@link TextRecordImageReader}
     * <strong>and</strong> {@code Spi} and overriding some of their methods.
     *
     * @since 2.1
     * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/coverage/src/main/java/org/geotools/image/io/TextRecordImageReader.java $
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Spi extends TextImageReader.Spi {
        /**
         * Numéro de colonne des <var>x</var>, compté à partir de 0.
         * Par défaut, on suppose que les <var>x</var> se trouvent
         * dans la première colonne (colonne #0).
         *
         * @see TextRecordImageReader#getColumnX
         * @see TextRecordImageReader#parseLine
         */
        final int xColumn;
        
        /**
         * Numéro de colonne des <var>y</var>, compté à partir de 0.
         * Par défaut, on suppose que les <var>y</var> se trouvent
         * dans la deuxième colonne (colonne #1).
         *
         * @see TextRecordImageReader#getColumnY
         * @see TextRecordImageReader#parseLine
         */
        final int yColumn;
        
        /**
         * A tolerance factor during decoding, between 0 and 1. During decoding,
         * the image reader compute cell's width and height   (i.e. the smallest
         * non-null difference between ordinates in a given column: <var>x</var>
         * for cell's width and <var>y</var> for cell's height). Then, it checks
         * if every coordinate points fall on a grid having this cell's size. If
         * a point depart from more than {@code gridTolerance} percent of cell's
         * width or height, an exception is thrown.
         * <p>
         * {@code gridTolerance} should be a small number like {@code 1E-5f}
         * or {@code 1E-3f}. The later is more tolerant than the former.
         */
        protected float gridTolerance = EPS;
        
        /**
         * Constructs a new SPI with name "gridded records" and MIME type "text/x-grid".
         */
        public Spi() {
            this("gridded records", "text/x-grid");
        }
        
        /**
         * Constructs a new SPI for {@link TextRecordImageReader}. <var>x</var> and
         * <var>y</var> columns are assumed to be in column #0 and 1 respectively.
         * Others parameters are initialized as in superclass constructor.
         *
         * @param name Format name, or {@code null} to let {@link #names} unset.
         * @param mime MIME type, or {@code null} to let {@link #MIMETypes} unset.
         */
        public Spi(final String name, final String mime) {
            this(name, mime, 0, 1);
        }
        
        /**
         * Constructs a new SPI for {@link TextRecordImageReader}.
         *
         * @param name Format name, or {@code null} to let {@link #names} unset.
         * @param mime MIME type, or {@code null} to let {@link #MIMETypes} unset.
         * @param xColumn 0-based column number for <var>x</var> values.
         * @param yColumn 0-based column number for <var>y</var> values.
         */
        public Spi(final String name, final String mime, final int xColumn, final int yColumn) {
            super(name, mime);
            this.xColumn = xColumn;
            this.yColumn = yColumn;
            if (xColumn < 0) {
                throw new IllegalArgumentException(Errors.format(
                        ErrorKeys.NEGATIVE_COLUMN_$2, "x", new Integer(xColumn)));
            }
            if (yColumn < 0) {
                throw new IllegalArgumentException(Errors.format(
                        ErrorKeys.NEGATIVE_COLUMN_$2, "y", new Integer(yColumn)));
            }
            pluginClassName = "org.geotools.image.io.TextRecordImageReader";
        }
        
        /**
         * Returns a brief, human-readable description of this service provider
         * and its associated implementation. The resulting string should be
         * localized for the supplied locale, if possible.
         *
         * @param  locale A Locale for which the return value should be localized.
         * @return A String containing a description of this service provider.
         */
        public String getDescription(final Locale locale) {
            return Descriptions.getResources(locale).getString(DescriptionKeys.CODEC_GRID);
        }
        
        /**
         * Vérifie si la ligne spécifiée peut être décodée.
         *
         * @param  line Une des premières lignes du flot à lire.
         * @return {@link Boolean#TRUE} si la ligne peut être décodée, {@link Boolean#FALSE}
         *         si elle ne peut pas être décodée ou {@code null} si on ne sait pas.
         *         Dans ce dernier cas, cette méthode sera appelée une nouvelle fois avec la
         *         ligne suivante.
         */
        protected Boolean canDecodeLine(final String line) {
            if (line.trim().length()!=0) {
                try {
                    final LineFormat reader = (locale!=null) ? new LineFormat(locale) :
                                                               new LineFormat();
                    if (reader.setLine(line) >= (xColumn==yColumn ? 2 : 3)) {
                        return Boolean.TRUE;
                    }
                } catch (ParseException exception) {
                    return Boolean.FALSE;
                }
            }
            return null;
        }
        
        /**
         * Returns an instance of the ImageReader implementation associated
         * with this service provider.
         *
         * @param  extension An optional extension object, which may be null.
         * @return An image reader instance.
         * @throws IOException if the attempt to instantiate the reader fails.
         */
        public ImageReader createReaderInstance(final Object extension) throws IOException {
            return new TextRecordImageReader(this);
        }
        
        /**
         * Vérifie si la ligne a un nombre de valeurs acceptable. Cette méthode est appelée
         * automatiquement par {@link #canDecodeLine} avec en argument le nombre de valeurs
         * dans une des premières lignes trouvées dans la source. Cette indication n'est
         * qu'approximative et il est correct de retourner {@link Boolean#FALSE} de façon
         * conservative.
         */
        Boolean isValueCountAcceptable(final int count) {
            return count<=10 ? Boolean.TRUE : Boolean.FALSE;
        }
    }
}
