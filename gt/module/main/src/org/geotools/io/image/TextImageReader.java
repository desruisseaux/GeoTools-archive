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

// Input/output
import java.awt.image.DataBuffer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Locale;

import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.geotools.io.LineFormat;
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.gcs.Resources;


/**
 * Base class for text image decoders. "Text images" are usually ASCII files
 * containing pixel values (often geophysical values, like sea level anomalies).
 * This base class provides a convenient way to get {@link BufferedReader} for
 * reading lines.
 * <br><br>
 * <code>TextImageReader</code> accepts many input types, including {@link File},
 * {@link URL}, {@link Reader}, {@link InputStream} and {@link ImageInputStream}.
 * The {@link Spi} provider automatically advises those input types. The above
 * cited <code>Spi</code> provided also provides a convenient way to control the
 * character encoding, with the {@link Spi#charset charset} field. Developer can
 * gain yet more control on character encoding by overriding the {@link #getCharset}
 * method.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public abstract class TextImageReader extends SimpleImageReader {
    /**
     * Type des images les plus proches du format de l'image. Ce type
     * devrait �tre une des constantes {@link DataBuffer#TYPE_FLOAT},
     * {@link DataBuffer#TYPE_DOUBLE} ou {@link DataBuffer#TYPE_INT}.
     */
    private final int rawImageType;
    
    /**
     * Flot � utiliser pour lire les donn�es. Ce flot sera construit �
     * partir de {@link #input} la premi�re fois qu'il sera demand�.
     */
    private BufferedReader reader;
    
    /**
     * Construct a new image reader storing pixels
     * as {@link DataBuffer#TYPE_FLOAT}.
     *
     * @param provider the {@link ImageReaderSpi} that is
     *                 invoking this constructor, or null.
     */
    protected TextImageReader(final ImageReaderSpi provider) {
        this(provider, DataBuffer.TYPE_FLOAT);
    }
    
    /**
     * Construct a new image reader storing pixels
     * in buffer of the specified type.
     *
     * @param provider the {@link ImageReaderSpi} that is
     *                 invoking this constructor, or null.
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
     * <code>rawImageType</code> argument provided at construction time.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The data type (<code>TYPE_FLOAT</code> by default).
     * @throws IOException If an error occurs reading the format information
     *         from the input source.
     */
    public int getRawDataType(final int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        return rawImageType;
    }
    
    /**
     * Retourne l'encodage des caract�res qui seront � lire. L'impl�mentation
     * par d�faut retourne l'encodage sp�cifi� dans l'objet {@link Spi} qui a
     * cr�� ce d�codeur. Les classes d�riv�es peuvent red�finir cette m�thode
     * pour d�terminer l'encodage d'une fa�on plus �labor�e.
     *
     * @param  input Flot � lire.
     * @return Encodage du flot � lire, ou <code>null</code> pour utiliser
     *         l'encodage par d�faut de la plateforme locale.
     * @throws IOException si une lecture du flot <code>input</code> �tait
     *         n�cessaire et a �chou�.
     *
     * @see Spi#charset
     */
    public Charset getCharset(final InputStream input) throws IOException {
        return (originatingProvider instanceof Spi) ? ((Spi)originatingProvider).charset : null;
    }
    
    /**
     * Retourne l'objet � utiliser pour lire chaque ligne d'une image. L'impl�mentation par
     * d�faut construit un nouvel objet {@link LineFormat} en utilisant les conventions
     * locales sp�cifi�es par {@link Spi#locale}. Les classes d�riv�es peuvent red�finir
     * cette m�thode pour construire un objet {@link LineFormat} d'une fa�on plus �labor�e.
     *
     * @param  imageIndex Index de l'image � lire.
     * @throws IOException si l'op�ration n�cessitait une lecture du fichier (par exemple
     *         des informations inscrites dans un en-t�te) et que cette lecture a �chou�e.
     */
    public LineFormat getLineFormat(final int imageIndex) throws IOException {
        if (originatingProvider instanceof Spi) {
            final Locale locale = ((Spi)originatingProvider).locale;
            if (locale!=null) {
                return new LineFormat(locale);
            }
        }
        return new LineFormat();
    }
    
    /**
     * Retourne la valeur repr�sentant les donn�es manquantes, ou {@link Double#NaN}
     * s'il n'y en a pas. Cette valeur s'appliquera � toutes les colonnes du fichier
     * sauf les colonnes des <var>x</var> et des <var>y</var>.  L'impl�mentation par
     * d�faut retourne la valeur qui avait �t� sp�cifi�e dans l'objet {@link Spi} qui
     * a cr�� ce d�codeur. Les classes d�riv�es peuvent red�finir cette m�thode pour
     * d�terminer cette valeur d'une fa�on plus �labor�e.
     *
     * @param  imageIndex Index de l'image � lire.
     * @throws IOException si l'op�ration n�cessitait une lecture du fichier (par exemple
     *         des informations inscrites dans un en-t�te) et que cette lecture a �chou�e.
     */
    public double getPadValue(final int imageIndex) throws IOException {
        return (originatingProvider instanceof Spi) ? ((Spi)originatingProvider).padValue : Double.NaN;
    }
    
    /**
     * Retourne l'entr� {@link #input} sous forme d'objet {@link BufferedReader}. Si possible,
     * cette m�thode tentera de retourner plus sp�cifiquement un objet {@link LineNumberReader}.
     * Cette m�thode convertira automatiquement les objets de classes {@link File}, {@link URL},
     * {@link Reader}, {@link InputStream} et {@link ImageInputStream}.
     * <br><br>
     * Cette m�thode ne construira un nouveau objet {@link Reader} que la premi�re fois
     * o� elle sera appel�e. Tous les appels subs�quents retourneront le {@link Reader}
     * ouvert. En cons�quent, ce flot <strong>ne doit pas</strong> �tre ferm�. Les m�thodes
     * <code>setInput(...)</code> et {@link #reset()} s'occuperont de le fermer si n�cessaire.
     *
     * @return {@link #getInput} sous forme de flot {@link LineNumberReader} si possible,
     *         ou {@link BufferedReader} sinon.
     * @throws IOException si le flot n'a pas pu �tre ouvert.
     */
    protected final BufferedReader getReader() throws IOException {
        if (reader==null) {
            final Object input=getInput();
            if (input==null) {
                throw new IllegalStateException(Resources.format(ResourceKeys.ERROR_NO_IMAGE_INPUT));
            }
            if (input instanceof BufferedReader) {
                return reader = (BufferedReader) input;
            }
            if (input instanceof Reader) {
                return reader = new LineReader((Reader) input);
            }
            if (input instanceof InputStream) {
                final InputStream stream = (InputStream) input;
                return reader = new LineReader(stream, getCharset(stream));
            }
            if (input instanceof File) {
                final InputStream stream = new FileInputStream((File) input);
                return reader = new LineReader(stream, getCharset(stream));
            }
            if (input instanceof URL) {
                final InputStream stream = ((URL) input).openStream();
                return reader = new LineReader(stream, getCharset(stream));
            }
            if (input instanceof URLConnection) {
                final InputStream stream = ((URLConnection) input).getInputStream();
                return reader = new LineReader(stream, getCharset(stream));
            }
            final InputStream stream = new InputStreamAdapter((ImageInputStream) input);
            reader = new LineReader(stream, getCharset(stream));
        }
        return reader;
    }
    
    /**
     * Ferme le flot {@link #reader}, � la condition que
     * c'�tait un flot que nous avions ouvert nous-m�me.
     */
    private void close() {
        if (input instanceof File || input instanceof URL) {
            if (reader!=null) try {
                reader.close();
            } catch (IOException exception) {
                Utilities.unexpectedException("org.geotools.gcs",
                        "TextImageReader", "close", exception);
            }
        }
        reader = null;
    }
    
    /**
     * Sp�cifie le flot � utiliser en entr�. Si un autre flot
     * �tait ouvert avant l'appel de cette m�thode, il sera ferm�.
     */
    public void setInput(final Object  input,
                         final boolean seekForwardOnly,
                         final boolean ignoreMetadata)
    {
        close();
        super.setInput(input, seekForwardOnly, ignoreMetadata);
    }
    
    /**
     * Retourne la position du flot sp�cifi�, ou <code>-1</code> si cette position est
     * inconnue. Note: la position retourn�e est <strong>approximative</strong>.  Elle
     * est utile pour afficher un rapport des progr�s, mais sans plus.
     *
     * @param  reader Flot dont on veut conna�tre la position.
     * @return Position approximative du flot, ou <code>-1</code>
     *         si cette position n'a pas pu �tre obtenue.
     * @throws IOException si l'op�ration a �chou�e.
     */
    static long getStreamPosition(final Reader reader) throws IOException {
        return (reader instanceof LineReader) ? ((LineReader) reader).getPosition() : -1;
    }
    
    /**
     * Retourne une cha�ne de caract�res donnant la position actuelle du flot. La
     * cha�ne retourn�e sera par exemple "Ligne 14 dans le fichier HUV18204.asc".
     * Cette m�thode retourne <code>null</code> si la position du flot n'a pas pu
     * �tre d�termin�e.
     *
     * @param message Un message optionel � placer apr�s la position, ou
     *        <code>null</code> s'il n'y en a pas.
     */
    protected String getPositionString(final String message) {
        final String file;
        final Object input = getInput();
        if (input instanceof File) {
            file = ((File) input).getName();
        } else if (input instanceof URL) {
            file = ((URL ) input).getFile();
        } else {
            file = null;
        }
        final Integer line = (reader instanceof LineNumberReader) ?
                new Integer(((LineNumberReader) reader).getLineNumber()) : null;
        
        final Resources resources = Resources.getResources(null);
        final String position;
        if (file!=null) {
            if (line!=null) {
                position = resources.getString(ResourceKeys.FILE_POSITION_$2, file, line);
            } else {
                position = resources.getString(ResourceKeys.FILE_$1, file);
            }
        } else if (line!=null) {
            position = resources.getString(ResourceKeys.LINE_$1, line);
        } else {
            position=null;
        }
        
        if (position!=null) {
            if (message!=null) {
                return position+": "+message;
            } else {
                return position;
            }
        } else {
            return message;
        }
    }
    
    /**
     * Remet ce d�codeur dans son �tat initial.
     * Si un flot avait �t� ouvert, il sera ferm�.
     */
    public void reset() {
        close();
        super.reset();
    }
    
    
    
    
    /**
     * Service provider interface (SPI) for {@link TextImageReader}s. This
     * SPI provides a convenient way to control the {@link TextImageReader}
     * character encoding: the {@link #charset} field. For example, many
     * <code>Spi</code> subclasses will put the following line in their
     * constructor:
     *
     * <blockquote><pre>
     * {@link #charset} = Charset.forName("ISO-LATIN-1"); // ISO Latin Alphabet No. 1 (ISO-8859-1)
     * </pre></blockquote>
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    public static abstract class Spi extends ImageReaderSpi {
        /**
         * List of legal input types for {@link TextImageReader}.
         */
        private static final Class[] INPUT_TYPES = new Class[] {
            File.class,
            URL.class,
            URLConnection.class,
            Reader.class,
            InputStream.class,
            ImageInputStream.class
        };
        
        /**
         * Default list of file's extensions.
         */
        private static final String[] EXTENSIONS = new String[] {"txt","asc","dat"};
        
        /**
         * Encodage des caract�res � lire, ou <code>null</code> pour utiliser l'encodage
         * par d�faut de la plateforme locale.  Ce champ est initialement nul et devrait
         * �tre initialis� par les classes d�riv�es qui souhaite utiliser un encodage
         * sp�cifique.
         *
         * @see TextImageReader#getCharset
         */
        protected Charset charset;
        
        /**
         * Conventions locales � utiliser pour lire les nombres.  Par exemple
         * la valeur {@link Locale#US} signifie que les nombres seront �crits
         * en utilisant le point comme s�parateur d�cimal (entre autres
         * conventions). La valeur <code>null</code> signifie qu'il faudra
         * utiliser les conventions locales par d�faut au moment ou une image
         * sera lue.
         *
         * @see TextImageReader#getLineFormat
         * @see TextRecordImageReader#parseLine
         */
        protected Locale locale;
        
        /**
         * Valeur par d�faut repr�sentant les donn�es manquantes, ou
         * {@link Double#NaN} s'il n'y en a pas.  Lors de la lecture
         * d'une image, toutes les occurences de cette valeur seront
         * remplac�es par {@link Double#NaN} dans toutes les colonnes
         * sauf les colonnes des <var>x</var> et des <var>y</var>.
         *
         * @see TextImageReader#getPadValue
         * @see TextRecordImageReader#parseLine
         */
        protected double padValue = Double.NaN;
        
        /**
         * Construct a new SPI for {@link TextImageReader}. This
         * constructor initialize the following fields to default
         * values:
         *
         * <ul>
         *   <li>Image format names ({@link #names}):
         *       An array of lenght 1 containing the <code>name</code> argument.
         *
         *   <li>MIME type ({@link #MIMETypes}):
         *       An array of length 1 containing the <code>mime</code> argument.
         *
         *   <li>File suffixes ({@link #suffixes}):
         *       "<code>.txt</code>", "<code>.asc</code>" et "<code>.dat</code>"
         *       (uppercase and lowercase).</li>
         *
         *   <li>Input types ({@link #inputTypes}):
         *       {@link File}, {@link URL}, {@link Reader}, {@link InputStream} et {@link ImageInputStream}.</li>
         * </ul>
         *
         * Others fields should be set by subclasses
         * (usually in their constructors).
         *
         * @param name Format name, or <code>null</code> to let {@link #names} unset.
         * @param mime MIME type, or <code>null</code> to let {@link #MIMETypes} unset.
         */
        public Spi(final String name, final String mime) {
            if (name!=null) {
                names = new String[] {name};
            }
            if (mime!=null) {
                MIMETypes = new String[] {mime};
            }
            suffixes   = EXTENSIONS;
            inputTypes = INPUT_TYPES;
        }
        
        /**
         * V�rifie si le flot sp�cifi� semble �tre un fichier ASCII lisible.
         * Cette m�thode tente simplement de lire les premi�res lignes du fichier.
         * La valeur retourn�e par cette m�thode n'est qu'� titre indicative.
         * <code>true</code> n'implique pas que la lecture va forc�ment r�ussir,
         * et <code>false</code> n'implique pas que la lecture va obligatoirement
         * �chouer.
         *
         * @param  source Source dont on veut tester la lisibilit�.
         * @return <code>true</code> si la source <u>semble</u> �tre lisible.
         * @throws IOException si une erreur est survenue lors de la lecture.
         */
        public boolean canDecodeInput(final Object source) throws IOException {
            return canDecodeInput(source, 1024);
        }
        
        /**
         * V�rifie si le flot sp�cifi� semble �tre un fichier ASCII lisible.
         * Cette m�thode tente simplement de lire les premi�res lignes du fichier.
         * La valeur retourn�e par cette m�thode n'est qu'� titre indicative.
         * <code>true</code> n'implique pas que la lecture va forc�ment r�ussir,
         * et <code>false</code> n'implique pas que la lecture va obligatoirement
         * �chouer.
         *
         * @param  source Source dont on veut tester la lisibilit�.
         * @param  readAheadLimit Nombre maximal de caract�res � lire. Si ce
         *         nombre est d�pass� sans que cette m�thode ait pu d�terminer
         *         si la source est lisible ou pas, alors cette m�thode retourne
         *         <code>false</code>.
         * @return <code>true</code> si la source <u>semble</u> �tre lisible.
         * @throws IOException si une erreur est survenue lors de la lecture.
         */
        public boolean canDecodeInput(final Object source,
                                      final int readAheadLimit)
            throws IOException
        {
            if (source instanceof Reader) {
                final Reader input = (Reader)source;
                if (input.markSupported()) {
                    input.mark(readAheadLimit);
                    final boolean result = canDecodeReader(input, readAheadLimit);
                    input.reset();
                    return result;
                }
            }
            if (source instanceof InputStream) {
                return canDecodeInput((InputStream) source, readAheadLimit, false);
            }
            if (source instanceof File) {
                return canDecodeInput(new FileInputStream((File) source), readAheadLimit, true);
            }
            if (source instanceof URL) {
                return canDecodeInput(((URL) source).openStream(), readAheadLimit, true);
            }
            if (source instanceof URLConnection) {
                return canDecodeInput(((URLConnection) source).getInputStream(), readAheadLimit, false);
            }
            if (source instanceof ImageInputStream) {
                return canDecodeInput(new InputStreamAdapter((ImageInputStream) source), readAheadLimit, false);
            }
            return false;
        }
        
        /**
         * V�rifie si le flot sp�cifi� semble �tre un fichier ASCII lisible.
         * Cette m�thode tente simplement de lire la premi�re ligne du fichier.
         *
         * @param  input Source dont on veut tester la lisibilit�.
         * @param  canClose <code>true</code> si on peut fermer le flot apr�s avoir v�rifi� sa
         *         premi�re ligne. Si cet argument est <code>false</code>, alors cette m�thode
         *         utilisera {@link InputStream#mark} et {@link InputStream#reset} si ces
         *         op�rations sont autoris�es.
         * @param  readAheadLimit Nombre maximal de caract�res � lire. Si ce
         *         nombre est d�pass� sans que cette m�thode ait pu d�terminer
         *         si la source est lisible ou pas, alors cette m�thode retourne
         *         <code>false</code>.
         * @return <code>true</code> si la source <u>semble</u> �tre lisible.
         * @throws IOException si une erreur est survenue lors de la lecture.
         */
        private boolean canDecodeInput(final InputStream input,
                                       final int readAheadLimit,
                                       final boolean canClose)
            throws IOException
        {
            if (!canClose) {
                if (!input.markSupported()) {
                    return false;
                }
                input.mark(readAheadLimit);
            }
            final Reader reader = (charset!=null) ? new InputStreamReader(input, charset) :
                                                    new InputStreamReader(input);
            final boolean canDecode = canDecodeReader(reader, readAheadLimit);
            if (canClose) {
                input.close();
            } else {
                input.reset();
            }
            return canDecode;
        }
        
        /**
         * V�rifie si le flot sp�cifi� semble �tre un fichier ASCII lisible.
         * Cette m�thode tente simplement de lire la premi�re ligne du fichier.
         * Il est de la responsabilit� de l'appelant d'utiliser {@link Reader#mark},
         * {@link Reader#reset} et/ou {@link Reader#close} aux endroits appropri�s.
         *
         * @param  input Source dont on veut tester la lisibilit�.
         * @param  readAheadLimit Nombre maximal de caract�res � lire. Si ce
         *         nombre est d�pass� sans que cette m�thode ait pu d�terminer
         *         si la source est lisible ou pas, alors cette m�thode retourne
         *         <code>false</code>.
         * @return <code>true</code> si la source <u>semble</u> �tre lisible.
         * @throws IOException si une erreur est survenue lors de la lecture.
         */
        private boolean canDecodeReader(final Reader input,
                                        final int readAheadLimit)
            throws IOException
        {
            int           lower = 0;
            int           upper = 0;
            boolean      skipLF = false;
            final char[] buffer = new char[readAheadLimit];
            while (upper < buffer.length) {
                final int stop = upper + input.read(buffer, upper, Math.min(64, buffer.length-upper));
                if (stop <= upper) {
                    return false;
                }
                do {
                    switch (buffer[upper]) {
                        default:   skipLF=false;  continue;
                        case '\r': skipLF=true;   break;
                        case '\n': if (!skipLF)   break;
                        else lower++;  continue;
                    }
                    final Boolean canDecode = canDecodeLine(new String(buffer, lower, upper-lower));
                    if (canDecode!=null) {
                        return canDecode.booleanValue();
                    }
                    lower = upper;
                }
                while (++upper < stop);
            }
            return false;
        }
        
        /**
         * V�rifie si la ligne sp�cifi�e peut �tre d�cod�e. Cette m�thode est appel�e
         * automatiquement par {@link #canDecodeInput(Object,int)} avec en argument une
         * des premi�res lignes trouv�es dans la source. L'impl�mentation par d�faut
         * v�rifie si la ligne contient au moins un nombre d�cimal.
         *
         * @param  line Une des premi�res lignes du flot � lire.
         * @return {@link Boolean#TRUE} si la ligne peut �tre d�cod�e, {@link Boolean#FALSE}
         *         si elle ne peut pas �tre d�cod�e ou <code>null</code> si on ne sait pas
         *         encore. Dans ce dernier cas, cette m�thode sera appel�e une nouvelle fois
         *         avec la ligne suivante en argument.
         */
        protected Boolean canDecodeLine(final String line) {
            if (line.trim().length()!=0) {
                try {
                    final LineFormat reader = (locale!=null) ? new LineFormat(locale) : new LineFormat();
                    if (reader.setLine(line) >= 1) {
                        return isValueCountAcceptable(reader.getValueCount());
                    }
                } catch (ParseException exception) {
                    return Boolean.FALSE;
                }
            }
            return null;
        }
        
        /**
         * V�rifie si la ligne a un nombre de valeurs acceptable. Cette m�thode est appel�e
         * automatiquement par {@link #canDecodeLine} avec en argument le nombre de valeurs
         * dans une des premi�res lignes trouv�es dans la source. Cette indication n'est
         * qu'approximative et il est correct de retourner {@link Boolean#FALSE} de fa�on
         * conservative. L'impl�mentation par d�faut retourne toujours {@link Boolean#TRUE}.
         */
        Boolean isValueCountAcceptable(final int count) {
            return Boolean.TRUE;
        }
    }
}
