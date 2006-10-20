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
import java.io.IOException;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;

// Geomatys dependencies
import org.geotools.image.Palette;


/**
 * Fournisseur de d�codeurs d'images de temp�rature.
 * 
 * 
 * @author C�dric Brian�on
 * @author Martin Desruisseaux
 * @version $Id$
 */
public class TemperatureReaderSpi extends AbstractReaderSpi {    
    /**
     * Nom de la palette de couleurs.
     */
    private static final String PALETTE_NAME = "Temperature";

    /**
     * Les valeurs minimales et maximales attendues.
     */
    private static final int MINIMUM = -22100, MAXIMUM = 10950;

    /**
     * La valeur repr�sentant une donn�e manquante.
     */
    private static final int NODATA = Short.MAX_VALUE;

    /**
     * Construit une nouvelle instance de ce d�codeur.
     */
    public TemperatureReaderSpi() {
        super("temperature");
    }

    /**
     * Retourne le type d'image.
     */
    protected ImageTypeSpecifier getRawImageType() throws IOException {
        return Palette.forNodataFirst(PALETTE_NAME, MAXIMUM - MINIMUM);
    }

    /**
     * Convertit une valeur du fichier NetCDF en valeur � stocker dans l'image.
     */
    @Override
    public int convert(final int value) {
        return (value == NODATA) ? 0 : (value - MINIMUM);
    }
}
