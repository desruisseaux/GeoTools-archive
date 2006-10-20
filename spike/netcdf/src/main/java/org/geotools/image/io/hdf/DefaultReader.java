/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Institut de Recherche pour le D�veloppement
 *    (C) 2006, Geomatys
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
package org.geotools.image.io.hdf;

// JS2E dependencies
import java.util.List;
import java.lang.reflect.Array;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;

// HDF dependencies
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.Dataset; 
import ncsa.hdf.object.Group;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import static org.geotools.resources.i18n.ErrorKeys.*;

// Sicade dependencies
import org.geotools.image.io.FileBasedReader;
import org.geotools.image.io.FileBasedReaderSpi;


/**
 * Impl�mentation par d�faut des d�codeurs d'images au format HDF. Dans la plupart des
 * cas, il ne sera pas n�cessaire de cr�er des classes d�riv�es. Des classes d�riv�es
 * de {@link AbstractReaderSpi} suffisent.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
final class DefaultReader extends FileBasedReader {
    /**
     * Le format des donn�es � lire.
     */
    private FileFormat format;

    /**
     * L'ensemble des donn�es du ph�nom�ne �tudi�.
     * <p>
     * <b>Note:</b> Ne pas appeler {@link Dataset#init} explicitement. C'est sens� �tre fait
     * automatiquement par la biblioth�que HDF. L'exp�rience montre qu'un appel explicite �
     * {@code init()} fonctionne sous Windows, mais cause un crash de la JVM sous Linux Gentoo.
     *
     * @see #prepareDataset
     */
    private Dataset dataset;

    /**
     * Les indicateurs de qualit� pour chaque donn�e, ou {@code null} s'il n'y en a pas.
     *
     * @see #prepareDataset
     */
    private Dataset qualityDataset;

    /**
     * Indique le nombre de bits sur lequel est cod�e l'information sans la qualit�.
     * La valeur par d�faut est 8.
     *
     * @see #setBitCount
     */
    private int dataBitCount = 8;

    /**
     * Indique le nombre de bits sur lequel est cod� la qualit�. La valeur par d�faut est 0,
     * c'est � dire une absence d'informations concernant la qualit� des donn�es.
     *
     * @see #setBitCount
     */
    private int qualityBitCount = 0;

    /** 
     * Construit un nouveau d�codeur HDF. Les classes d�riv�es devraient appeler {@link #setBitCount
     * setBitCount} apr�s la construction, ou au moins avant le premier appel de {@link #read read}.
     *
     * @param spi Une description du service fournit par ce d�codeur.
     */
    public DefaultReader(final AbstractReaderSpi spi) {
        super(spi);
    }

    /**
     * Sp�cifie le nombre de bits sur lesquels sont cod�s les donn�es. Si cette m�thode n'est
     * jamais appel�e, alors la valeur par d�faut est de 8 bits pour les donn�es et 0 bits pour
     * la qualit�.
     *
     * @param  data      Nombre de bits sur lequel est cod�e l'information sans la qualit�.
     * @param  quality   Nombre de bits sur lequel est cod� la qualit�.
     * @throws IllegalArgumentException si un des arguments sp�cifi� est n�gatif.
     */
    protected final void setBitCount(final int data, final int quality) throws IllegalArgumentException {
        if (data < 0 || data >= Integer.SIZE) {
            throw new IllegalArgumentException(Errors.format(ILLEGAL_ARGUMENT_$2, "data", data));
        }
        if (quality < 0 || data+quality >= Integer.SIZE) {
            throw new IllegalArgumentException(Errors.format(ILLEGAL_ARGUMENT_$2, "quality", quality));
        }
        dataBitCount    = data;
        qualityBitCount = quality;
    }

    /**
     * Sp�cifie la source des donn�es � utiliser en entr�e. Cette source doit �tre un objet de
     * type {@link File}.
     */
    @Override
    public void setInput(final Object input, final boolean seekForwardOnly, final boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        close(qualityDataset); qualityDataset = null;
        close(dataset);        dataset        = null;
    }

    /**
     * Retourne la largeur de l'image.
     */
    public int getWidth(final int imageIndex) throws IOException {
        prepareDataset(imageIndex, false);
        return dataset.getWidth();
    }

    /**
     * Retourne la hauteur de l'image.
     */
    public int getHeight(final int imageIndex) throws IOException {
        prepareDataset(imageIndex, false);
        return dataset.getHeight();
    }

    /**
     * Construit une image � partir des param�tre de lecture sp�cifi�s.
     *
     * @throws  IOException Si la lecture de l'image a �chou�e.
     */
    public BufferedImage read(final int imageIndex, final ImageReadParam param) throws IOException {
        clearAbortRequest();
        checkReadParamBandSettings(param, 1, 1);
        prepareDataset(imageIndex, true);
        final int            width  = dataset.getWidth();
        final int            height = dataset.getHeight();
        final BufferedImage  image  = getDestination(param, getImageTypes(imageIndex), width, height);
        final WritableRaster raster = image.getRaster();
        final Rectangle   srcRegion = new Rectangle();
        final Rectangle  destRegion = new Rectangle();
        final int strideX, strideY;
        if (param != null) {
            strideX = param.getSourceXSubsampling();
            strideY = param.getSourceYSubsampling();
        } else {
            strideX = 1;
            strideY = 1;
        }
        computeRegions(param, width, height, image, srcRegion, destRegion);
        processImageStarted(imageIndex);
        /*
         * Ici on fixe les parmat�res tels qu'au prochain appel de dataset.getData(),
         * l'API HDF va nous permettre de r�cup�rer les sizes[0] x sizes[1] valeurs enti�res 
         * de la sous r�gion dont le point haut gauche a pour coordonn�es start[0], start[1] 
         * et dont les largeur et hauteur sont respectivement size[0], size[1]
         * 
         * ATTENTION - ATTENTION : Il faut prendre la convention matricielle ligne colonne 
         *                         c'est � dire start[0] = y et start[1] = x !!!!!
         */
        final long[] start  = dataset.getStartDims();
        final long[] stride = dataset.getStride();
        final long[] sizes  = dataset.getSelectedDims();
        start [0] = srcRegion.y;
        start [1] = srcRegion.x;
        sizes [0] = srcRegion.height;
        sizes [1] = srcRegion.width;
        stride[0] = strideY;
        stride[1] = strideX;
        final Object data, quality;
        try {
            data = dataset.read();
            quality = (qualityDataset != null) ? qualityDataset.read() : null;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw hdfFailure(e);
        }
        final int dataMask          = getMask(dataset);
        final int qualityMask       = getMask(qualityDataset);
        final int targetDataMask    = ~((1 << dataBitCount) - 1);
        final int targetQualityMask = ~((1 << qualityBitCount) - 1);
        /*
         * Maintenant que l'ensemble des donn�es ont �t� obtenues, pr�pare la copie vers le raster.
         * Si les entiers sont non-sign�s, on utilisera un masque pour supprimer le signe. Note: on
         * n'utilise pas Dataset.convertFromUnsignedC(Object) afin d'�viter la cr�ation d'un tableau
         * temporaire qui peut �tre volumineux.
         */
        int index = 0;
        final float toPercent = 100f / Array.getLength(data);
        final int xmax = destRegion.x + destRegion.width;
        final int ymax = destRegion.y + destRegion.height;
        for (int y=destRegion.y; y<ymax; y++) {
            for (int x=destRegion.x; x<xmax; x++) {
                int value = Array.getInt(data, index) & dataMask;
                if (quality != null) {
                    if ((value & targetDataMask) != 0) {
                        throw new IIOException("La valeur " + value + " � la position (" + x + ',' + y +
                                               ") n'est pas un entier non-sign� sur " + dataBitCount + " bits.");
                    }
                    final int q = Array.getInt(quality, index) & qualityMask;
                    if ((q & targetQualityMask) != 0) {
                        throw new IIOException("L'indicateur de qualit� " + q + " � la position (" + x + ',' + y +
                                               ") n'est pas un entier non-sign� sur " + qualityBitCount + " bits.");
                    }
                    value |= (q << dataBitCount);
                }
                raster.setSample(x, y, 0, value);
                index++;
            }
            processImageProgress(index * toPercent);
            if (abortRequested()) {
                processReadAborted();
                return image;
            }
        }
        processImageComplete();
        assert index == Array.getLength(data) : index;
        return image;
    }

    /**
     * V�rifie que les donn�es ont bien �t� charg�e dans {@link #dataset} pour l'image sp�cifi�e.
     * Si les donn�es ont d�j� �t� charg�e lors d'un appel pr�c�dent, alors cette m�thode ne fait
     * rien.
     * <p>
     * Certaines donn�es enregistrent dans un fichier s�par� des indicateurs de qualit� pour chaque
     * pixel. Si ces informations existent, alors le param�tre {@code includeQuality} indique s'il
     * faut proc�der � leur chargement ou pas.
     * 
     * @param   imageIndex L'index de l'image � traiter.
     * @param   {@code true} pour proc�der aussi au chargement des indicateurs de qualit�.
     * @throws  IndexOutOfBoundsException Si {@code indexImage} est diff�rent de 0,
     *          car on consid�re qu'il n'y a qu'une image par fichier HDF.
     * @throws  IllegalStateException Si le champ {@link #input} n'a pas �t� initialis� via
     *          {@link #setInput setInput(...)}.
     * @throws  IIOException Si le fichier HDF ne semble pas correct.
     * @throws  IOException Si la lecture a �chou�e pour une autre raison.
     */
    private void prepareDataset(final int imageIndex, final boolean includeQuality) throws IOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(Errors.format(ILLEGAL_ARGUMENT_$2, "imageIndex", imageIndex));
        }
        if (dataset == null) {
            final File inputFile = getInputFile();
            dataset = open(inputFile.getPath());
            if (dataset == null) {
                throw new IOException("Aucune donn�e n'a �t� trouv�e dans le fichier HDF.");
            }
            if (originatingProvider instanceof AbstractReaderSpi) {
                checkName(inputFile, dataset, ((AbstractReaderSpi) originatingProvider).dataName);
            }
        }
        /*
         * Proc�de au chargement des indicateurs de qualit�s, s'ils ont �t� demand�s.
         * Le nom de l'objet 'Dataset' obtenu sera compar� avec celui qui �tait attendu,
         * et les indicateurs de qualit�s rejet�s s'ils ne correspondent pas.
         */
        if (includeQuality && qualityDataset==null && originatingProvider instanceof AbstractReaderSpi) {
            final File inputFile = getInputFile();
            final File qualityFile;
            if (isTemporaryFile()) {
                qualityFile = null; // TODO: prendre en compte les indicateurs de qualit�.
            } else {
                qualityFile = ((AbstractReaderSpi) originatingProvider).getQualityFile(inputFile);
            }
            if (qualityFile != null) {
                if (!qualityFile.isFile()) {
                    processWarningOccurred("Le fichier d'indicateurs de qualit� \"" +
                                           qualityFile.getName() + "\" n'a pas �t� trouv�.");
                } else {
                    qualityDataset = open(qualityFile.getPath());
                    if (originatingProvider instanceof AbstractReaderSpi) {
                        checkName(qualityFile, qualityDataset, ((AbstractReaderSpi) originatingProvider).qualityName);
                    }
                }
            }
        }
    }

    /**
     * Renvoie un objet {@link Dataset} � partir d'un objet {@link HObject}.
     *
     * @param   hobject L'objet {@link HObject} � parcourir.
     * @return  L'objet {@link Dataset} contenu dans l'objet {@link HObject}, ou {@code null}
     *          si aucun n'a �t� trouv�.
     * @throws  IIOException si une erreur est survenu lors de l'examen de {@code object}.
     */
    private static Dataset getDataset(final HObject object) throws IIOException {
        if (object instanceof Dataset) {
            return (Dataset) object;
        }
        if (object instanceof Group) try {
            @SuppressWarnings("unchecked")
            final List<HObject> members = ((Group) object).getMemberList();
            for (final HObject member : members) {
                final Dataset candidate = getDataset(member);
                if (candidate != null) {
                    return candidate;
                }
            }
        } catch (ClassCastException exception) {
            throw hdfFailure(exception);
        }
        return null;
    }

    /**
     * Retourne le masque � appliquer sur les donn�es lues pour �viter que le Java ne transforme
     * certains entiers non-sign�s en valeurs n�gatives.
     */
    private static int getMask(final Dataset dataset) throws IIOException {
        if (dataset != null) {
            final Datatype type = dataset.getDatatype();
            final int      size = type.getDatatypeSize() * Byte.SIZE;
            if (size<1 || size>Integer.SIZE) {
                throw new IIOException("Les entiers sur " + size + " bits ne sont pas support�es.");
            }
            if (type.isUnsigned()) {
                if (size == Integer.SIZE) {
                    throw new IIOException("Les entiers non-sign�s sur " + size + " bits ne sont pas support�es.");
                }
                return (1 << size) - 1;
            }
        }
        return ~0;
    }

    /**
     * V�rifie que le nom du {@code dataset} est bien celui que l'on attend ({@code expected}).
     * 
     * @param   file         Le nom du fichier HDF trait�.
     * @param   dataset      Les donn�es obtenues.
     * @param   datasetName  Le nom du dataset attendu. Actuellement, pour la temp�rature ce nom est 
     *                       {@code sst} et pour la chlorophylle le nom est {@code l3m_data}.
     * @throws  IIOException Si le nom du dataset n'est pas celui que l'on attendait.
     */
    private static void checkName(final File file, final Dataset dataset, String expected) throws IIOException {
        if (dataset != null && expected != null) {
            final String name = dataset.getName().trim();
            if (name != null) {
                expected = expected.trim();
                if (!name.equalsIgnoreCase(expected)) {
                    throw new IIOException("Le fichier \"" + file.getName() +
                            "\" ne semble par contenir les donn�es attendues. " +
                            "Ses donn�es portent le nom \"" + name +
                            "\" alors que l'on attendait \"" + expected + "\".");
                }
            }
        }
    }

    /**
     * Lance une exception un peu plus explicite lorsqu'une erreur est survenue
     * lors de la lecture d'un fichier HDF.
     */
    private static IIOException hdfFailure(final Exception e) {
        return new IIOException("Echec lors de la lecture du fichier HDF", e);
    }

    /**
     * Ouvre le fichier sp�cifi� et retourne les donn�es qu'il contient, ou {@code null} si aucune
     * donn�e n'a �t� trouv�e.
     *
     * @throws IOException si l'ouverture et la lecture du fichier a �chou�.
     */
    private Dataset open(final String filename) throws IOException {
        if (format == null) {
            format = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF4);
            if (format == null) {
                throw new IIOException("Format non-disponible.");
            }
        }
        final FileFormat file;
        final HObject object;
        try {
            file = format.open(filename, FileFormat.READ);
            object = file.get("/");
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw hdfFailure(e);
        }
        final Dataset data = getDataset(object);
        assert data.getFileFormat() == file;
        return data;
    }

    /**
     * Ferme le fichier associ� au dataset sp�cifi�.
     */
    private static void close(final Dataset dataset) {
        if (dataset != null) {
            final FileFormat format = dataset.getFileFormat();
            if (format != null) try {
                format.close();
            } catch (Exception e) {
                /*
                 * Ignore cette erreur (except� pour l'�criture dans le journal), �tant
                 * donn� que l'on ferme ce fichier dans l'intention d'en ouvrir un autre.
                 */
                Utilities.unexpectedException("net.sicade.image.io.hdf", "DefaultReader", "setInput", e);
            }
        }
    }

    /**
     * Lib�re toutes les ressources utilis�es par cet objet.
     */
    @Override
    public void dispose() {
        super.dispose();
        format = null;
    }
}
