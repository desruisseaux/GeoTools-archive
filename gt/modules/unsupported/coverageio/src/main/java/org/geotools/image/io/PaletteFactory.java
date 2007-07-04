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

// J2SE dependencies
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.RenderedImage;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import javax.imageio.ImageReader;   // For javadoc
import javax.imageio.IIOException;
import java.util.*;

// Geotools dependencies
import org.geotools.io.DefaultFileFilter;
import org.geotools.io.LineFormat;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.ResourceBundle;
import org.geotools.resources.image.ColorUtilities;
import org.geotools.util.Logging;
import org.geotools.util.CanonicalSet;


/**
 * A factory for {@linkplain IndexColorModel index color models} created from RGB values listed
 * in files. The palette definition files are text files containing an arbitrary number of lines,
 * each line containing RGB components ranging from 0 to 255 inclusive. An optional fourth column
 * may be provided for alpha components. Empty lines and lines starting with the {@code '#'}
 * character are ignored. Example:
 *
 * <blockquote><pre>
 * # RGB codes for SeaWiFs images
 * # (chlorophylle-a concentration)
 *
 *   033   000   096
 *   032   000   097
 *   031   000   099
 *   030   000   101
 *   029   000   102
 *   028   000   104
 *   026   000   106
 *   025   000   107
 * <i>etc...</i>
 * </pre></blockquote>
 *
 * The number of RGB codes doesn't have to match the target {@linkplain IndexColorModel#getMapSize
 * color map size}. RGB codes will be automatically interpolated as needed.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PaletteFactory {
    /**
     * The file which contains a list of available color palettes. This file is optional
     * and used only in last resort, since scanning a directory content is more reliable.
     * If such file exists in the same directory than the one that contains the palettes,
     * this file will be used by {@link #getAvailableNames}.
     */
    private static final String LIST_FILE = "list.txt";

    /**
     * The default palette factories.
     */
    private static final Map/*<Locale,PaletteFactory>*/ defaultFactories = new HashMap();

    /**
     * The parent factory, or {@code null} if there is none. The parent factory
     * will be queried if a palette was not found in current factory.
     */
    private final PaletteFactory parent;

    /**
     * The class loader from which to load the palette definition files. If {@code null} and
     * {@link #loader} is null as well, then loading will occurs from the system current
     * working directory.
     */
    private final ClassLoader classloader;

    /**
     * An alternative to {@link #classloader} for loading resources. At most one of
     * {@code classloader} and {@code loader} can be non-null. If both are {@code null},
     * then loading will occurs from the system current working directory.
     */
    private final Class loader;

    /**
     * The base directory from which to search for palette definition files.
     * If {@code null}, then the working directory ({@code "."}) is assumed.
     */
    private final File directory;

    /**
     * The file extension.
     */
    private final String extension;

    /**
     * The charset to use for parsing files, or {@code null} for the current default.
     */
    private final Charset charset;

    /**
     * The locale to use for parsing files, or {@code null} for the current default.
     */
    private final Locale locale;

    /**
     * The locale to use for formatting error messages, or {@code null} for the current default.
     */
    private final Locale messageLocale;

    /**
     * The set of palettes already created.
     */
    private final CanonicalSet palettes = new CanonicalSet();

    /**
     * The set of palettes protected from garbage collection. We protect a palette as long as it
     * holds a reference to a color model - this is necessary in order to prevent multiple creation
     * of the same {@link IndexColorModel}. The references are cleaned by {@link PaletteDisposer}.
     */
    final Set protectedPalettes = new HashSet();

    /**
     * Gets the default palette factory. This default instance search for
     * {@code org/geotools/image/io/colors/*.pal} files where {@code '*'}
     * are the names to be specified to {@link #getPalette} and similar methods.
     *
     * @deprecated Use {@link #getDefault(Locale)} instead.
     */
    public static PaletteFactory getDefault() {
        return getDefault(null);
    }

    /**
     * Gets the default palette factory. This default instance search for
     * {@code org/geotools/image/io/colors/*.pal} files where {@code '*'}
     * are the names to be specified to {@link #getPalette} and similar methods.
     *
     * @param messageLocale The locale to use for formatting error messages, or {@code null}
     *        for the default locale. This is typically the {@linkplain ImageReader#getLocale
     *        image reader locale}.
     *
     * @since 2.4
     */
    public static synchronized PaletteFactory getDefault(final Locale messageLocale) {
        PaletteFactory factory = (PaletteFactory) defaultFactories.get(messageLocale);
        if (factory == null) {
            factory = new PaletteFactory(
            /* parent factory */ null,
            /* class loader   */ PaletteFactory.class,
            /* root directory */ new File("colors"),
            /* extension      */ ".pal",
            /* character set  */ Charset.forName("ISO-8859-1"),
            /* locale         */ Locale.US,
            /* message locale */ messageLocale);
            defaultFactories.put(messageLocale, factory);
        }
        return factory;
    }

    /**
     * @deprecated Use the same constructor with one additional {@link Locale} argument.
     */
    public PaletteFactory(final PaletteFactory parent,
                          final ClassLoader    loader,
                          final File        directory,
                                String      extension,
                          final Charset       charset,
                          final Locale         locale)
    {
        this(parent, loader, directory, extension, charset, locale, null);
    }

    /**
     * Constructs a palette factory using an optional {@linkplain ClassLoader class loader}
     * for loading palette definition files.
     *
     * @param parent    An optional parent factory, or {@code null} if there is none. The parent
     *                  factory will be queried if a palette was not found in the current factory.
     * @param loader    An optional class loader to use for loading the palette definition files.
     *                  If {@code null}, loading will occurs from the system current working
     *                  directory.
     * @param directory The base directory for palette definition files. It may be a Java package
     *                  if a {@code loader} were specified. If {@code null}, then {@code "."} is
     *                  assumed.
     * @param extension File name extension, or {@code null} if there is no extension
     *                  to add to filename. If non-null, this extension will be automatically
     *                  appended to filename. It should starts with the {@code '.'} character.
     * @param charset   The charset to use for parsing files, or {@code null} for the default.
     * @param locale    The locale to use for parsing files, or {@code null} for the default.
     * @param messageLocale The locale to use for formatting error messages, or {@code null}
     *                  for the default locale. This is typically the
     *                  {@linkplain ImageReader#getLocale image reader locale}.
     *
     * @since 2.4
     */
    public PaletteFactory(final PaletteFactory parent,
                          final ClassLoader    loader,
                          final File        directory,
                                String      extension,
                          final Charset       charset,
                          final Locale         locale,
                          final Locale  messageLocale)
    {
        if (extension!=null && !extension.startsWith(".")) {
            extension = '.' + extension;
        }
        this.parent        = parent;
        this.classloader   = loader;
        this.loader        = null;
        this.directory     = directory;
        this.extension     = extension;
        this.charset       = charset;
        this.locale        = locale;
        this.messageLocale = messageLocale;
    }

    /**
     * @deprecated Use the same constructor with one additional {@link Locale} argument.
     *
     * @since 2.2
     */
    public PaletteFactory(final PaletteFactory parent,
                          final Class          loader,
                          final File        directory,
                                String      extension,
                          final Charset       charset,
                          final Locale         locale)
    {
        this(parent, loader, directory, extension, charset, locale, null);
    }

    /**
     * Constructs a palette factory using an optional {@linkplain Class class} for loading
     * palette definition files. Using a {@linkplain Class class} instead of a {@linkplain
     * ClassLoader class loader} can avoid security issue on some platforms (some platforms
     * do not allow to load resources from a {@code ClassLoader} because it can load from the
     * root package).
     *
     * @param parent    An optional parent factory, or {@code null} if there is none. The parent
     *                  factory will be queried if a palette was not found in the current factory.
     * @param loader    An optional class to use for loading the palette definition files.
     *                  If {@code null}, loading will occurs from the system current working
     *                  directory.
     * @param directory The base directory for palette definition files. It may be a Java package
     *                  if a {@code loader} were specified. If {@code null}, then {@code "."} is
     *                  assumed.
     * @param extension File name extension, or {@code null} if there is no extension
     *                  to add to filename. If non-null, this extension will be automatically
     *                  appended to filename. It should starts with the {@code '.'} character.
     * @param charset   The charset to use for parsing files, or {@code null} for the default.
     * @param locale    The locale to use for parsing files. or {@code null} for the default.
     * @param messageLocale The locale to use for formatting error messages, or {@code null}
     *                  for the default locale. This is typically the
     *                  {@linkplain ImageReader#getLocale image reader locale}.
     *
     * @since 2.4
     */
    public PaletteFactory(final PaletteFactory parent,
                          final Class          loader,
                          final File        directory,
                                String      extension,
                          final Charset       charset,
                          final Locale         locale,
                          final Locale  messageLocale)
    {
        if (extension!=null && !extension.startsWith(".")) {
            extension = '.' + extension;
        }
        this.parent        = parent;
        this.classloader   = null;
        this.loader        = loader;
        this.directory     = directory;
        this.extension     = extension;
        this.charset       = charset;
        this.locale        = locale;
        this.messageLocale = messageLocale;
    }

    /**
     * Returns the resources for formatting error messages.
     */
    final ResourceBundle getErrorResources() {
        return Errors.getResources(messageLocale);
    }

    /**
     * Returns an input stream for reading the specified resource. The default
     * implementation delegates to the {@link Class#getResourceAsStream(String) Class} or
     * {@link ClassLoader#getResourceAsStream(String) ClassLoader} method of the same name,
     * according the {@code loader} argument type given to the constructor. Subclasses may
     * override this method if a more elaborated mechanism is wanted for fetching resources.
     * This is sometime required in the context of applications using particular class loaders.
     *
     * @param name The name of the resource to load, constructed as {@code directory} + {@code name}
     *             + {@code extension} where <var>directory</var> and <var>extension</var> were
     *             specified to the constructor, while {@code name} was given to the
     *             {@link #getPalette(String) getPalette} method.
     * @return The input stream, or {@code null} if the resources was not found.
     *
     * @since 2.3
     */
    protected InputStream getResourceAsStream(final String name) {
        if (loader != null) {
            return loader.getResourceAsStream(name);
        }
        if (classloader != null) {
            return classloader.getResourceAsStream(name);
        }
        return null;
    }

    /**
     * Returns the list of available palette names. Any item in this list can be specified as
     * argument to {@link #getPalette(String)}.
     *
     * @return The list of available palette name, or {@code null} if this method
     *         is unable to fetch this information.
     */
    public String[] getAvailableNames() {
        final Set names = new TreeSet();
        PaletteFactory factory = this;
        do {
            factory.getAvailableNames(names);
            factory = factory.parent;
        } while (parent != null);
        return (String[]) names.toArray(new String[names.size()]);
    }

    /**
     * Adds available palette names to the specified collection.
     */
    private void getAvailableNames(final Collection names) {
        /*
         * First, parses the content of every "list.txt" files found on the classpath. Those files
         * are optional. But if they are present, we assume that their content are accurate.
         */
        String filename = new File(directory, LIST_FILE).getPath();
        BufferedReader in = getReader(LIST_FILE, "getAvailableNames");
        try {
            if (in != null) {
                readNames(in, names);
            }
            if (classloader != null) {
                for (final Enumeration it=classloader.getResources(filename); it.hasMoreElements();) {
                    final URL url = (URL) it.nextElement();
                    in = getReader(url.openStream());
                    readNames(in, names);
                }
            }
        } catch (IOException e) {
            /*
             * Logs a warning but do not stop. The only consequence is that the names list
             * will be incomplete. We log the message as if came from getAvailableNames(),
             * which is the public method that invoked this one.
             */
            Logging.unexpectedException("org.geotools.image.io",
                    PaletteFactory.class, "getAvailableNames", e);
        }
        /*
         * After the "list.txt" files, check if the resources can be read as a directory.
         * It may happen if the classpath point toward a directory of .class files rather
         * than a JAR file.
         */
        File dir = (directory != null) ? directory : new File(".");
        if (classloader != null) {
            dir = toFile(classloader.getResource(dir.getPath()));
            if (dir == null) {
                // Directory not found.
                return;
            }
        } else if (loader != null) {
            dir = toFile(loader.getResource(dir.getPath()));
            if (dir == null) {
                // Directory not found.
                return;
            }
        }
        if (!dir.isDirectory()) {
            return;
        }
        final String[] list = dir.list(new DefaultFileFilter('*' + extension));
        final int extLg = extension.length();
        for (int i=0; i<list.length; i++) {
            filename = list[i];
            final int lg = filename.length();
            if (lg>extLg && filename.regionMatches(true, lg-extLg, extension, 0, extLg)) {
                names.add(filename.substring(0, lg-extLg));
            }
        }
    }

    /**
     * Copies the content of the specified reader to the specified collection.
     * The reader is closed after this operation.
     */
    private static void readNames(final BufferedReader in, final Collection names) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.length() != 0 && line.charAt(0) != '#') {
                names.add(line);
            }
        }
        in.close();
    }

    /**
     * Transforms an {@link URL} into a {@link File}. If the URL can't be
     * interpreted as a file, then this method returns {@code null}.
     */
    private static File toFile(final URL url) {
        if (url!=null && url.getProtocol().equalsIgnoreCase("file")) {
            return new File(url.getPath());
        }
        return null;
    }

    /**
     * Returns a buffered reader for the specified palette.
     *
     * @param  The palette's name to load. This name doesn't need to contains a path
     *         or an extension. Path and extension are set according value specified
     *         at construction time.
     * @return A buffered reader to read {@code name}, or {@code null} if the resource is not found.
     */
    private LineNumberReader getPaletteReader(String name) {
        if (extension!=null && !name.endsWith(extension)) {
            name += extension;
        }
        return getReader(name, "getPalette");
    }

    /**
     * Returns a buffered reader for the specified filename.
     *
     * @param  The filename. Path and extension are set according value specified
     *         at construction time.
     * @return A buffered reader to read {@code name}, or {@code null} if the resource is not found.
     */
    private LineNumberReader getReader(final String name, final String caller) {
        final File   file = new File(directory, name);
        final String path = file.getPath().replace(File.separatorChar, '/');
        InputStream stream;
        try {
            stream = getResourceAsStream(path);
            if (stream == null) {
                if (file.canRead()) try {
                    stream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    /*
                     * Should not occurs, since we checked for file existence. This is not a fatal
                     * error however, since this method is allowed to returns null if the resource
                     * is not available.
                     */
                    Logging.unexpectedException("org.geotools.image.io", PaletteFactory.class, caller, e);
                    return null;
                } else {
                    return null;
                }
            }
        } catch (SecurityException e) {
            Utilities.recoverableException("org.geotools.image.io", PaletteFactory.class, caller, e);
            return null;
        }
        return getReader(stream);
    }

    /**
     * Wraps the specified input stream into a reader.
     */
    private LineNumberReader getReader(final InputStream stream) {
        return new LineNumberReader((charset != null) ?
            new InputStreamReader(stream, charset) : new InputStreamReader(stream));
    }

    /**
     * Reads the colors declared in the specified input stream. Colors must be encoded on 3 or 4
     * columns. If 3 columns, it is assumed RGB values. If 4 columns, it is assumed RGBA values.
     * Values must be in the 0-255 ranges. Empty lines and lines starting by {@code '#'} are
     * ignored.
     *
     * @param  input The stream to read.
     * @param  name  The palette name to read. Used for formatting error message only.
     * @return The colors.
     * @throws IOException if an I/O error occured.
     * @throws IIOException if a syntax error occured.
     */
    private Color[] getColors(final LineNumberReader input, final String name) throws IOException {
        int values[] = null;
        final LineFormat reader = (locale!=null) ? new LineFormat(locale) : new LineFormat();
        final List colors       = new ArrayList();
        String line; while ((line=input.readLine()) != null) try {
            line = line.trim();
            if (line.length() == 0)        continue;
            if (line.charAt(0) == '#')     continue;
            if (reader.setLine(line) == 0) continue;
            values = reader.getValues(values);
            int A=255,R,G,B;
            switch (values.length) {
                case 4: A = byteValue(values[3]); // fall through
                case 3: B = byteValue(values[2]);
                        G = byteValue(values[1]);
                        R = byteValue(values[0]);
                        break;
                default: {
                    throw syntaxError(input, name, null);
                }
            }
            final Color color;
            try {
                color = new Color(R, G, B, A);
            } catch (IllegalArgumentException exception) {
                /*
                 * Color constructor checks the RGBA value and throws an IllegalArgumentException
                 * if they are not in the 0-255 range. Intercept this exception and rethrows as a
                 * checked IIOException, since we want to notify the user that the palette file is
                 * badly formatted. (additional note: it is somewhat redundant with byteValue(int)
                 * work. Lets keep it as a safety).
                 */
                throw syntaxError(input, name, exception);
            }
            colors.add(color);
        } catch (ParseException exception) {
            throw syntaxError(input, name, exception);
        }
        return (Color[]) colors.toArray(new Color[colors.size()]);
    }

    /**
     * Prepares an exception for the specified cause, which may be {@code null}.
     */
    private IIOException syntaxError(final LineNumberReader input, final String name, final Exception cause) {
        String message = getErrorResources().getString(
                ErrorKeys.BAD_LINE_IN_FILE_$2, name, new Integer(input.getLineNumber()));
        if (cause != null) {
            message += cause.getLocalizedMessage();
        }
        return new IIOException(message, cause);
    }

    /**
     * Load colors from an URL.
     *
     * @param  url The palette's URL.
     * @return The set of colors, or {@code null} if the set was not found.
     * @throws IOException if an error occurs during reading.
     * @throws IIOException if an error occurs during parsing.
     *
     * @deprecated This method should not be defined here since {@code PaletteFactory} is all
     *             about name relative to a directory specified at construction time. If a user
     *             wants the functionality provided by this method, he should consider creating
     *             a new instance of {@code PaletteFactory}.
     */
    public Color[] getColors(final URL url) throws IOException {
        final InputStream stream = url.openStream();
        final LineNumberReader reader = new LineNumberReader((charset!=null) ?
                new InputStreamReader(stream, charset) : new InputStreamReader(stream));
        final Color[] colors = getColors(reader, url.getFile());
        reader.close();
        return colors;
    }

    /**
     * Load colors from a definition file.
     *
     * @param  name The palette's name to load. This name doesn't need to contains a path
     *              or an extension. Path and extension are set according value specified
     *              at construction time.
     * @return The set of colors, or {@code null} if the set was not found.
     * @throws IOException if an error occurs during reading.
     * @throws IIOException if an error occurs during parsing.
     */
    public Color[] getColors(final String name) throws IOException {
        final LineNumberReader reader = getPaletteReader(name);
        if (reader == null) {
            return (parent!=null) ? parent.getColors(name) : null;
        }
        final Color[] colors = getColors(reader, name);
        reader.close();
        return colors;
    }

    /**
     * Loads an index color model from a definition file.
     * The returned model will use index from 0 to 255 inclusive.
     *
     * @param  name The palette's name to load. This name doesn't need to contains a path
     *              or an extension. Path and extension are set according value specified
     *              at construction time.
     * @return The index color model, or {@code null} if the palettes was not found.
     *
     * @throws IOException if an error occurs during reading.
     * @throws IIOException if an error occurs during parsing.
     *
     * @deprecated Replaced by {@link Palette#getColorModel}.
     */
    public IndexColorModel getIndexColorModel(final String name) throws IOException {
        return getIndexColorModel(name, 0, 256);
    }

    /**
     * Loads an index color model from a definition file.
     * The returned model will use index from {@code lower} inclusive to
     * {@code upper} exclusive. Other index will have a transparent color.
     *
     * @param  name The palette's name to load. This name doesn't need to contains a path
     *              or an extension. Path and extension are set according value specified
     *              at construction time.
     * @param  lower Palette's lower index (inclusive).
     * @param  upper Palette's upper index (exclusive).
     * @return The index color model, or {@code null} if the palettes was not found.
     *
     * @throws IOException if an error occurs during reading.
     * @throws IIOException if an error occurs during parsing.
     *
     * @since 2.3
     *
     * @deprecated Replaced by {@link Palette#getColorModel}.
     */
    public IndexColorModel getIndexColorModel(final String name,
                                              final int    lower,
                                              final int    upper)
            throws IOException
    {
        if (lower < 0) {
            throw new IllegalArgumentException(getErrorResources().getString(
                    ErrorKeys.ILLEGAL_ARGUMENT_$2, "lower", new Integer(lower)));
        }
        if (upper <= lower) {
            throw new IllegalArgumentException(getErrorResources().getString(
                    ErrorKeys.BAD_RANGE_$2, new Integer(lower), new Integer(upper)));
        }
        final Color[] colors = getColors(name);
        if (colors == null) {
            return (parent!=null) ? parent.getIndexColorModel(name, lower, upper) : null;
        }
        final int[] ARGB = new int[1 << ColorUtilities.getBitCount(upper)];
        ColorUtilities.expand(colors, ARGB, lower, upper);
        return ColorUtilities.getIndexColorModel(ARGB);
    }

    /**
     * Ensure that the specified valus is inside the {@code [0..255]} range.
     * If the value is outside that range, a {@link ParseException} is thrown.
     */
    private int byteValue(final int value) throws ParseException {
        if (value>=0 && value<256) {
            return value;
        }
        throw new ParseException(getErrorResources().getString(
                ErrorKeys.RGB_OUT_OF_RANGE_$1, new Integer(value)), 0);
    }

    /**
     * Returns the specified color palette as an image of the specified size.
     * This is useful for looking visually at a color palette.
     *
     * @param  name The palette's name to load. This name doesn't need to contains a path
     *              or an extension. Path and extension are set according value specified
     *              at construction time.
     * @param  size The image size. The palette will be vertical if
     *              <code>size.{@linkplain Dimension#height height}</code> &gt;
     *              <code>size.{@linkplain Dimension#width  width }</code>
     *
     * @since 2.3
     *
     * @deprecated Replaced by {@link Palette#getImage}.
     */
    public RenderedImage getImage(final String name, final Dimension size)
            throws IOException
    {
        return getPalette(name, Math.max(size.width, size.height)).getImage(size);
    }

    /**
     * Returns the palette of the specified name and size. The palette's name doesn't need
     * to contains a directory path or an extension. Path and extension are set according
     * values specified at construction time.
     *
     * @param  name The palette's name to load.
     * @param  size The {@linkplain IndexColorModel index color model} size.
     * @return The palette.
     *
     * @since 2.4
     */
    public Palette getPalette(final String name, final int size) {
        return getPalette(name, 0, size, size);
    }

    /**
     * Returns a palette with a <cite>pad value</cite> at index 0.
     *
     * @param  name The palette's name to load.
     * @param  size The {@linkplain IndexColorModel index color model} size.
     * @return The palette.
     *
     * @since 2.4
     */
    public Palette getPalettePadValueFirst(final String name, final int size) {
        return getPalette(name, 1, size, size);
    }

    /**
     * Returns a palette with <cite>pad value</cite> at the last index.
     *
     * @param  name The palette's name to load.
     * @param  size The {@linkplain IndexColorModel index color model} size.
     * @return The palette.
     *
     * @since 2.4
     */
    public Palette getPalettePadValueLast(final String name, final int size) {
        return getPalette(name, 0, size-1, size);
    }

    /**
     * Returns the palette of the specified name and size. The RGB colors will be distributed
     * in the range {@code lower} inclusive to {@code upper} exclusive. Remaining pixel values
     * (if any) will be left to a black or transparent color by default.
     * <p>
     * The palette's name doesn't need to contains a directory path or an extension.
     * Path and extension are set according values specified at construction time.
     *
     * @param  name The palette's name to load.
     * @param lower Index of the first valid element (inclusive) in the
     *              {@linkplain IndexColorModel index color model} to be created.
     * @param upper Index of the last valid element (exclusive) in the
     *              {@linkplain IndexColorModel index color model} to be created.
     * @param size  The size of the {@linkplain IndexColorModel index color model} to be created.
     *              This is the value to be returned by {@link IndexColorModel#getMapSize}.
     * @return The palette.
     *
     * @since 2.4
     */
    public Palette getPalette(final String name, final int lower, final int upper, final int size) {
        Palette palette = new Palette(this, name, lower, upper, size);
        palette = (Palette) palettes.unique(palette);
        return palette;
    }
}
