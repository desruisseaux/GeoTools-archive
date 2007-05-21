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
package org.geotools.image.io.text;

// J2SE dependencies
import java.awt.image.DataBuffer;
import java.io.*; // Many imports, including some for javadoc only.
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Locale;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

// Geotools dependencies
import org.geotools.io.LineFormat;
import org.geotools.image.io.SimpleImageReader;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;


/**
 * Base class for text image decoders. "Text images" are usually ASCII files containing pixel
 * as geophysical values. This base class provides a convenient way to get {@link BufferedReader}
 * for reading lines.
 * <p>
 * {@code TextImageReader} accepts many input types, including {@link File}, {@link URL},
 * {@link Reader}, {@link InputStream} and {@link ImageInputStream}. The {@link Spi} provider
 * automatically advises those input types. The above cited {@code Spi} provided also provides
 * a convenient way to control the character encoding, with the {@link Spi#charset charset} field.
 * Developer can gain yet more control on character encoding by overriding the {@link #getCharset}
 * method.
 *
 * @since 2.4
 * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/coverage/src/main/java/org/geotools/image/io/TextImageReader.java $
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class TextImageReader extends SimpleImageReader {
    /**
     * Type des images les plus proches du format de l'image. Ce type
     * devrait être une des constantes {@link DataBuffer#TYPE_FLOAT},
     * {@link DataBuffer#TYPE_DOUBLE} ou {@link DataBuffer#TYPE_INT}.
     */
    private final int rawImageType;

    /**
     * {@link #input} as a reader, or {@code null} if none.
     *
     * @see #getReader
     */
    private BufferedReader reader;

    /**
     * Constructs a new image reader storing pixels as {@link DataBuffer#TYPE_FLOAT}.
     *
     * @param provider The {@link ImageReaderSpi} that is invoking this constructor, or
     *                 {@code null} if none.
     */
    protected TextImageReader(final ImageReaderSpi provider) {
        this(provider, DataBuffer.TYPE_FLOAT);
    }

    /**
     * Constructs a new image reader storing pixels in buffer of the specified type.
     *
     * @param provider The {@link ImageReaderSpi} that is invoking this constructor, or
     *                 {@code null} if none.
     * @param rawImageType The buffer type. It should be a constant from
     *        {@link DataBuffer}. Common types are {@link DataBuffer#TYPE_INT},
     *        {@link DataBuffer#TYPE_FLOAT} and {@link DataBuffer#TYPE_DOUBLE}.
     */
    protected TextImageReader(final ImageReaderSpi provider, final int rawImageType) {
        super(provider);
        this.rawImageType = rawImageType;
    }

    /**
     * Returns the data type which most closely represents the "raw"
     * internal data of the image. Default implementation returns the
     * {@code rawImageType} argument provided at construction time.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The data type ({@code TYPE_FLOAT} by default).
     * @throws IOException If an error occurs reading the format information
     *         from the input source.
     */
    //@Override
    public int getRawDataType(final int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return rawImageType;
    }

    /**
     * Returns the character set to uses for decoding the string from the input stream.
     * The default implementation returns the character set specified to the {@link Spi}
     * object given to this {@code TextImageReader} constructor. Subclasses may override
     * this method if they want to detect the character encoding in some other way.
     *
     * @param  input The input stream.
     * @return The character encoding, or {@code null} for the platform default encoding.
     * @throws IOException If reading from the input stream failed.
     *
     * @see Spi#charset
     */
    protected Charset getCharset(final InputStream input) throws IOException {
        return (originatingProvider instanceof Spi) ? ((Spi)originatingProvider).charset : null;
    }

    /**
     * Returns the line format to use for parsing every lines in the input stream. The default
     * implementation creates a new {@link LineFormat} instance using the locale specified by
     * {@link Spi#locale}. Subclasses should override this method if they want more control
     * on the parser to be created.
     *
     * @param  imageIndex the index of the image to be queried.
     * @throws IOException If reading from the input stream failed.
     *
     * @see Spi#locale
     */
    protected LineFormat getLineFormat(final int imageIndex) throws IOException {
        if (originatingProvider instanceof Spi) {
            final Locale locale = ((Spi) originatingProvider).locale;
            if (locale != null) {
                return new LineFormat(locale);
            }
        }
        return new LineFormat();
    }

    /**
     * Returns the pad value for missing data, or {@link Double#NaN} if none. The pad value will
     * applies to all columns except the one for {@link TextRecordImageReader#getColumnX x} and
     * {@link TextRecordImageReader#getColumnY y} values, if any.
     * <p>
     * The default implementation returns the pad value specified to the {@link Spi} object given
     * to this {@code TextImageReader} constructor. Subclasses may override this method if they
     * want to detect the pad value in some other way.
     *
     * @param  imageIndex the index of the image to be queried.
     * @throws IOException If reading from the input stream failed.
     *
     * @see Spi#padValue
     */
    protected double getPadValue(final int imageIndex) throws IOException {
        return (originatingProvider instanceof Spi) ? ((Spi) originatingProvider).padValue : Double.NaN;
    }

    /**
     * Returns the {@linkplain #input input} as an {@linkplain BufferedReader buffered reader}.
     * If the input is already a buffered reader, it is returned unchanged. Otherwise this method
     * creates a new {@linkplain LineNumberReader line number reader} from various input types
     * including {@link File}, {@link URL}, {@link URLConnection}, {@link Reader},
     * {@link InputStream} and {@link ImageInputStream}.
     * <p>
     * This method creates a new {@linkplain BufferedReader reader} only when first invoked.
     * All subsequent calls will returns the same instance. Consequently, the returned reader
     * should never be closed by the caller. It may be {@linkplain #close closed} automatically
     * when {@link #setInput setInput(...)}, {@link #reset() reset()} or {@link #dispose()
     * dispose()} methods are invoked.
     *
     * @return {@link #getInput} as a {@link BufferedReader}.
     * @throws IllegalStateException if the {@linkplain #input input} is not set.
     * @throws IOException If the input stream can't be created for an other reason.
     *
     * @see #getInput
     * @see #getInputStream
     */
    protected BufferedReader getReader() throws IllegalStateException, IOException {
        if (reader == null) {
            final Object input = getInput();
            if (input instanceof BufferedReader) {
                reader = (BufferedReader) input;
                closeOnReset = null; // We don't own the underlying reader, so don't close it.
            } else if (input instanceof Reader) {
                reader = new LineReader((Reader) input);
                closeOnReset = null; // We don't own the underlying reader, so don't close it.
            } else {
                final InputStream stream = getInputStream();
                reader = new LineReader(getInputStreamReader(stream));
                if (closeOnReset == stream) {
                    closeOnReset = reader;
                }
            }
        }
        return reader;
    }

    /**
     * Returns the specified {@link InputStream} as a {@link Reader}.
     */
    final Reader getInputStreamReader(final InputStream stream) throws IOException {
        final Charset charset = getCharset(stream);
        return (charset != null) ? new InputStreamReader(stream, charset) : new InputStreamReader(stream);
    }

    /**
     * Returns {@code true} if the specified line is a comment. This method is invoked automatically
     * during a {@link #read read} operation. The default implementation returns {@code true} if the
     * line is empty or if the first non-whitespace character is {@code '#'}, and {@code false}
     * otherwise. Override this method if comment lines should be determined in a different way.
     *
     * @param  line A line to be parsed.
     * @return {@code true} if the line is a comment and should be ignored, or {@code false} if it
     *         should be parsed.
     */
    protected boolean isComment(final String line) {
        final int length = line.length();
        for (int i=0; i<length; i++) {
            final char c = line.charAt(i);
            if (!Character.isSpaceChar(c)) {
                return (c == '#');
            }
        }
        return true;
    }

    /**
     * Retourne une approximation du nombre d'octets du flot occupés par les
     * images {@code fromImage} inclusivement jusqu'à {@code toImage}
     * exclusivement. L'implémentation par défaut calcule cette longueur en
     * supposant que toutes les images se divisent la longueur totale du flot
     * en parts égales.
     *
     * @param fromImage Index de la première image à prendre en compte.
     * @param   toImage Index suivant celui de la dernière image à prendre en
     *                  compte, ou -1 pour prendre en compte toutes les images
     *                  restantes jusqu'à la fin du flot.
     * @return Le nombre d'octets occupés par les images spécifiés, ou -1 si
     *         cette longueur n'a pas pu être calculée. Si le calcul précis de
     *         cette longueur serait prohibitif, cette méthode est autorisée à
     *         retourner une simple approximation ou même à retourner la longueur
     *         totale du flot.
     * @throws IOException si une erreur est survenue lors de la lecture du flot.
     */
    final long getStreamLength(final int fromImage, int toImage) throws IOException {
        long length = getStreamLength();
        if (length > 0) {
            final int numImages = getNumImages(false);
            if (numImages > 0) {
                if (toImage == -1) {
                    toImage = numImages;
                }
                if (fromImage<0 || fromImage>numImages) {
                    throw new IndexOutOfBoundsException(String.valueOf(fromImage));
                }
                if (toImage<0 || toImage>numImages) {
                    throw new IndexOutOfBoundsException(String.valueOf(  toImage));
                }
                if (fromImage > toImage) {
                    throw new IllegalArgumentException();
                }
                return length * (toImage-fromImage) / numImages;
            }
        }
        return length;
    }

    /**
     * Retourne la position du flot spécifié, ou {@code -1} si cette position est
     * inconnue. Note: la position retournée est <strong>approximative</strong>.
     * Elle est utile pour afficher un rapport des progrès, mais sans plus.
     *
     * @param  reader Flot dont on veut connaître la position.
     * @return Position approximative du flot, ou {@code -1}
     *         si cette position n'a pas pu être obtenue.
     * @throws IOException si l'opération a échouée.
     */
    static long getStreamPosition(final Reader reader) throws IOException {
        return (reader instanceof LineReader) ? ((LineReader) reader).getPosition() : -1;
    }

    /**
     * Returns a string representation of the current stream position. For example this method
     * may returns something like {@code "Line 14 in file HUV18204.asc"}. This method returns
     * {@code null} if the stream position is unknown.
     *
     * @param message An optional message to append to the stream position, or {@code null}
     *        if none.
     */
    protected String getPositionString(final String message) {
        final String file;
        final Object input = getInput();
        if (input instanceof File) {
            file = ((File) input).getName();
        } else if (input instanceof URL) {
            file = ((URL) input).getFile();
        } else {
            file = null;
        }
        final Integer line = (reader instanceof LineNumberReader) ?
                new Integer(((LineNumberReader) reader).getLineNumber()) : null;

        final Vocabulary resources = Vocabulary.getResources(null);
        final String position;
        if (file != null) {
            if (line != null) {
                position = resources.getString(VocabularyKeys.FILE_POSITION_$2, file, line);
            } else {
                position = resources.getString(VocabularyKeys.FILE_$1, file);
            }
        } else if (line != null) {
            position = resources.getString(VocabularyKeys.LINE_$1, line);
        } else {
            position = null;
        }
        if (position != null) {
            if (message != null) {
                return position + ": " + message;
            } else {
                return position;
            }
        } else {
            return message;
        }
    }

    /**
     * Closes the reader created by {@link #getReader()}. This method does nothing if
     * the reader is the {@linkplain #input input} instance given by the user rather
     * than a reader created by this class from a {@link File} or {@link URL} input.
     *
     * @see #closeOnReset
     */
    //@Override
    protected void close() throws IOException {
        reader = null;
        super.close();
    }




    /**
     * Service provider interface (SPI) for {@link TextImageReader}s. This
     * SPI provides a convenient way to control the {@link TextImageReader}
     * character encoding: the {@link #charset} field. For example, many
     * {@code Spi} subclasses will put the following line in their
     * constructor:
     *
     * <blockquote><pre>
     * {@link #charset} = Charset.forName("ISO-LATIN-1"); // ISO Latin Alphabet No. 1 (ISO-8859-1)
     * </pre></blockquote>
     *
     * @since 2.4
     * @source $URL: http://svn.geotools.org/geotools/trunk/gt/modules/library/coverage/src/main/java/org/geotools/image/io/TextImageReader.java $
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static abstract class Spi extends SimpleImageReader.Spi {
        /**
         * List of legal input types for {@link TextImageReader}.
         */
        private static final Class[] INPUT_TYPES = new Class[] {
            File.class,
            URL.class,
            URLConnection.class,
            Reader.class,
            InputStream.class,
            ImageInputStream.class,
            String.class  // To be interpreted as file path.
        };

        /**
         * Default list of file's extensions.
         */
        private static final String[] EXTENSIONS = new String[] {"txt","asc","dat"};

        /**
         * Character encoding, or {@code null} for the default. This field is initially
         * {@code null}. A value shall be set by subclasses decoding files using some
         * specific character encoding.
         *
         * @see TextImageReader#getCharset
         */
        protected Charset charset;

        /**
         * The locale for numbers or dates parsing. For example {@link Locale#US} means that
         * numbers are expected to use dot as decimal separator. This field is initially
         * {@code null}, which means that default locale should be used.
         *
         * @see TextImageReader#getLineFormat
         * @see TextRecordImageReader#parseLine
         */
        protected Locale locale;

        /**
         * The pad value, or {@link Double#NaN} if none. Every occurences of pixel value equals
         * to this pad value will be replaced by {@link Double#NaN} during read operation. Note
         * that this replacement doesn't apply to non-pixel values (for example <var>x</var>,
         * <var>y</var> coordinates in some file format).
         *
         * @see TextImageReader#getPadValue
         * @see TextRecordImageReader#parseLine
         */
        protected double padValue = Double.NaN;

        /**
         * Constructs a new SPI for {@link TextImageReader}. This constructor
         * initializes the following fields to default values:
         *
         * <ul>
         *   <li>Image format names ({@link #names}):
         *       An array of lenght 1 containing the {@code name} argument.
         *
         *   <li>MIME type ({@link #MIMETypes}):
         *       An array of length 1 containing the {@code mime} argument.
         *
         *   <li>File suffixes ({@link #suffixes}):
         *       "{@code .txt}", "{@code .asc}" et "{@code .dat}"
         *       (uppercase and lowercase).</li>
         *
         *   <li>Input types ({@link #inputTypes}):
         *       {@link File}, {@link URL}, {@link Reader}, {@link InputStream} et {@link ImageInputStream}.</li>
         * </ul>
         *
         * Others fields should be set by subclasses
         * (usually in their constructors).
         *
         * @param name Format name, or {@code null} to let {@link #names} unset.
         * @param mime MIME type, or {@code null} to let {@link #MIMETypes} unset.
         */
        public Spi(final String name, final String mime) {
            super(name, mime);
            suffixes   = EXTENSIONS;
            inputTypes = INPUT_TYPES;
        }

        /**
         * Returns {@code true} if the supplied source object appears to be of the format
         * supported by this reader. The default implementation tries to parse the first
         * few lines up to 1024 characters.
         *
         * @param  source The object (typically an {@link ImageInputStream}) to be decoded.
         * @return {@code true} if the source <em>seems</em> readable.
         * @throws IOException If an error occured during reading.
         */
        public boolean canDecodeInput(final Object source) throws IOException {
            return canDecodeInput(source, 1024);
        }

        /**
         * Returns {@code true} if the supplied source object appears to be of the format
         * supported by this reader. The default implementation tries to parse the first
         * few lines up to the specified number of characters.
         *
         * @param  source The object (typically an {@link ImageInputStream}) to be decoded.
         * @param  readAheadLimit Maximum number of characters to read. If this amount is reached
         *         but this method still unable to make a choice, then it conservatively returns
         *         {@code false}.
         * @return {@code true} if the source <em>seems</em> readable.
         * @throws IOException If an error occured during reading.
         */
        protected boolean canDecodeInput(final Object source, final int readAheadLimit)
                throws IOException
        {
            final TestReader test = new TestReader(this);
            test.setInput(source);
            final boolean result = test.canDecode(readAheadLimit);
            test.close();
            return result;
        }

        /**
         * Returns {@code true} if the content of the first few rows seems valid, or {@code false}
         * otherwise. The number of rows depends on the row length and the {@code readAheadLimit}
         * argument given to {@link #canDecodeInput(Object,int) canDecodeInput}.
         * <p>
         * The default implementation returns {@code true} if there is at least one row
         * and every row have the same number of columns.
         */
        protected boolean isValidContent(final double[][] rows) {
            if (rows.length == 0) {
                return false;
            }
            final int length = rows[0].length;
            for (int i=1; i<rows.length; i++) {
                if (rows[i].length != length) {
                    return false;
                }
            }
            return isValidColumnCount(length);
        }

        /**
         * Returns {@code true} if the specified row length is valid. If unsure, this methods
         * may conservatively returns {@code false}. The default implementation always returns
         * {@code true}.
         */
        boolean isValidColumnCount(final int count) {
            return true;
        }
    }
}
