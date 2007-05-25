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

// NetCDF dependencies
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.ma2.Array;
import ucar.ma2.Range;
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
     * The default source bands to read from the NetCDF file.
     * Also the default destination bands in the buffered image.
     */
    private static final int[] DEFAULT_BANDS = new int[] {0};

    /**
     * The NetCDF file, or {@code null} if not yet open.
     *
     * @todo Uses {@link ucar.nc2.dataset.NetcdfDataset} instead.
     */
    private NetcdfFile file;

    /**
     * The name of the {@linkplain Variable variable} to be read in a NetCDF file.
     * The first name is assigned to image index 0, the second name to image index 1,
     * <cite>etc.</cite>.
     */
    private final String[] variableNames;

    /**
     * The image index of the current {@linkplain #variable variable}.
     */
    private int variableIndex;

    /**
     * The data from the NetCDF file.
     */
    private Variable variable;

    /**
     * The dimension in {@link #variable} to use as image width.
     * Will be computed by {@link #prepareVariable} as the last dimension.
     */
    private int xDimension;

    /**
     * The dimension in {@link #variable} to use as image height.
     * Will be computed by {@link #prepareVariable} as the 2th last dimension.
     */
    private int yDimension;

    /**
     * The dimension in {@link #variable} to use as bands.
     * Will be computed by {@link #prepareVariable} as the 3th last dimension.
     */
    private int zDimension;

    /**
     * The converter for sample values.
     */
    private final SampleConverter converter;

    /**
     * The ranges for each image index, or {@code null} if unknown.
     */
    private NumberRange[] ranges;

    /** 
     * Constructs a new NetCDF reader.
     *
     * @param spi The service provider.
     */
    public DefaultReader(final Spi spi) {
        super(spi);
        this.variableNames = spi.variables;
        this.converter     = spi.converter;
    }

    /**
     * Returns the number of images available from the current input source.
     *
     * @throws IllegalStateException if the input source has not been set.
     * @throws IOException if an error occurs reading the information from the input source.
     */
    //@Override
    public int getNumImages(final boolean allowSearch) throws IllegalStateException, IOException {
        ensureFileOpen();
        // TODO: consider returning the actual number of images in the file.
        return variableNames.length;
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
    public javax.media.jai.util.Range getExpectedRange(final int imageIndex, final int bandIndex) throws IOException {
        checkImageIndex(imageIndex);
        if (ranges == null) {
            ranges = new NumberRange[imageIndex + 1];
        } else if (ranges.length <= imageIndex) {
            ranges = (NumberRange[]) XArray.resize(ranges, imageIndex + 1);
        }
        NumberRange range = ranges[imageIndex];
        if (range == null) {
            ranges[imageIndex] = range = computeExpectedRange(imageIndex);
        }
        return range;
    }

    /**
     * Computes the range of values.
     */
    private NumberRange computeExpectedRange(final int imageIndex) throws IOException {
        prepareVariable(imageIndex);
        final DataType    type = variable.getDataType();
        final Array      array = variable.read();
        final IndexIterator it = array.getIndexIterator();
        if (type.equals(DataType.BYTE)) {
            byte min = Byte.MAX_VALUE;
            byte max = Byte.MIN_VALUE;
            while (it.hasNext()) {
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
            while (it.hasNext()) {
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
            while (it.hasNext()) {
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
            while (it.hasNext()) {
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
     * Returns parameters initialized with default values appropriate for this format.
     *
     * @return Parameters which may be used to control the decoding process using a set
     *         of default settings.
     */
    //@Override
    public ImageReadParam getDefaultReadParam() {
        final ImageReadParam param = super.getDefaultReadParam();
        param.setSourceBands     (DEFAULT_BANDS);
        param.setDestinationBands(DEFAULT_BANDS);
        return param;
    }

    /**
     * Ensures that the NetCDF file is open, but do not load any variable yet.
     */
    private void ensureFileOpen() throws IOException {
        if (file == null) {
            final File inputFile = getInputFile();
            file = NetcdfFile.open(inputFile.getPath()); // TODO: consider using NetcdfFileCache.acquire(...)
            if (file == null) {
                throw new FileNotFoundException(Errors.format(ErrorKeys.FILE_DOES_NOT_EXIST_$1, file));
            }
        }
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
        if (variable == null || variableIndex != imageIndex) {
            ensureFileOpen();
            final String variableName = variableNames[imageIndex];
            // TODO: consider using 'findVariable'
            //@SuppressWarnings("unchecked")
            final List/*<Variable>*/ variables = (List/*<Variable>*/) file.getVariables();
            for (final Iterator it=variables.iterator(); it.hasNext();) {
                final Variable v = (Variable) it.next();
                if (variableName.equalsIgnoreCase(v.getName().trim())) {
                    variable = v;
                    variableIndex = imageIndex;
                    final int rank = v.getRank();
                    xDimension = rank - 1;
                    yDimension = rank - 2;
                    zDimension = rank - 3;
                    return;
                }
            }
            throw new IIOException(Errors.format(
                    ErrorKeys.VARIABLE_NOT_FOUND_IN_FILE_$2, variableName, file));
        }
    }

    /**
     * Creates an image from the specified parameters.
     */
    public BufferedImage read(final int imageIndex, final ImageReadParam param) throws IOException {
        clearAbortRequest();
        prepareVariable(imageIndex);
        /*
         * Fetchs the parameters that are not already processed by utility
         * methods like 'getDestination' or 'computeRegions' (invoked below).
         */
        final int strideX, strideY;
        final int[] srcBands, dstBands;
        if (param != null) {
            strideX  = param.getSourceXSubsampling();
            strideY  = param.getSourceYSubsampling();
            srcBands = param.getSourceBands();
            dstBands = param.getDestinationBands();
        } else {
            strideX  = 1;
            strideY  = 1;
            srcBands = null;
            dstBands = null;
        }
        /*
         * Gets the destination image of appropriate size. We create it now
         * since it is a convenient way to get the number of destination bands.
         */
        final int            width  = variable.getDimension(xDimension).getLength();
        final int            height = variable.getDimension(yDimension).getLength();
        final BufferedImage  image  = getDestination(param, getImageTypes(imageIndex), width, height);
        final WritableRaster raster = image.getRaster();
        /*
         * Checks the band setting. If the NetCDF file is at least 3D, the
         * data along the 'z' dimension are considered as different bands.
         */
        final boolean hasZ    = (zDimension >= 0);
        final int numSrcBands = hasZ ? variable.getDimension(zDimension).getLength() : 1;
        final int numDstBands = raster.getNumBands();
        if (param != null) {
            // Do not test for 'param == null' since our default 'srcBands'
            // value is not the same than the one documented in Image I/O.
            checkReadParamBandSettings(param, numSrcBands, numDstBands);
        }
        /*
         * Computes the source region (in the NetCDF file) and the destination region
         * (in the buffered image). Copies those informations into UCAR Range structure.
         */
        final Rectangle  srcRegion = new Rectangle();
        final Rectangle destRegion = new Rectangle();
        computeRegions(param, width, height, image, srcRegion, destRegion);
        final Range[] ranges = new Range[variable.getRank()];
        for (int i=0; i<ranges.length; i++) {
            final int first, length, stride;
            if (i == xDimension) {
                first  = srcRegion.x;
                length = srcRegion.width;
                stride = strideX;
            } else if (i == yDimension) {
                first  = srcRegion.y;
                length = srcRegion.height;
                stride = strideY;
            } else {
                first  = 0;
                length = 1;
                stride = 1;
            }
            try {
                ranges[i] = new Range(first, first+length-1, stride);
            } catch (InvalidRangeException e) {
                throw netcdfFailure(e);
            }
        }
        final List sections = Range.toList(ranges);
        /*
         * Reads the requested sub-region only.
         */
        processImageStarted(imageIndex);
        final float toPercent = 100f / numDstBands;
        final int type = raster.getTransferType();
        final int xmin = destRegion.x;
        final int ymin = destRegion.y;
        final int xmax = destRegion.width  + xmin;
        final int ymax = destRegion.height + ymin;
        for (int zi=0; zi<numDstBands; zi++) {
            /*
             * Checks for abort request before the call to variable.read(...). We don't perform
             * this check in a deeper loop because the costly part is the call to 'read', which
             * can't process the abort request. The loop that copy the pixels is fast, so there
             * is few reasons to check for abort request there.
             */
            if (abortRequested()) {
                processReadAborted();
                return image;
            }
            final int srcBand = (srcBands == null) ? zi : srcBands[zi];
            final int dstBand = (dstBands == null) ? zi : dstBands[zi];
            final Array array;
            try {
                if (hasZ) {
                    ranges[zDimension] = new Range(srcBand, srcBand, 1);
                }
                array = variable.read(sections);
            } catch (InvalidRangeException e) {
                throw netcdfFailure(e);
            }
            final IndexIterator it = array.getIndexIterator();
            for (int y=ymax; --y>=ymin;) {
                for (int x=xmin; x<xmax; x++) {
                    switch (type) {
                        case DataBuffer.TYPE_DOUBLE: {
                            raster.setSample(x, y, dstBand, converter.convert(it.getDoubleNext()));
                            break;
                        }
                        case DataBuffer.TYPE_FLOAT: {
                            raster.setSample(x, y, dstBand, converter.convert(it.getFloatNext()));
                            break;
                        }
                        default: {
                            raster.setSample(x, y, dstBand, converter.convert(it.getIntNext()));
                            break;
                        }
                    }
                }
            }
            /*
             * Reports progress here, not in the deeper loop, because the costly part is the
             * call to 'variable.read(...)' which can't report progress.  The loop that copy
             * pixel values is fast, so reporting progress there would be pointless.
             */
            processImageProgress(zi * toPercent);
        }
        processImageComplete();
        return image;
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
         * The names of the {@linkplain Variable variable} to be read in a NetCDF file.
         */
        private final String[] variables;

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
         * Constructs a service provider for the specified variable names. The first name
         * is assigned to image index 0, the second name to image index 1, <cite>etc.</cite>.
         *
         * @param variable The names of the {@linkplain Variable variables} to be read.
         */
        public Spi(final String[] variables) {
            super("NetCDF", "image/x-netcdf");
            names            = NAMES;
            suffixes         = SUFFIXES;
            vendorName       = "Geotools";
            version          = "2.4";
            pluginClassName  = "org.geotools.image.io.netcdf.DefaultReader";
            this.variables   = (String[]) variables.clone();
        }

        /**
         * Constructs a service provider for the specified variable name and color palette.
         *
         * @param variable The names of the {@linkplain Variable variables} to be read.
         * @param palette  The name of a color palette to fetch from the
         *                 {@linkplain PaletteFactory#getDefault default palette factory}.
         * @param lower    The lowest sample value, inclusive.
         * @param upper    The highest sample value, exclusive.
         * @param padValue The pad value.
         *
         * @todo The pad value may be available in variable properties instead.
         */
        public Spi(final String[] variables, final String palette,
                   final int lower, final int upper, final int padValue)
        {
            this(variables);
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
