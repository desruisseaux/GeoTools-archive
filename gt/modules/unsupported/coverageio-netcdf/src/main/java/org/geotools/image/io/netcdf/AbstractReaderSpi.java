/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.image.io.netcdf;

// J2SE dependencies
import java.util.Locale;
import java.io.IOException;
import javax.imageio.ImageReader;

// NetCDF dependencies
import ucar.nc2.Variable;  // For javadoc

// Geomatys dependencies
import org.geotools.image.io.SampleConverter;
import org.geotools.image.io.FileBasedReaderSpi;


/**
 * Classe de base des fournisseurs de décodeurs d'images NetCDF.
 * <p>
 * Cette classe exige que l'on donne au constructeur le nom de la variable à lire. La liste
 * des variables d'un fichier donné peut être obtenu en exécutant l'instruction suivante:
 *
 * <blockquote>
 * <code>java org.geotools.image.io.netcdf.Explorer</code> <var>fichier</var>
 * </blockquote>
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public abstract class AbstractReaderSpi extends FileBasedReaderSpi implements SampleConverter {
    /**
     * List of legal names for NetCDF readers.
     */
    private static final String[] NAMES = new String[] {"netcdf", "NetCDF"};

    /**
     * Default list of file's extensions.
     */
    private static final String[] SUFFIXES = new String[] {"nc", "NC"};

    /**
     * The name of the {@linkplain Variable variable} to be read in a NetCDF file.
     */
    private final String variable;

    /**
     * An offset to add to the value to be read, before to store them in the raster.
     * This is used primarily for transforming <em>signed</em> short into <em>unsigned</em>
     * short. The default value is 0.
     */
    private final int offset;

    /**
     * The value standing for "no data". This is used primarily for integer types.
     */
    private final int nodata;

    /**
     * Constructs a service provider for the specified variable name.
     *
     * @param variable The default name of the {@linkplain Variable variable} to be read.
     */
    public AbstractReaderSpi(final String variable) {
        this(variable, 0, 0);
    }

    /**
     * Constructs a service provider for the specified variable name.
     *
     * @param variable The default name of the {@linkplain Variable variable} to be read.
     * @param offset An offset to add to the value to be read, before to store them in the raster.
     *        This is used primarily for transforming <em>signed</em> short into <em>unsigned</em>
     *        short. The default value is 0.
     * @param 
     */
    public AbstractReaderSpi(final String variable, final int offset, final int nodata) {
        names            = NAMES;
        suffixes         = SUFFIXES;
        vendorName       = "Geomatys";
        version          = "1.0";
        pluginClassName  = "org.geotools.image.io.netcdf.DefaultReader";
        this.variable    = variable;
        this.offset      = offset;
        this.nodata      = nodata;
    }

    /**
     * Retourne une description de ce format d'image.
     */
    @Override
    public String getDescription(final Locale locale) {
        return "Decodeur d'images NetCDF";
    }

    /**
     * Vérifie si le flot spécifié semble être un fichier NetCDF lisible.
     * Cette méthode tente simplement de lire les premiers octets du fichier.
     * La valeur retournée par cette méthode n'est qu'à titre indicative.
     * {@code true} n'implique pas que la lecture va forcément réussir,
     * et {@code false} n'implique pas que la lecture va obligatoirement
     * échouer.
     *
     * @param  source Source dont on veut tester la lisibilité.
     * @return {@code true} si la source <u>semble</u> être lisible.
     * @throws IOException si une erreur est survenue lors de la lecture.
     */
    public boolean canDecodeInput(final Object source) throws IOException {
        // TODO: Effectuer la vérification en utilisant NetcdfFileCache.acquire(...)
        return true;
    }

    /**
     * Constructs a NetCDF image reader. If {@code extension} is an instance of
     * {@link CharSequence}, then it is interpreted as the {@linkplain Variable variable} name.
     * Otherwise, the name specified at the {@linkplain #AbstractReaderSpi construction time}
     * is used as the default variable name.
     */
    public ImageReader createReaderInstance(final Object extension) throws IOException {
        return new DefaultReader(this, getVariable(extension));
    }

    /**
     * Returns the variable name from the specified extension.
     */
    final String getVariable(final Object extension) {
        return (extension instanceof CharSequence) ? extension.toString() : variable;
    }

    /**
     * Converts a double-precision value before to store it in the raster.
     * Subclasses should override this method if some fixed values need to
     * be converted into {@link Double#NaN} value.
     *
     * @param value The value read from the NetCDF file.
     * @return The value to store in the {@linkplain Raster raster}.
     */
    public double convert(final double value) {
        return value;
    }

    /**
     * Converts a float-precision value before to store it in the raster.
     * Subclasses should override this method if some fixed values need to
     * be converted into {@link Float#NaN} value.
     *
     * @param value The value read from the NetCDF file.
     * @return The value to store in the {@linkplain Raster raster}.
     */
    public float convert(final float value) {
        return value;
    }

    /**
     * Converts a float-precision value before to store it in the raster.
     *
     * @param value The value read from the NetCDF file.
     * @return The value to store in the {@linkplain Raster raster}.
     */
    public int convert(final int value) {
        return value;
    }
}
