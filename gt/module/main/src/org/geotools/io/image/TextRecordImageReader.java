/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le D�veloppement
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
 *     FRANCE: Surveillance de l'Environnement Assist�e par Satellite
 *             Institut de Recherche pour le D�veloppement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.io.image;

// Images
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

import org.geotools.io.LineFormat;
import org.geotools.resources.XArray;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;


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
 * <br><br>
 * By default, <code>TextRecordImageReader</code> assume that <var>x</var> and
 * <var>y</var> coordinates appear in column #0 and 1 respectively. It also assumes
 * that numeric values are encoded using current defaults {@link java.nio.charset.Charset}
 * and {@link java.util.Locale}, and that there is no pad value. The easiest way to change
 * the default setting is to create a {@link Spi} subclass. There is no need to subclass
 * <code>TextRecordImageReader</code>, unless you want more control on the decoding process.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class TextRecordImageReader extends TextImageReader {
    /**
     * Petit facteur de tol�rance servant � tenir
     * compte des erreurs d'arrondissement.
     */
    private static final float EPS = 1E-5f;
    
    /**
     * Intervalle (en nombre d'octets) entre les rapports de progr�s.
     */
    private static final int PROGRESS_INTERVAL = 4096;
    
    /**
     * Lorsque la lecture se fait par-dessus une image {@link BufferedReader} existante,
     * indique s'il faut effacer la r�gion dans laquelle sera plac�e l'image avant de la
     * lire. La valeur <code>false</code> permettra de conserver les anciens pixels dans
     * les r�gions ou le fichier ne d�finit pas de nouvelles valeurs.
     */
    private static final boolean CLEAR = true;
    
    /**
     * Num�ro de colonne des <var>x</var>, compt� � partir de 0.
     * Ce champ n'existe que pour des raisons de performances; il
     * n'est utilis� que par {@link #parseLine} pendant la lecture
     * d'une image. Dans tous les autres cas, on utilisera plut�t
     * {@link #getColumnX}.
     */
    private transient int xColumn = 0;
    
    /**
     * Num�ro de colonne des <var>y</var>, compt� � partir de 0.
     * Ce champ n'existe que pour des raisons de performances; il
     * n'est utilis� que par {@link #parseLine} pendant la lecture
     * d'une image. Dans tous les autres cas, on utilisera plut�t
     * {@link #getColumnY}.
     */
    private transient int yColumn = 1;
    
    /**
     * Valeur repr�sentant les donn�es manquantes,   ou {@link Double#NaN} s'il n'y en
     * a pas. Ce champ n'existe que pour des raisons de performances; il n'est utilis�
     * que par {@link #parseLine} pendant la lecture d'une image. Dans tous les autres
     * cas, on utilisera plut�t {@link #getPadValue}.
     */
    private transient double padValue = Double.NaN;
    
    /**
     * Objet � utiliser pour lire chacune des lignes de fichier. Ce champ n'existe que
     * pour des raisons de performances; il n'est utilis� que par {@link #parseLine}
     * pendant la lecture d'une image. Dans tous les autres cas, on utilisera plut�t
     * {@link #getLineFormat}.
     */
    private transient LineFormat lineFormat;
    
    /**
     * Donn�es des images, ou <code>null</code> si aucune lecture n'a encore �t�
     * faite. Chaque �l�ment contient les donn�es de l'image � l'index correspondant
     * (i.e. l'�l�ment <code>data[0]</code> contient les donn�es de l'image #0,
     * <code>data[1]</code> contient les donn�es de l'image #1, etc.). Des �l�ments
     * de ce tableau peuvent �tre nuls si les donn�es des images correspondantes
     * ne sont pas retenues apr�s chaque lecture (c'est-�-dire si
     * <code>{@link #seekForwardOnly}==true</code>).
     */
    private RecordList[] data;
    
    /**
     * Index de la prochaine image � lire. Cet index n'est pas n�cessairement
     * �gal � la longueur du tableau {@link #data}. Il peut �tre aussi bien
     * plus petit que plus grand.
     */
    private int nextImageIndex;
    
    /**
     * Nombre moyen de caract�res par donn�es (incluant les espaces et les codes
     * de fin de ligne). Cette information n'est qu'� titre indicative, mais son
     * exactitude peut aider � accelerer la lecture et rendre les rapport des
     * progr�s plus pr�cis. Elle sera automatiquement mise � jour en fonction
     * des lignes lues.
     */
    private float expectedDatumLength = 10.4f;
    
    /**
     * Construit un d�codeur d'images
     * de type {@link DataBuffer#TYPE_FLOAT}.
     *
     * @param provider Le fournisseur
     *        qui a construit ce d�codeur.
     */
    public TextRecordImageReader(final ImageReaderSpi provider) {
        super(provider, DataBuffer.TYPE_FLOAT);
    }
    
    /**
     * Construit un d�codeur d'images.
     *
     * @param provider Le fournisseur qui a construit ce d�codeur.
     * @param rawImageType Type par d�faut des images. Ce type devrait �tre une des
     *        constantes de {@link DataBuffer}, notamment {@link DataBuffer#TYPE_INT}
     *        ou {@link DataBuffer#TYPE_FLOAT}. Le type {@link DataBuffer#TYPE_DOUBLE}
     *        est accept� mais d�conseill�, �tant donn� que l'impl�mentation actuelle
     *        ne lira les donn�es qu'avec la pr�cision des types <code>float</code>.
     */
    public TextRecordImageReader(final ImageReaderSpi provider, final int rawImageType) {
        super(provider, rawImageType);
        clear();
        if (rawImageType == DataBuffer.TYPE_DOUBLE) {
            Logger.getLogger("org.geotools.io.image").warning("Type double is deprecated.");
        }
    }
    
    /**
     * Returns the grid tolerance (epsilon) value.
     */
    private float getGridTolerance() {
        return (originatingProvider instanceof Spi) ? ((Spi)originatingProvider).gridTolerance : EPS;
    }
    
    /**
     * Retourne le num�ro de colonne des <var>x</var>, compt� � partir de 0.
     * L'impl�mentation par d�faut retourne le num�ro de colonne qui avait �t�
     * sp�cifi� dans l'objet {@link Spi} qui a cr�� ce d�codeur. Les classes
     * d�riv�es peuvent red�finir cette m�thode pour d�terminer cette valeur
     * d'une fa�on plus �labor�e.
     *
     * @param  imageIndex Index de l'image � lire.
     * @throws IOException si l'op�ration n�cessitait une lecture du fichier (par exemple
     *         des informations inscrites dans un en-t�te) et que cette lecture a �chou�e.
     */
    public int getColumnX(final int imageIndex) throws IOException {
        return (originatingProvider instanceof Spi) ? ((Spi)originatingProvider).xColumn : 0;
    }
    
    /**
     * Retourne le num�ro de colonne des <var>y</var>, compt� � partir de 0.
     * L'impl�mentation par d�faut retourne le num�ro de colonne qui avait �t�
     * sp�cifi� dans l'objet {@link Spi} qui a cr�� ce d�codeur. Les classes
     * d�riv�es peuvent red�finir cette m�thode pour d�terminer cette valeur
     * d'une fa�on plus �labor�e.
     *
     * @param  imageIndex Index de l'image � lire.
     * @throws IOException si l'op�ration n�cessitait une lecture du fichier (par exemple
     *         des informations inscrites dans un en-t�te) et que cette lecture a �chou�e.
     */
    public int getColumnY(final int imageIndex) throws IOException {
        return (originatingProvider instanceof Spi) ? ((Spi)originatingProvider).yColumn : 1;
    }
    
    /**
     * Retourne le num�ro de colonne dans laquelle se trouvent les donn�es de la
     * bande sp�cifi�e. L'impl�mentation par d�faut retourne <code>band</code>+1
     * ou 2 si la bande est plus grand ou �gal � {@link #getColumnX} et/ou
     * {@link #getColumnY}. Cette impl�mentation devrait convenir pour des donn�es
     * se trouvant aussi bien avant qu'apr�s les colonnes <var>x</var>
     * et <var>y</var>, m�me si ces derni�res ne sont pas cons�cutives.
     *
     * @param  imageIndex Index de l'image � lire.
     * @param  band Bande de l'image � lire.
     * @return Num�ro de colonne des donn�es de l'image.
     * @throws IOException si l'op�ration n�cessitait une lecture du fichier (par exemple
     *         des informations inscrites dans un en-t�te) et que cette lecture a �chou�e.
     */
    private int getColumn(final int imageIndex, int band) throws IOException {
        final int xColumn = getColumnX(imageIndex);
        final int yColumn = getColumnY(imageIndex);
        if (band >= Math.min(xColumn, yColumn)) band++;
        if (band >= Math.max(xColumn, yColumn)) band++;
        return band;
    }
    
    /**
     * Sp�cifie le flot � utiliser en entr�. Ce flot peut �tre un objet des
     * objets suivants (en ordre de pr�f�rence): {@link java.io.File},
     * {@link java.net.URL} ou {@link java.io.BufferedReader}.
     *
     * Les flots de type {@link java.io.Reader}, {@link java.io.InputStream} et
     * {@link javax.imageio.stream.ImageInputStream} sont aussi accept�s, mais
     * moins conseill�s.
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
     * @throws IOException if an error occurs reading the information from the
     *         input source.
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
     * @throws IOException If an error occurs reading the width information from
     *         the input source.
     */
    public int getWidth(final int imageIndex) throws IOException {
        return getRecords(imageIndex).getPointCount(getColumnX(imageIndex), getGridTolerance());
    }
    
    /**
     * Returns the height in pixels of the given image within the input source.
     *
     * @param  imageIndex the index of the image to be queried.
     * @return Image height.
     * @throws IOException If an error occurs reading the height information
     *         from the input source.
     */
    public int getHeight(final int imageIndex) throws IOException {
        return getRecords(imageIndex).getPointCount(getColumnY(imageIndex), getGridTolerance());
    }
    
    /**
     * Returns the smallest bounding box containing the full image in user coordinates.
     * The default implementation search for minimum and maximum values in <var>x</var>
     * and <var>y</var> columns (as returned by {link #getColumnX} and {link #getColumnY})
     * and returns a rectangle containing <code>(xmin-dx/2,&nbsp;ymin-dy/2)</code>) and
     * <code>(xmax+dx/2,&nbsp;ymax+dy/2)</code>) points, where <var>dx</var> and
     * <var>dy</var> are grid cell width and height (i.e. the smallest interval between
     * <var>x</var> and <var>y</var> values).
     *
     * @param  imageIndex the index of the image to be queried.
     * @return Image bounds in user coordinates.
     * @throws IOException If an error occurs reading the width information from
     *         the input source.
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
     * Returns an {@link AffineTransform} for transforming pixel coordinates
     * to logical coordinates. Pixel coordinates are usually integer values
     * with (0,0) at the image's upper-left corner, while logical coordinates
     * are floating point values at the pixel's upper-left corner. The later
     * is consistent with <a href="http://java.sun.com/products/java-media/jai/">Java
     * Advanced Imaging</a> convention. In order to get logical values at the pixel
     * center, a translation must be apply once as below:
     *
     * <blockquote><pre>
     * AffineTransform tr = getTransform(imageIndex);
     * tr.translate(0.5, 0.5);
     * </pre></blockquote>
     *
     * The default implementation compute the affine transform from
     * {@link #getLogicalBounds}.
     *
     * @param  imageIndex The 0-based image index.
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
     * Retourne la valeur minimale et maximale m�moris�e dans une bande de l'image.
     *
     * @param  imageIndex Index de l'image dont on veut conna�tre la plage de valeurs.
     * @param  band Bande pour laquelle on veut la valeur minimale. Les num�ros de
     *         bandes commencent � 0  et sont ind�pendents des valeurs qui peuvent
     *         avoir �t� sp�cifi�es � {@link ImageReadParam#setSourceBands}.
     * @return Plage de valeurs trouv�e dans l'image et la bande sp�cifi�e.
     * @throws IOException si l'op�ration a �chou�e � cause d'une erreur d'entr�s/sorties.
     */
    public Range getExpectedRange(final int imageIndex, final int band) throws IOException {
        final int         column = getColumn(imageIndex, band);
        final RecordList records = getRecords(imageIndex);
        return new Range(Double.class, new Double(records.getMinimum(column)),
                                       new Double(records.getMaximum(column)));
    }
    
    /**
     * Convertit une ligne en valeurs num�riques. Cette m�thode est appel�e automatiquement
     * lors de la lecture de chaque ligne, avec en argument la ligne lue (<code>line</code>)
     * et le buffer dans lequel placer les valeurs num�riques (<code>values</code>).
     *
     * L'impl�mentation par d�faut d�code la ligne en exigeant qu'il y ait autant de nombres
     * que la longueur du tableau <code>values</code>.  Elle remplace ensuite les occurences
     * de <code>padValue</code> par {@link Double#NaN}  dans toutes les colonnes sauf celles
     * des coordonn�es <var>x</var> et <var>y</var>.
     *
     * @param line   Ligne � d�coder.
     * @param values Derni�res valeurs � avoir �t� lues, ou <code>null</code> si cette ligne
     *               est la premi�re � �tre d�cod�e. Ce buffer peut �tre r�utilis� en �crasant
     *               les anciennes valeurs par les nouvelles valeurs de la ligne <code>line</code>.
     * @return Les valeurs lues, ou <code>null</code> si la fin de l'image a �t� atteinte. Le
     *         tableau retourn� sera habituellement le m�me que <code>values</code>, mais pas
     *         obligatoirement. Par convention, un tableau de longueur 0 signifie que la ligne
     *         ne contient aucune donn�e et doit �tre ignor�e.
     * @throws ParseException si une erreur est survenue lors du d�codage de la ligne.
     */
    protected double[] parseLine(final String line, double[] values) throws ParseException {
        if (line==null) return null;
        if (lineFormat.setLine(line)==0) {
            return new double[0];
        }
        values=lineFormat.getValues(values);
        for (int i=0; i<values.length; i++) {
            if (i!=xColumn && i!=yColumn && values[i]==padValue) {
                values[i]=Double.NaN;
            }
        }
        return values;
    }
    
    /**
     * Retourne les donn�es de l'image � l'index sp�cifi�. Si cette image avait d�j� �t� lue, ses
     * donn�es seront retourn�es imm�diatement.  Sinon, cette image sera lue ainsi que toutes les
     * images qui pr�c�dent <code>imageIndex</code> et qui n'avaient pas encore �t� lues. Que ces
     * images pr�c�dentes soient m�moris�es ou oubli�es d�pend de {@link #seekForwardOnly}.
     *
     * @param  imageIndex Index de l'image � lire.
     * @return Les donn�es de l'image. Cette m�thode ne retourne jamais <code>null</code>.
     * @throws IOException si une erreur est survenue lors de la lecture du flot,
     *         ou si des nombres n'�taient pas correctement format�s dans le flot.
     * @throws IndexOutOfBoundsException si l'index sp�cifi� est en dehors des
     *         limites permises ou si aucune image n'a �t� conserv�e � cet index.
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
            for (;nextImageIndex<=imageIndex; nextImageIndex++) {
                /*
                 * R�duit la consommation de m�moire des images pr�c�dentes. On ne r�duit
                 * pas celle de l'image courante,  puisque la plupart du temps le tableau
                 * sera bient�t d�truit de toute fa�on.
                 */
                if (seekForwardOnly) {
                    minIndex=nextImageIndex;
                }
                if (nextImageIndex!=0 && data!=null) {
                    final RecordList records = data[nextImageIndex-1];
                    if (records!=null) {
                        if (seekForwardOnly) {
                            data[nextImageIndex-1]=null;
                        } else {
                            records.trimToSize();
                        }
                    }
                }
                /*
                 * Proc�de � la lecture de chacune des lignes de donn�es. Que ces lignes
                 * soient m�moris�es ou pas d�pend de l'image que l'on est en train de
                 * d�coder ainsi que de la valeur de {@link #seekForwardOnly}.
                 */
                double[]    values = null;
                RecordList records = null;
                final boolean  keep = (nextImageIndex==imageIndex) || !seekForwardOnly;
                // Initialise temporary fields used by 'parseLine'.
                this.xColumn    = getColumnX   (nextImageIndex);
                this.yColumn    = getColumnY   (nextImageIndex);
                this.padValue   = getPadValue  (nextImageIndex);
                this.lineFormat = getLineFormat(nextImageIndex);
                try {
                    String line;
                    while ((line=reader.readLine())!=null) {
                        values = parseLine(line, values);
                        if (values  ==  null) break;
                        if (values.length==0) continue;
                        if (keep) {
                            if (records==null) {
                                final int expectedLineCount = Math.max(8, Math.min(65536, Math.round(length / (expectedDatumLength*values.length))));
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
                 * Apr�s la lecture d'une image, v�rifie s'il y avait un nombre suffisant de lignes.
                 * Une exception sera lanc�e si l'image ne contenait pas au moins deux lignes. On
                 * ajustera ensuite le nombre moyens de caract�res par donn�es.
                 */
                if (records!=null) {
                    final int lineCount = records.getLineCount();
                    if (lineCount<2) {
                        throw new IIOException(getPositionString(Resources.format(ResourceKeys.ERROR_FILE_HAS_TOO_FEW_DATA)));
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
         * Une fois les lectures termin�es, retourne les donn�es de l'image
         * demand�e. Une exception sera lanc�e si ces donn�es n'ont pas �t�
         * conserv�es.
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
     * Reads the image indexed by <code>imageIndex</code> and returns it as a complete buffered image.
     *
     * @param  imageIndex the index of the image to be retrieved.
     * @param  param Parameters used to control the reading process, or <code>null</code>.
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
             * On convertit maintenant la coordonn�e (x,y) logique en coordonn�e pixel. Cette
             * coordonn�e pixel se r�f�re � l'image "source";  elle ne se r�f�re pas encore �
             * l'image destination. Elle doit obligatoirement �tre enti�re. Plus loin, nous
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
     * Retourne quelques types d'images qui pourront contenir les donn�es.
     * Le premier type retourn� sera celui qui se rapprochera le plus du
     * type des donn�es � lire.
     *
     * @param  imageIndex Index de l'image dont on veut les types.
     * @param  numBanfd Nombre de bandes.
     * @return It�rateur balayant les types de l'image.
     * @throws IndexOutOfBoundsException si <code>imageIndex</code> est invalide.
     * @throws IllegalStateException si aucune source n'a �t� sp�cifi�e avec {@link #setInput}.
     * @throws IIOException si l'op�ration a �chou� pour une autre raison.
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
     * Pr�vient qu'une coordonn�e est mauvaise. Cette m�thode est appel�e lors de la lecture
     * s'il a �t� d�tect� qu'une coordonn�e est en dehors des limites pr�vues, ou qu'elle ne
     * correspond pas � des coordonn�es pixels enti�res.
     */
    private void fireBadCoordinate(final float coordinate) {
        processWarningOccurred(getPositionString(Resources.format(
                ResourceKeys.ERROR_BAD_COORDINATE_$1, new Float(coordinate))));
    }
    
    /**
     * Supprime les donn�es de toutes les images
     * qui avait �t� conserv�es en m�moire.
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
     * Replace le d�codeur dans son �tat initial.
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
     *         {@link #vendorName vendorName} = "Institut de Recherche pour le D�veloppement";
     *         {@link #version    version}    = "1.0";
     *         {@link #locale     locale}     = Locale.US;
     *         {@link #charset    charset}    = Charset.forName("ISO-LATIN-1");
     *         {@link #padValue   padValue}   = 9999;
     *     }
     * }
     * </pre></blockquote>
     *
     * (Note: fields <code>vendorName</code> and <code>version</code> are only informatives).
     * There is no need to override any method in this example. However, developers
     * can gain more control by creating subclasses of {@link TextRecordImageReader}
     * <strong>and</strong> <code>Spi</code> and overriding some of their methods.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    public static class Spi extends TextImageReader.Spi {
        /**
         * Num�ro de colonne des <var>x</var>, compt� � partir de 0.
         * Par d�faut, on suppose que les <var>x</var> se trouvent
         * dans la premi�re colonne (colonne #0).
         *
         * @see TextRecordImageReader#getColumnX
         * @see TextRecordImageReader#parseLine
         */
        final int xColumn;
        
        /**
         * Num�ro de colonne des <var>y</var>, compt� � partir de 0.
         * Par d�faut, on suppose que les <var>y</var> se trouvent
         * dans la deuxi�me colonne (colonne #1).
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
         * a point depart from more than <code>gridTolerance<code> percent of
         * cell's width or height, an exception is thrown.
         * <br><br>
         * <code>gridTolerance</code> should be a small number like <code>1E-5f</code>
         * or <code>1E-3f</code>. The later is more tolerant than the former.
         */
        protected float gridTolerance = EPS;
        
        /**
         * Construct a new SPI with name "gridded records" and MIME type "text/x-grid".
         */
        public Spi() {
            this("gridded records", "text/x-grid");
        }
        
        /**
         * Construct a new SPI for {@link TextRecordImageReader}. <var>x</var> and
         * <var>y</var> columns are assumed to be in column #0 and 1 respectively.
         * Others parameters are initialized as in superclass constructor.
         *
         * @param name Format name, or <code>null</code> to let {@link #names} unset.
         * @param mime MIME type, or <code>null</code> to let {@link #MIMETypes} unset.
         */
        public Spi(final String name, final String mime) {
            this(name, mime, 0, 1);
        }
        
        /**
         * Construct a new SPI for {@link TextRecordImageReader}.
         *
         * @param name Format name, or <code>null</code> to let {@link #names} unset.
         * @param mime MIME type, or <code>null</code> to let {@link #MIMETypes} unset.
         * @param xColumn 0-based column number for <var>x</var> values.
         * @param yColumn 0-based column number for <var>y</var> values.
         */
        public Spi(final String name, final String mime, final int xColumn, final int yColumn) {
            super(name, mime);
            this.xColumn = xColumn;
            this.yColumn = yColumn;
            if (xColumn < 0) {
                throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_NEGATIVE_COLUMN_$2, "x", new Integer(xColumn)));
            }
            if (yColumn < 0) {
                throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_NEGATIVE_COLUMN_$2, "y", new Integer(yColumn)));
            }
            pluginClassName = "org.geotools.io.image.TextRecordImageReader";
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
            return Resources.getResources(locale).getString(ResourceKeys.CODEC_GRID);
        }
        
        /**
         * V�rifie si la ligne sp�cifi�e peut �tre d�cod�e.
         *
         * @param  line Une des premi�res lignes du flot � lire.
         * @return {@link Boolean#TRUE} si la ligne peut �tre d�cod�e, {@link Boolean#FALSE}
         *         si elle ne peut pas �tre d�cod�e ou <code>null</code> si on ne sait pas.
         *         Dans ce dernier cas, cette m�thode sera appel�e une nouvelle fois avec la
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
         * V�rifie si la ligne a un nombre de valeurs acceptable. Cette m�thode est appel�e
         * automatiquement par {@link #canDecodeLine} avec en argument le nombre de valeurs
         * dans une des premi�res lignes trouv�es dans la source. Cette indication n'est
         * qu'approximative et il est correct de retourner {@link Boolean#FALSE} de fa�on
         * conservative.
         */
        Boolean isValueCountAcceptable(final int count) {
            return count<=10 ? Boolean.TRUE : Boolean.FALSE;
        }
    }
}
