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
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.IndexColorModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.imageio.IIOException;

// Geotools dependencies
import org.geotools.io.DefaultFileFilter;
import org.geotools.io.LineFormat;
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.image.ColorUtilities;


/**
 * A factory class for {@link IndexColorModel} objects.
 * Default implementation for this class create {@link IndexColorModel} objects from
 * palette definition files. Definition files are text files containing an arbitrary
 * number of lines, each line containing RGB components ranging from 0 to 255 inclusive.
 * Empty line and line starting with '#' are ignored. Example:
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
 * The number of RGB codes doesn't have to match an {@link IndexColorModel}'s
 * map size. RGB codes will be automatically interpolated RGB values when needed.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PaletteFactory {
    /**
     * The parent factory, or {@code null} if there is none. The parent factory
     * will be queried if a palette was not found in current factory.
     */
    private final PaletteFactory parent;
    
    /**
     * The class loader from which to load the palette definition files. If {@code null} and
     * {@link #altLoader} is null as well, then loading will occurs from the system current
     * working directory.
     */
    private final ClassLoader loader;
    
    /**
     * An alternative to {@link #loader} for loading resources. At most one of {@code loader}
     * and {@code altLoader} can be non-null. If both are {@code null}, then loading will occurs
     * from the system current working directory.
     */
    private final Class altLoader;
    
    /**
     * The base directory from which to search for palette definition files.
     * If {@code null}, then the working directory (".") is assumed.
     */
    private final File directory;
    
    /**
     * File extension.
     */
    private final String extension;
    
    /**
     * The charset to use for parsing files, or {@code null} for the current default.
     */
    private final Charset charset;
    
    /**
     * The locale to use for parsing files. or {@code null} for the current default.
     */
    private final Locale locale;
    
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
     * @param locale    The locale to use for parsing files. or {@code null} for the default.
     */
    public PaletteFactory(final PaletteFactory parent,
                          final ClassLoader    loader,
                          final File        directory,
                                String      extension,
                          final Charset       charset,
                          final Locale         locale)
    {
        if (extension!=null && !extension.startsWith(".")) {
            extension = '.' + extension;
        }
        this.parent    = parent;
        this.loader    = loader;
        this.altLoader = null;
        this.directory = directory;
        this.extension = extension;
        this.charset   = charset;
        this.locale    = locale;
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
        if (extension!=null && !extension.startsWith(".")) {
            extension = '.' + extension;
        }
        this.parent    = parent;
        this.loader    = null;
        this.altLoader = loader;
        this.directory = directory;
        this.extension = extension;
        this.charset   = charset;
        this.locale    = locale;
    }

    /**
     * Returns an input stream for reading the specified resource. The default
     * implementation delegates to the {@link Class#getResourceAsStream(String) Class} or
     * {@link ClassLoader#getResourceAsStream(String) ClassLoader} method of the same name,
     * according the {@code loader} argument value given to the constructor. Subclasses may
     * override this method if a more elaborated mechanism is wanted for fetching resources.
     * This is sometime required in the context of applications using particular class loaders.
     *
     * @param name The name of the resource to load, constructed as {@code directory} + {@code name}
     *             + {@code extension} where <var>directory</var> and <var>extension</var> were
     *             specified to the constructor, while {@code name} was given to the
     *             {@link #getColors(String) getColors} method.
     * @return The input stream, or {@code null} if the resources was not found.
     *
     * @since 2.3
     */
    protected InputStream getResourceAsStream(final String name) {
        if (altLoader != null) {
            return altLoader.getResourceAsStream(name);
        }
        if (loader != null) {
            return loader.getResourceAsStream(name);
        }
        return null;
    }

    /**
     * Returns the list of available palette names. Any item in this list can be specified as
     * argument to {@link #getColors(String)} or {@link #getIndexColorModel(String)} methods.
     *
     * @return The list of available palette name, or {@code null} if this method
     *         is unable to fetch this information.
     */
    public String[] getAvailableNames() {
        File dir = (directory != null) ? directory : new File(".");
        if (loader != null) {
            dir = toFile(loader.getResource(dir.getPath()));
            if (dir == null) {
                // Directory not found.
                return null;
            }
        } else if (altLoader != null) {
            dir = toFile(altLoader.getResource(dir.getPath()));
            if (dir == null) {
                // Directory not found.
                return null;
            }
        }
        if (!dir.isDirectory()) {
            return null;
        }
        final String[] names = dir.list(new DefaultFileFilter('*'+extension));
        final int extLg = extension.length();
        for (int i=0; i<names.length; i++) {
            final String name = names[i];
            final int lg = name.length();
            if (lg>extLg && name.regionMatches(true, lg-extLg, extension, 0, extLg)) {
                names[i] = name.substring(0, lg-extLg);
            }
        }
        return names;
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
     * Returns a buffered reader for the specified name.
     *
     * @param  The palette's name to load. This name doesn't need to contains a path
     *         or an extension. Path and extension are set according value specified
     *         at construction time.
     * @return A buffered reader to read {@code name}.
     * @throws IOException if an I/O error occured.
     */
    private BufferedReader getReader(String name) throws IOException {
        if (extension!=null && !name.endsWith(extension)) {
            name += extension;
        }
        final File   file = new File(directory, name);
        final String path = file.getPath().replace(File.separatorChar, '/');
        InputStream stream;
        try {
            stream = getResourceAsStream(path);
            if (stream == null) {
                if (file.exists()) {
                    stream = new FileInputStream(file);
                } else {
                    return null;
                }
            }
        } catch (SecurityException e) {
            // 'getColors' is the public method that invoked this private method.
            Utilities.recoverableException("org.geotools.image", "PaletteFactory", "getColors", e);
            return null;
        }
        return getReader(stream);
    }
    
    /**
     * Returns a buffered reader for the specified stream.
     *
     * @param  The input stream.
     * @return A buffered reader to read the input stream.
     * @throws IOException if an I/O error occured.
     */
    private BufferedReader getReader(final InputStream stream) throws IOException {
        final Reader reader = (charset!=null) ? new InputStreamReader(stream, charset) :
                                                new InputStreamReader(stream);
        return new BufferedReader(reader);
    }
    
    /**
     * Procède au chargement d'un ensemble de couleurs. Les couleurs doivent
     * être codées sur trois colonnes dans un fichier texte. Les colonnes
     * doivent être des entiers de 0 à 255 correspondant (dans l'ordre) aux
     * couleurs rouge (R), verte (G) et bleue (B). Les lignes vierges ainsi
     * que les lignes dont le premier caractère non-blanc est # seront ignorées.
     *
     * @param  input Flot contenant les codes de couleurs de la palette.
     * @return Couleurs obtenues à partir des codes lues.
     * @throws IOException si une erreur est survenue lors de la lecture.
     * @throws IIOException si une erreur est survenue lors de l'interprétation des codes de couleurs.
     */
    private Color[] getColors(final BufferedReader input) throws IOException {
        int values[]=new int[3]; // On attend exactement 3 composantes par ligne.
        final LineFormat reader = (locale!=null) ? new LineFormat(locale) : new LineFormat();
        final List colors       = new ArrayList();
        String line; while ((line=input.readLine())!=null) try {
            line=line.trim();
            if (line.length() == 0)        continue;
            if (line.charAt(0) == '#')     continue;
            if (reader.setLine(line) == 0) continue;
            values = reader.getValues(values);
            colors.add(new Color(byteValue(values[0]), byteValue(values[1]), byteValue(values[2])));
        } catch (ParseException exception) {
            final IIOException error = new IIOException(exception.getLocalizedMessage());
            error.initCause(exception);
            throw error;
        }
        return (Color[]) colors.toArray(new Color[colors.size()]);
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
        final BufferedReader reader = getReader(name);
        if (reader == null) {
            return (parent!=null) ? parent.getColors(name) : null;
        }
        final Color[] colors = getColors(reader);
        reader.close();
        return colors;
    }
    
    /**
     * Load colors from an URL.
     *
     * @param  url The palette's URL.
     * @return The set of colors, or {@code null} if the set was not found.
     * @throws IOException if an error occurs during reading.
     * @throws IIOException if an error occurs during parsing.
     */
    public Color[] getColors(final URL url) throws IOException {
        final BufferedReader reader = getReader(url.openStream());
        final Color[] colors = getColors(reader);
        reader.close();
        return colors;
    }
    
    /**
     * Load an index color model from a definition file.
     * The returned model will use index from 0 to 255 inclusive.
     *
     * @param  name The palette's name to load. This name doesn't need to contains a path
     *              or an extension. Path and extension are set according value specified
     *              at construction time.
     * @return The index color model, or {@code null} if the palettes was not found.
     * @throws IOException if an error occurs during reading.
     * @throws IIOException if an error occurs during parsing.
     */
    public IndexColorModel getIndexColorModel(final String name) throws IOException {
        return getIndexColorModel(name, 0, 256);
    }
    
    /**
     * Load an index color model from a definition file.
     * The returned model will use index from {@code lower} inclusive to
     * {@code upper} exclusive. Other index will have a transparent color.
     *
     * @param  name The palette's name to load. This name doesn't need to contains a path
     *              or an extension. Path and extension are set according value specified
     *              at construction time.
     * @param  lower Palette's lower index (inclusive).
     * @param  upper Palette's upper index (exclusive).
     * @return The index color model, or {@code null} if the palettes was not found.
     * @throws IOException if an error occurs during reading.
     * @throws IIOException if an error occurs during parsing.
     *
     * @since 2.3
     */
    public IndexColorModel getIndexColorModel(final String name,
                                              final int    lower,
                                              final int    upper)
            throws IOException
    {
        if (lower < 0) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                                               "lower", new Integer(lower)));
        }
        if (upper <= lower) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_RANGE_$2,
                                               new Integer(lower), new Integer(upper)));
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
    private static int byteValue(final int value) throws ParseException {
        if (value>=0 && value<256) return value;
        throw new ParseException(Errors.format(ErrorKeys.RGB_OUT_OF_RANGE_$1,
                                 new Integer(value)), 0);
    }

    /**
     * Returns the specified color palette as an image of the specified size.
     *
     * @param  name The palette's name to load. This name doesn't need to contains a path
     *              or an extension. Path and extension are set according value specified
     *              at construction time.
     * @param  size The image size. The palette will be vertical if
     *              <code>size.{@linkplain Dimension#height height}</code> &gt;
     *              <code>size.{@linkplain Dimension#width  width }</code>
     *
     * @since 2.3
     */
    public RenderedImage getImage(final String name, final Dimension size)
            throws IOException
    {
        final IndexColorModel colors;
        final BufferedImage   image;
        final WritableRaster  raster;
        colors = getIndexColorModel(name);
        image  = new BufferedImage(size.width, size.height, BufferedImage.TYPE_BYTE_INDEXED, colors);
        raster = image.getRaster();
        int xmin   = raster.getMinX();
        int ymin   = raster.getMinY();
        int width  = raster.getWidth();
        int height = raster.getHeight();
        final boolean horizontal = size.width >= size.height;
        // Computation will be performed as if the image were horizontal.
        // If it is not, interchanges x and y values.
        if (!horizontal) {
            int tmp;
            tmp = xmin;  xmin  = ymin;   ymin   = tmp;
            tmp = width; width = height; height = tmp;
        }
        final int xmax = xmin + width;
        final int ymax = ymin + height;
        final double scale = (double)colors.getMapSize() / (double)width;
        for (int x=xmin; x<xmax; x++) {
            final int value = (int) Math.round(scale * (x-xmin));
            for (int y=ymin; y<ymax; y++) {
                if (horizontal) {
                    raster.setSample(x, y, 0, value);
                } else {
                    raster.setSample(y, x, 0, value);
                }
            }
        }
        return image;
    }
}
