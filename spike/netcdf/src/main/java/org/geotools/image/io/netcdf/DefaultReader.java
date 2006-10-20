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
import java.util.List;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;

// NetCDF dependencies
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.resources.i18n.Errors;
import static org.geotools.resources.i18n.ErrorKeys.*;

// Geomatys dependencies
import org.geotools.image.io.SampleConverter;
import org.geotools.image.io.FileBasedReader;
import org.geotools.image.io.FileBasedReaderSpi;


/**
 * Impl�mentation par d�faut des d�codeurs d'images au format NetCDF. Dans la plupart des
 * cas, il ne sera pas n�cessaire de cr�er des classes d�riv�es. Des classes d�riv�es
 * de {@link AbstractReaderSpi} suffisent.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class DefaultReader extends FileBasedReader {
    /**
     * Dimension correspondant aux colonnes. Doit �tre constant, car il y aura des appels
     * � {@link Array#set0} cod�s en dur.
     */
    private int X_DIMENSION = 3;

    /**
     * Dimension correspondant aux lignes. Doit �tre constant, car il y aura des appels
     * � {@link Array#set1} cod�s en dur.
     */
    private int Y_DIMENSION = 2;

    /**
     * Le fichier NetCDF, ou {@code null} s'il n'a pas encore �t� ouvert.
     */
    private NetcdfFile file;

    /**
     * L'ensemble des donn�es du ph�nom�ne �tudi�.
     */
    private Variable variable;

    /**
     * The name of the {@linkplain Variable variable} to be read in a NetCDF file.
     */
    private final String variableName;

    /**
     * The converter for sample values.
     */
    private final SampleConverter converter;

    /** 
     * Constructs a new NetCDF reader.
     *
     * @param spi       The service provider.
     * @param variable  The default name of the {@linkplain Variable variable} to be read.
     */
    public DefaultReader(final AbstractReaderSpi spi, final String variableName) {
        super(spi);
        this.variableName = variableName;
        this.converter    = spi;
    }

    /**
     * Sp�cifie la source des donn�es � utiliser en entr�e.
     * Cette source devrait �tre de pr�f�rence un objet de type {@link File}.
     */
    @Override
    public void setInput(final Object input, final boolean seekForwardOnly, final boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);
        if (file != null) {
            try {
                file.close();
            } catch (IOException e) {
                LOGGER.warning("Echec lors de la fermeture du fichier pr�c�dent.");
                /*
                 * On continue. Ce n'est qu'un avertissement car de toute fa�on on
                 * n'utilisera plus ce fichier.
                 */
            }
            file = null;
            variable = null;
        }
    }

    /**
     * Retourne la largeur de l'image.
     */
    public int getWidth(final int imageIndex) throws IOException {
        prepareVariable(imageIndex);
        return variable.getDimension(X_DIMENSION).getLength();
    }

    /**
     * Retourne la hauteur de l'image.
     */
    public int getHeight(final int imageIndex) throws IOException {
        prepareVariable(imageIndex);
        return variable.getDimension(Y_DIMENSION).getLength();
    }
    
    public void setXDimension(final int x) {
        this.X_DIMENSION = x;
    }
    
    public void setYDimension(final int y) {
        this.Y_DIMENSION = y;
    }
    
    private static void set(final Index index, final int dimension, final int value) {
        switch(dimension) {
            case 0: index.set0(value); break;
            case 1: index.set1(value); break;
            case 2: index.set2(value); break;
            case 3: index.set3(value); break;
            case 4: index.set4(value); break;
            case 5: index.set5(value); break;
            case 6: index.set6(value); break;
        }
    }
    
    /**
     * V�rifie que les donn�es ont bien �t� charg�e dans {@link #variable} pour l'image sp�cifi�e.
     * Si les donn�es ont d�j� �t� charg�e lors d'un appel pr�c�dent, alors cette m�thode ne fait
     * rien.
     * 
     * @param   imageIndex L'index de l'image � traiter.
     * @throws  IndexOutOfBoundsException Si {@code indexImage} est diff�rent de 0,
     *          car on consid�re qu'il n'y a qu'une image par fichier HDF.
     * @throws  IllegalStateException Si le champ {@link #input} n'a pas �t� initialis� via
     *          {@link #setInput setInput(...)}.
     * @throws  IIOException Si le fichier NetCDF ne semble pas correct.
     * @throws  IOException Si la lecture a �chou�e pour une autre raison.
     */
    private void prepareVariable(final int imageIndex) throws IOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException(Errors.format(ILLEGAL_ARGUMENT_$2, "imageIndex", imageIndex));
        }
        if (variable == null) {
            final File inputFile = getInputFile();
            file = new NetcdfFile(inputFile.getPath()); // TODO: consider using NetcdfFileCache.acquire(...)
            @SuppressWarnings("unchecked")
            final List<Variable> variables = (List<Variable>) file.getVariables();
            for (final Variable v : variables) {
                if (variableName.equalsIgnoreCase(v.getName().trim())) {
                    variable = v;
                    return;
                }
            }
            file.close();
            file = null;
            throw new IIOException("La variable \"" + variableName + "\" n'a pas �t� trouv�e.");
        }
    }

    /**
     * Construit une image � partir des param�tre de lecture sp�cifi�s.
     *
     * @throws  IOException Si la lecture de l'image a �chou�e.
     */
    public BufferedImage read(final int imageIndex, final ImageReadParam param) throws IOException {
        clearAbortRequest();
        checkReadParamBandSettings(param, 1, 1);
        prepareVariable(imageIndex);
        final int            width  = variable.getDimension(X_DIMENSION).getLength();
        final int            height = variable.getDimension(Y_DIMENSION).getLength();
        final BufferedImage  image  = getDestination(param, getImageTypes(imageIndex), width, height);
        final WritableRaster raster = image.getRaster();
        final Rectangle   srcRegion = new Rectangle();
        final Rectangle  destRegion = new Rectangle();
        final int strideX, strideY;
        if (param != null) {
            strideX = param.getSourceXSubsampling();
            strideY = param.getSourceYSubsampling();
        } else {
            strideX = 1;
            strideY = 1;
        }
        computeRegions(param, width, height, image, srcRegion, destRegion);
        processImageStarted(imageIndex);
        /*
         * Proc�de � la lecture de la sous-r�gion demand�e par l'utilisateur.
         */
        final int[] shape  = variable.getShape();
        final int[] origin = new int[shape.length];
        origin [X_DIMENSION] = srcRegion.x;
        origin [Y_DIMENSION] = srcRegion.y;
        shape  [X_DIMENSION] = srcRegion.width;
        shape  [Y_DIMENSION] = srcRegion.height;
        final Array array;
        try {
            array = variable.read(origin, shape);
        } catch (InvalidRangeException e) {
            throw netcdfFailure(e);
        }
        final Index index = array.getIndex();
        final float toPercent = 100f / height;
        final int type = raster.getTransferType();
        final int xmax = destRegion.x + destRegion.width;
        final int ymax = destRegion.y + destRegion.height;
        for (int yi=0,y=destRegion.y+destRegion.height; --y>=destRegion.y;) {
            set(index, Y_DIMENSION, yi);
            for (int xi=0,x=destRegion.x; x<xmax; x++) {
                set(index, X_DIMENSION, xi);
                switch (type) {
                    case DataBuffer.TYPE_DOUBLE: {
                        raster.setSample(x, y, 0, converter.convert(array.getDouble(index)));
                        break;
                    }
                    case DataBuffer.TYPE_FLOAT: {
                        raster.setSample(x, y, 0, converter.convert(array.getFloat(index)));
                        break;
                    }
                    default: {
                        raster.setSample(x, y, 0, converter.convert(array.getInt(index)));
                        break;
                    }
                }
                xi += strideX;
            }
            yi += strideY;
            processImageProgress(yi * toPercent);
            if (abortRequested()) {
                processReadAborted();
                return image;
            }
        }
        processImageComplete();
        return image;
    }

    /**
     * Lance une exception un peu plus explicite lorsqu'une erreur est survenue
     * lors de la lecture d'un fichier NetCDF.
     */
    private static IIOException netcdfFailure(final Exception e) {
        return new IIOException("Echec lors de la lecture du fichier NetCDF", e);
    }
}
