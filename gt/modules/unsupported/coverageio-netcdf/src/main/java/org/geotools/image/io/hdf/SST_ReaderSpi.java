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

// J2SE dependencies
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;

// Geomatys dependencies
import org.geotools.image.Palette;


/**
 * Fournisseurs de d�codeur d'images HDF contenant des donn�es de temp�rature de surface.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 *
 * @deprecated Ce d�codeur n'est pas encore fonctionnel. La palette de couleur doit �tre ajust�e
 *             pour les images de type "best SST" (sans indicateur de qualit�, pour l'instant).
 */
public abstract class SST_ReaderSpi extends AbstractReaderSpi {
    /**
     * Nombre de bits utilis�s pour coder la temp�rature.
     */
    private static final int DATA_BITS_COUNT = 10;

    /**
     * Nombre de bits utilis�s pour coder la qualit� de la mesure.
     */
    private static final int QUALITY_BITS_COUNT = 3;

    /**
     * Nom d'un dataset "temp�rature".
     */
    private static final String DATASET_NAME = "sst";

    /**
     * Nom d'un dataset "qualit�".
     */
    private static final String QUALITY_DATASET_NAME = "QUAL";

    /**
     * Nom de la palette de couleur pour la temp�rature.
     */
    private static final String PALETTE_NAME = "SST-Nasa";

    /**
     * Taille du mod�le de couleurs pour la SST, sans compter les r�p�titions.
     */
    private static final int PALETTE_SIZE = 512;

    /**
     * Construit un nouveau fournisseur de service.
     */
    public SST_ReaderSpi() {
        super(DATASET_NAME, QUALITY_DATASET_NAME);
        names = new String[] {
            "HDF-SST"
        };
        MIMETypes = new String[] {
            "application/x-hdf/SST"
        };
    }

    /**
     * Construit un d�codeur d'image HDF.
     */
    public ImageReader createReaderInstance(final Object extension) throws IOException {
        final DefaultReader reader = new DefaultReader(this);
        reader.setBitCount(DATA_BITS_COUNT, QUALITY_BITS_COUNT);
        return reader;
    }

    /**
     * Retourne le type d'image que cr�eront les d�codeurs HDF.
     */
    protected ImageTypeSpecifier getRawImageType() throws IOException {
        return Palette.forRepeated(PALETTE_NAME, 1 << DATA_BITS_COUNT,
                                   PALETTE_SIZE, 1 << QUALITY_BITS_COUNT);
    }

    /**
     * Retourne le nom du fichier de qualit� � partir du nom de fichier de donn�es sp�cifi�.
     */
    @Override
    protected File getQualityFile(final File input) {
        if (true) {
            // TODO: La prise en compte de fichiers de qualit� est d�sactiv�e pour l'instant.
            return super.getQualityFile(input);
        }
        final String filename  = input.getName();
        final int    separator = filename.lastIndexOf('.') + 1;
        final int    splitAt   = filename.indexOf('-', separator) + 1;
        return new File(input.getParent(), filename.substring(0, separator) + 'm' +
                                           filename.substring(separator+1, splitAt) + "qual.hdf");
    }
}
