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
import java.util.Iterator;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.media.jai.util.Range;

// NetCDF dependencies
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;

// Geomatys dependencies
import org.geotools.image.io.SampleConverter;
import org.geotools.image.io.FileImageReader;


/**
 * Base implementation for NetCDF image reader. In most case, there is no need to subclass.
 * Subclassing {@link AbstractReaderSpi} should be suffisient.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class DefaultReader extends FileImageReader {
    /**
     * The NetCDF file, or {@code null} if not yet open.
     */
    private NetcdfFile file;

    /**
     * The name of the {@linkplain Variable variable} to be read in a NetCDF file.
     */
    private final String variableName;

    /**
     * The data from the NetCDF file.
     */
    private Variable variable;

    /**
     * The dimension in {@link #variable} to use as image width.
     * Default value is 0.
     */
    private int xDimension = 0;

    /**
     * The dimension in {@link #variable} to use as image height.
     * Default value is 1.
     */
    private int yDimension = 1;

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
     * Returns the dimension in the NetCDF {@linkplain Variable variable} where to fetch
     * column data. This is usually a format-specific parameter. The default value is 0.
     */
    public int getXDimension() {
        return xDimension;
    }

    /**
     * Set the dimension in the NetCDF {@linkplain Variable variable} where to fetch
     * column data. This is usually a format-specific parameter. The default value is 0.
     */
    public void setXDimension(final int x) {
        xDimension = x;
    }

    /**
     * Returns the dimension in the NetCDF {@linkplain Variable variable} where to fetch
     * row data. This is usually a format-specific parameter. The default value is 1.
     */
    public int getYDimension() {
        return yDimension;
    }

    /**
     * Set the dimension in the NetCDF {@linkplain Variable variable} where to fetch
     * row data. This is usually a format-specific parameter. The default value is 1.
     */
    public void setYDimension(final int y) {
        yDimension = y;
    }

    /**
     * Returns the image width.
     */
    public int getWidth(final int imageIndex) throws IOException {
        prepareVariable(imageIndex);
        return variable.getDimension(xDimension).getLength();
    }

    /**
     * Returns the image height.
     */
    public int getHeight(final int imageIndex) throws IOException {
        prepareVariable(imageIndex);
        return variable.getDimension(yDimension).getLength();
    }

    /**
     */
    public Range getExpectedRange(final int imageIndex, final int bandIndex) throws IOException {
        throw new UnsupportedOperationException(); // TODO
    }

    /**
     * Ensures that data are loaded in the {@link #variable}. If data are already loaded,
     * then this method do nothing.
     * 
     * @param   imageIndex The image index.
     * @throws  IndexOutOfBoundsException if the specified index is outside the expected range.
     * @throws  IllegalStateException If {@link #input} is not set.
     * @throws  IOException If the operation failed because of an I/O error.
     */
    private void prepareVariable(final int imageIndex) throws IOException {
        checkImageIndex(imageIndex);
        if (variable == null) {
            final File inputFile = getInputFile();
            file = NetcdfFile.open(inputFile.getPath()); // TODO: consider using NetcdfFileCache.acquire(...)
            if (file == null) {
                throw new FileNotFoundException(Errors.format(ErrorKeys.FILE_DOES_NOT_EXIST_$1, file));
            }
            //@SuppressWarnings("unchecked")
            final List/*<Variable>*/ variables = (List/*<Variable>*/) file.getVariables();
            for (final Iterator it=variables.iterator(); it.hasNext();) {
                final Variable v = (Variable) it.next();
                if (variableName.equalsIgnoreCase(v.getName().trim())) {
                    variable = v;
                    return;
                }
            }
            file.close();
            file = null;
            throw new IIOException(Errors.format(
                    ErrorKeys.VARIABLE_NOT_FOUND_IN_FILE_$2, variableName, file));
        }
    }

    /**
     * Creates an image from the specified parameters.
     */
    public BufferedImage read(final int imageIndex, final ImageReadParam param) throws IOException {
        clearAbortRequest();
        checkReadParamBandSettings(param, 1, 1);
        prepareVariable(imageIndex);
        final int            width  = variable.getDimension(xDimension).getLength();
        final int            height = variable.getDimension(yDimension).getLength();
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
         * Read the requested sub-region only.
         */
        final int[] shape  = variable.getShape();
        final int[] origin = new int[shape.length];
        origin[xDimension] = srcRegion.x;
        origin[yDimension] = srcRegion.y;
        shape [xDimension] = srcRegion.width;
        shape [yDimension] = srcRegion.height;
        final Array array;
        try {
            array = variable.read(origin, shape);
        } catch (InvalidRangeException e) {
            throw netcdfFailure(e);
        }
        final Index index = array.getIndex();
        final float toPercent = 100f / height;
        final int type = raster.getTransferType();
        final int xmin = destRegion.x;
        final int ymin = destRegion.y;
        final int xmax = destRegion.width  + xmin;
        final int ymax = destRegion.height + ymin;
        for (int yi=0,y=ymax; --y>=ymin;) {
            set(index, yDimension, yi);
            for (int xi=0,x=xmin; x<xmax; x++) {
                set(index, xDimension, xi);
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
     * Sets the index value at the specified dimension.
     */
    private static void set(final Index index, final int dimension, final int value) {
        switch(dimension) {
            case 0: index.set0(value); break;
            case 1: index.set1(value); break;
            case 2: index.set2(value); break;
            case 3: index.set3(value); break;
            case 4: index.set4(value); break;
            case 5: index.set5(value); break;
            case 6: index.set6(value); break;
            default: throw new IndexOutOfBoundsException(String.valueOf(dimension));
        }
    }

    /**
     * Wraps a generic exception into a {@link IIOException}.
     */
    private IIOException netcdfFailure(final Exception e) throws IOException {
        return new IIOException(Errors.format(ErrorKeys.CANT_READ_$1, getInputFile()), e);
    }

    /**
     * Closes the NetCDF file.
     */
    //@Override
    protected void close() throws IOException {
        variable = null;
        if (file != null) {
            file.close();
            file = null;
        }
        super.close();
    }
}
