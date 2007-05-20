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
import java.util.Locale;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageTypeSpecifier;

// HDF dependencies
import ncsa.hdf.object.FileFormat;

// Geomatys dependencies
import org.geotools.image.io.FileBasedReaderSpi;


/**
 * Classe de base des fournisseurs de d�codeurs d'images HDF.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public abstract class AbstractReaderSpi extends FileBasedReaderSpi {
    /**
     * List of legal names for HDF readers.
     */
    private static final String[] NAMES = new String[] {"hdf", "HDF"};

    /**
     * Default list of file's extensions.
     */
    private static final String[] SUFFIXES = new String[] {"hdf", "HDF"};

    /**
     * Le nom du "dataset" des donn�es. Actuellement, pour la temp�rature ce nom
     * est {@code "sst"} et pour la chlorophylle le nom est {@code "l3m_data"}.
     */
    final String dataName;

    /**
     * Le nom du "dataset" des indicateurs de qualit�, ou {@code null} si aucun.
     */
    final String qualityName;

    /**
     * Construit une nouvelle instance de ce fournisseur de service.
     *
     * @param dataName     Le nom du "dataset" des donn�es.
     * @param qualityName  Le nom du "dataset" des indicateurs de qualit�, ou {@code null} si aucun.
     */
    public AbstractReaderSpi(final String dataName, final String qualityName) {
        names            = NAMES;
        suffixes         = SUFFIXES;
        vendorName       = "Geomatys";
        version          = "1.0";
        pluginClassName  = "org.geotools.image.io.hdf.DefaultReader";
        this.dataName    = dataName;
        this.qualityName = qualityName;
    }

    /**
     * Retourne une description de ce format d'image.
     */
    @Override
    public String getDescription(final Locale locale) {
        return "Decodeur d'images HDF";
    }

    /**
     * Retourne le nom du fichier de qualit� � partir du nom de fichier de donn�es sp�cifi�.
     * L'impl�mentation par d�faut retourne toujours {@code null}, ce qui signifie qu'il n'y
     * a pas d'information sur la qualit� des donn�es. Les classes d�riv�es devraient red�finir
     * cette m�thode si un fichier d'indicateurs de qualit� est associ� � chaque image.
     *
     * @param  input Le nom du fichier de donn�es.
     * @return Le nom du fichier d'indicateurs de qualit�, ou {@code null} s'il n'y en a pas.
     */
    protected File getQualityFile(final File input) {
        return null;
    }

    /**
     * Retourne le type d'image que cr�era le d�codeur HDF. Ce type d'image comprend g�n�ralement
     * une palette de couleurs qui d�pend du type de donn�es (SST, CHL...), et donc de la classe
     * d�riv�e.
     *
     * @throws IOException si la palette de couleur n'a pas pu �tre obtenue.
     */
    protected abstract ImageTypeSpecifier getRawImageType() throws IOException;

    /**
     * V�rifie si le flot sp�cifi� semble �tre un fichier HDF lisible.
     * Cette m�thode tente simplement de lire les premiers octets du fichier.
     * La valeur retourn�e par cette m�thode n'est qu'� titre indicative.
     * {@code true} n'implique pas que la lecture va forc�ment r�ussir,
     * et {@code false} n'implique pas que la lecture va obligatoirement
     * �chouer.
     *
     * @param  source Source dont on veut tester la lisibilit�.
     * @return {@code true} si la source <u>semble</u> �tre lisible.
     * @throws IOException si une erreur est survenue lors de la lecture.
     */
    public boolean canDecodeInput(final Object source) throws IOException {
        if (source instanceof File) {
            final String filepath = ((File) source).getPath();
            final FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF4);
            if (fileFormat!=null && fileFormat.isThisType(filepath)) try {
                final FileFormat testFile = fileFormat.open(filepath, FileFormat.READ);
                if (testFile != null) {
                    if (testFile.open() >= 0) {
                        // TODO: obtenir un Dataset et v�rifier son nom ici.
                        testFile.close();
                        return true;
                    }
                }
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                /*
                 * Si la une op�ration a �chou�e, on consid�rera que le fichier n'est pas lisible.
                 * Le contrat de cette m�thode stipule que l'on doit retourner 'false' dans cette
                 * situation, et non pas faire suivre l'exception (sauf si elle est du type I/O).
                 */
            }
        }
        return false;
    }
}
