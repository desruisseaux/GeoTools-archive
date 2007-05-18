/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2006, Institut de Recherche pour le Développement
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

// J2SE dependencies
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;


/**
 * Classe de base des fournisseurs de décodeurs {@link FileBasedReader}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public abstract class FileBasedReaderSpi extends ImageReaderSpi {
    /**
     * List of legal input types for {@link AbstractReader}.
     */
    private static final Class[] INPUT_TYPES = new Class[] {
        File.class, URL.class, InputStream.class, ImageInputStream.class
    };

    /**
     * Construit une nouvelle instance de ce fournisseur de service.
     */
    public FileBasedReaderSpi() {
        inputTypes = INPUT_TYPES;
        vendorName = "Geomatys";
    }

    /**
     * Retourne le type d'image que créera le décodeur. Ce type d'image comprend généralement
     * une palette de couleurs qui dépend du type de données (SST, CHL...), et donc de la classe
     * dérivée.
     *
     * @throws IOException si la palette de couleur n'a pas pu être obtenue.
     */
    protected abstract ImageTypeSpecifier getRawImageType() throws IOException;
}
