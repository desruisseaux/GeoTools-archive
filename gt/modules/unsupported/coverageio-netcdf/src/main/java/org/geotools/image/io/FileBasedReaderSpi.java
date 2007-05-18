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

// J2SE dependencies
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;


/**
 * Classe de base des fournisseurs de d�codeurs {@link FileBasedReader}.
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
     * Retourne le type d'image que cr�era le d�codeur. Ce type d'image comprend g�n�ralement
     * une palette de couleurs qui d�pend du type de donn�es (SST, CHL...), et donc de la classe
     * d�riv�e.
     *
     * @throws IOException si la palette de couleur n'a pas pu �tre obtenue.
     */
    protected abstract ImageTypeSpecifier getRawImageType() throws IOException;
}
