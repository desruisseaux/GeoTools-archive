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
package org.geotools.image.io;

// JS2E dependencies
import java.io.DataInput;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;
import java.net.URLDecoder;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.logging.Logger;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

// Geotools dependencies
import org.geotools.resources.i18n.Errors;
import static org.geotools.resources.i18n.ErrorKeys.*;


/**
 * Classe de base des d�codeurs d'images n�cessitant une source {@link File} plut�t que
 * {@link ImageInputStream}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public abstract class FileBasedReader extends ImageReader {
    /**
     * Le journal � utiliser pour les �v�nements de ce paquet.
     */
    protected static final Logger LOGGER = Logger.getLogger("org.geotools.netCDF.io");

    /**
     * Types d'images acceptables pour ce d�codeur. Ne sera construit que la premi�re fois o�
     * cette information sera demand�e.
     */
    private transient Set<ImageTypeSpecifier> imageTypes;

    /**
     * Le fichier � lire. Identique � {@link #input} si ce dernier �tait d�j� un objet
     * {@link File}, ou sera un fichier temporaire sinon.
     */
    private transient File inputFile;

    /**
     * {@code true} si {@link #inputFile} est un fichier temporaire.
     */
    private transient boolean isTemporary;

    /** 
     * Construit un nouveau d�codeur.
     *
     * @param spi Une description du service fournit par ce d�codeur.
     */
    public FileBasedReader(final FileBasedReaderSpi spi) {
        super(spi);
        if (spi == null) {
            throw new IllegalArgumentException(Errors.format(NULL_ARGUMENT_$1, "spi"));
        }
    }

    /**
     * Sp�cifie la source des donn�es � utiliser en entr�e. Cette source doit �tre un objet de
     * type {@link File} ou {@link URL}.
     */
    @Override
    public void setInput(final Object input, final boolean seekForwardOnly, final boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if (inputFile != null) {
            if (isTemporary) {
                inputFile.delete();
            }
            inputFile = null;
        }
        isTemporary = false;
    }

    /**
     * Retourne le chemin vers le fichier. Si la source est un URL, alors le contenu
     * sera copi� vers un fichier temporaire afin de pouvoir l'ouvrir comme un fichier.
     */
    protected final File getInputFile() throws IOException {
        if (inputFile == null) {
            if (input instanceof File) {
                inputFile = (File) input;
            } else if (input instanceof URL) {
                final URL sourceURL = (URL) input;
                if (sourceURL.getProtocol().equalsIgnoreCase("file")) {
                    inputFile = new File(URLDecoder.decode(sourceURL.getFile(), "UTF-8"));
                } else {
                    copy(sourceURL.openStream());
                }
            } else if (input instanceof InputStream) {
                copy((InputStream) input);
            } else if (input instanceof ImageInputStream){
                final ImageInputStream in = (ImageInputStream) input;
                final OutputStream    out = createTemporaryFile();
                final byte[]       buffer = new byte[4096];
                int length; while ((length=in.read(buffer)) >= 0) {
                    out.write(buffer, 0, length);
                }
                out.close();
                in.close();
            } else {
                throw new IllegalStateException(input == null ? Errors.format(NO_IMAGE_INPUT) :
                          Errors.format(ILLEGAL_CLASS_$2, input.getClass(), File.class));
            }
        }
        return inputFile;
    }

    /**
     * Creates a temporary file and returns an output stream for writting in it.
     */
    private OutputStream createTemporaryFile() throws IOException {
        String suffix = "tmp";
        if (originatingProvider != null) {
            final String[] suffixes = originatingProvider.getFileSuffixes();
            if (suffixes != null && suffixes.length != 0) {
                // We assume that the first file suffix is the
                // most representative of this file format.
                suffix = suffixes[0];
            }
        }
        inputFile = File.createTempFile("Image", suffix);
        inputFile.deleteOnExit();
        isTemporary = true;
        return new FileOutputStream(inputFile);
    }

    /**
     * Copies the content of the specified input stream to the temporary file.
     * The stream is closed after the copy.
     */
    private void copy(final InputStream in) throws IOException {
        final OutputStream out = createTemporaryFile();
        final byte[] buffer = new byte[4096];
        int length; while ((length=in.read(buffer)) >= 0) {
            out.write(buffer, 0, length);
        }
        out.close();
        in.close();
    }

    /**
     * Retourne {@code true} si le fichier retourn� par {@link #getInputFile} est temporaire.
     */
    protected final boolean isTemporaryFile() {
        return isTemporary;
    }

    /**
     * Retourne le nombre d'images, qui est fix� � 1 dans le cas de ce d�codeur.
     */
    public int getNumImages(final boolean allowSearch) throws IOException {
        return 1;
    }

    /**
     * Retourne {@code true} pour indiquer que ce format supporte les acc�s al�atoires
     * de mani�re assez efficace.
     */
    @Override
    public boolean isRandomAccessEasy(final int imageIndex) throws IOException {
        return true;
    }

    /**
     * Retourne le type d'image que cr�era ce d�codeur. L'impl�mentation par d�faut extrait
     * cette information � partir de {@link FileBasedReaderSpi#getRawImageType}.
     */
    @Override
    public ImageTypeSpecifier getRawImageType(int imageIndex) throws IOException {
        return ((FileBasedReaderSpi) originatingProvider).getRawImageType();
    }

    /**
     * Retourne les types d'image support�es. L'impl�mentation par d�faut retourne un singleton
     * ne contenant que <code>{@linkplain #getRawImageType getRawImageType}(index)</code>.
     */
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(Errors.format(ILLEGAL_ARGUMENT_$2, "imageIndex", imageIndex));
        }
        if (imageTypes == null) {
            imageTypes = Collections.singleton(getRawImageType(imageIndex));
        }
        return imageTypes.iterator();
    }

    /**
     * Retourne les m�ta-donn�es associ�es � l'ensemble du fichier courant.
     */
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    /**
     * Retourne les m�ta-donn�es associ�es � une image pr�cise du fichier courant.
     */
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    /**
     * Lib�re toutes les ressources utilis�es par cet objet.
     */
    @Override
    public void dispose() {
        reset();
        imageTypes = null;
        super.dispose();
    }

    /**
     * Lib�re toutes les ressources utilis�es par cet objet.
     */
    @Override
    protected void finalize() {
        dispose();
    }
}
