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
import java.util.Locale;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import javax.imageio.IIOException;
import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.media.jai.util.Range;

// NetCDF dependencies
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;

// Geomatys dependencies
import org.geotools.util.NumberRange;
import org.geotools.image.io.PaletteFactory;
import org.geotools.image.io.FileImageReader;
import org.geotools.image.io.SampleConverter;
import org.geotools.image.io.IntegerConverter;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Base implementation for NetCDF image reader.
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
     *
     * @todo Uses {@link ucar.nc2.dataset.NetcdfDataset} instead.
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
     * The ranges for each image index, or {@code null} if unknown.
     */
    private Range[] ranges;

    /** 
     * Constructs a new NetCDF reader.
     *
     * @param spi The service provider.
     */
    public DefaultReader(final Spi spi) {
        super(spi);
        this.variableName = spi.variable;
        this.converter    = spi.converter;
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
     * Returns the range of values. The default implementation scans the file content.
     */
    public Range getExpectedRange(final int imageIndex, final int bandIndex) throws IOException {
        checkImageIndex(imageIndex);
        if (ranges == null) {
            ranges = new Range[imageIndex + 1];
        } else if (ranges.length <= imageIndex) {
            ranges = (Range[]) XArray.resize(ranges, imageIndex + 1);
        }
        Range range = ranges[imageIndex];
        if (range == null) {
            ranges[imageIndex] = range = computeExpectedRange(imageIndex);
        }
        return range;
    }

    /**
     * Computes the range of values.
     */
    private Range computeExpectedRange(final int imageIndex) throws IOException {
        prepareVariable(imageIndex);
        final Array    array = variable.read();
        final DataType type  = variable.getDataType();
        if (type.equals(DataType.BYTE)) {
            byte min = Byte.MAX_VALUE;
            byte max = Byte.MIN_VALUE;
            for (final IndexIterator it=array.getIndexIterator(); it.hasNext();) {
                final byte value = (byte) converter.convert(it.getByteNext());
                if (value < min) min = value;
                if (value > max) max = value;
            }
            if (min < max) {
                return new NumberRange(min, max);
            }
        } else if (type.equals(DataType.SHORT) || type.equals(DataType.INT)) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (final IndexIterator it=array.getIndexIterator(); it.hasNext();) {
                final int value = converter.convert(it.getIntNext());
                if (value < min) min = value;
                if (value > max) max = value;
            }
            if (min < max) {
                return new NumberRange(min, max);
            }
        } else if (type.equals(DataType.FLOAT)) {
            float min = Float.POSITIVE_INFINITY;
            float max = Float.NEGATIVE_INFINITY;
            for (final IndexIterator it=array.getIndexIterator(); it.hasNext();) {
                final float value = converter.convert(it.getFloatNext());
                if (value < min) min = value;
                if (value > max) max = value;
            }
            if (min < max) {
                return new NumberRange(min, max);
            }
        } else {
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for (final IndexIterator it=array.getIndexIterator(); it.hasNext();) {
                final double value = converter.convert(it.getDoubleNext());
                if (value < min) min = value;
                if (value > max) max = value;
            }
            if (min < max) {
                return new NumberRange(min, max);
            }
        }
        return null;
    }

    /**
     * Returns the data type which most closely represents the "raw" internal data of the image.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The data type.
     * @throws IOException If an error occurs reading the format information from the input source.
     */
    //@Override
    public int getRawDataType(final int imageIndex) throws IOException {
        prepareVariable(imageIndex);
        final DataType type = variable.getDataType();
        if (DataType.BOOLEAN.equals(type) || DataType.BYTE.equals(type)) {
            return DataBuffer.TYPE_BYTE;
        }
        if (DataType.CHAR.equals(type)) {
            return DataBuffer.TYPE_USHORT;
        }
        if (DataType.SHORT.equals(type)) {
            return variable.isUnsigned() ? DataBuffer.TYPE_USHORT : DataBuffer.TYPE_SHORT;
        }
        if (DataType.INT.equals(type)) {
            return DataBuffer.TYPE_INT;
        }
        if (DataType.FLOAT.equals(type)) {
            return DataBuffer.TYPE_FLOAT;
        }
        if (DataType.LONG.equals(type) || DataType.DOUBLE.equals(type)) {
            return DataBuffer.TYPE_DOUBLE;
        }
        return DataBuffer.TYPE_UNDEFINED;
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
        ranges = null;
        variable = null;
        if (file != null) {
            file.close();
            file = null;
        }
        super.close();
    }




    /**
     * The service provider for {@link DefaultReader}. This class requires the list of
     * {@linkplain Variable variables} to read. This list can be obtained by the following
     * instruction:
     *
     * <blockquote>
     * <code>java org.geotools.image.io.netcdf.Explorer</code> <var>fichier</var>
     * </blockquote>
     * 
     * @version $Id$
     * @author Antoine Hnawia
     * @author Martin Desruisseaux
     */
    public static class Spi extends FileImageReader.Spi {
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
         * The sample converter. Default to {@linkplain SampleConverter#IDENTITY identity}.
         */
        protected SampleConverter converter = SampleConverter.IDENTITY;

        /**
         * The name of a color palette to fetch from the {@linkplain PaletteFactory#getDefault
         * default palette factory}, or {@code null} if none.
         */
        protected String paletteName;

        /**
         * The color palette size. Valid only if {@link #paletteName} is non-null.
         */
        protected int paletteSize;

        /**
         * Constructs a service provider for the specified variable name.
         *
         * @param variable The default name of the {@linkplain Variable variable} to be read.
         */
        public Spi(final String variable) {
            super("NetCDF", "image/x-netcdf");
            names            = NAMES;
            suffixes         = SUFFIXES;
            vendorName       = "Geotools";
            version          = "2.4";
            pluginClassName  = "org.geotools.image.io.netcdf.DefaultReader";
            this.variable    = variable;
        }

        /**
         * Constructs a service provider for the specified variable name and color palette.
         *
         * @param variable The default name of the {@linkplain Variable variable} to be read.
         * @param palette  The name of a color palette to fetch from the
         *                 {@linkplain PaletteFactory#getDefault default palette factory}.
         * @param lower    The lowest sample value, inclusive.
         * @param upper    The highest sample value, exclusive.
         * @param padValue The pad value.
         *
         * @todo The pad value may be available in variable properties instead.
         */
        public Spi(final String variable, final String palette,
                   final int lower, final int upper, final int padValue)
        {
            this(variable);
            paletteName = palette;
            paletteSize = upper - lower;
            converter   = new IntegerConverter(padValue, 1-lower);
        }

        /**
         * Returns a description for this provider.
         */
        //@Override
        public String getDescription(final Locale locale) {
            return "NetCDF image decoder"; // TODO: localize
        }

        /**
         * Checks if the specified input seems to be a readeable NetCDF file.
         * This method is only for indication purpose. Current implementation
         * conservatively returns {@code false}.
         *
         * @todo Implements a more advanced check.
         */
        public boolean canDecodeInput(final Object source) throws IOException {
            return false;
        }

        /**
         * Constructs a NetCDF image reader.
         */
        public ImageReader createReaderInstance(final Object extension) throws IOException {
            return new DefaultReader(this);
        }

        /**
         * If a constant palette was specified to the constructor, returns a type specifier for it.
         */
        //@Override
        public ImageTypeSpecifier getForcedImageType(final int imageIndex) throws IOException {
            if (paletteName == null) {
                return super.getForcedImageType(imageIndex);
            }
            return PaletteFactory.getDefault().getPalettePadValueFirst(paletteName, paletteSize).
                    getImageTypeSpecifier();
        }
    }
}
