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

import java.util.ArrayList;
import java.util.List;
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
import javax.imageio.metadata.IIOMetadata;

// NetCDF dependencies
import ucar.nc2.Variable;
import ucar.ma2.Array;
import ucar.ma2.Range;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.dataset.CoordSysBuilder;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.util.CancelTask;

// Geotools dependencies
import org.geotools.util.NumberRange;
import org.geotools.image.io.PaletteFactory;
import org.geotools.image.io.FileImageReader;
import org.geotools.image.io.SampleConverter;
import org.geotools.image.io.IntegerConverter;
import org.geotools.image.io.metadata.GeographicMetadata;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Base implementation for NetCDF image reader. Pixels are assumed organized according the COARDS
 * convention (a precursor of <A HREF="http://www.cfconventions.org/">CF Metadata conventions</A>),
 * i.e. in (<var>t</var>,<var>z</var>,<var>y</var>,<var>x</var>) order, where <var>x</var> varies
 * faster. The image is created from the two last dimensions (<var>x</var>,<var>y</var>) and the
 * <var>z</var> is taken as the bands. Additional dimensions like <var>t</var> are ignored.
 * <p>
 * Users should select the <var>z</var> value using {@link ImageReadParam#setSourceBands}. If no
 * band is selected, the default selection is the first band (0) only. Note that this is different
 * than the usual Image I/O default, which is all bands.
 *
 * @since 2.4
 * @source $URL$
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class NetcdfImageReader extends FileImageReader implements CancelTask {
    /**
     * The default source bands to read from the NetCDF file.
     * Also the default destination bands in the buffered image.
     */
    private static final int[] DEFAULT_BANDS = new int[] {0};

    /**
     * The dimension <strong>relative to the rank</strong> in {@link #variable} to use as image
     * width. The actual dimension is {@code variable.getRank() - X_DIMENSION}. Is hard-coded
     * because the loop in the {@code read} method expects this order.
     */
    private static final int X_DIMENSION = 1;

    /**
     * The dimension <strong>relative to the rank</strong> in {@link #variable} to use as image
     * height. The actual dimension is {@code variable.getRank() - Y_DIMENSION}. Is hard-coded
     * because the loop in the {@code read} method expects this order.
     */
    private static final int Y_DIMENSION = 2;

    /**
     * The dimension <strong>relative to the rank</strong> in {@link #variable} to use as image
     * bands. The actual dimension is {@code variable.getRank() - Z_DIMENSION}. Is hard-coded
     * because the loop in the {@code read} method expects this order.
     *
     * @todo In this particular case, the "hard constant" could be relaxed into a modifiable
     *       parameter.
     */
    private static final int Z_DIMENSION = 3;

    /**
     * The NetCDF file, or {@code null} if not yet open.
     */
    private NetcdfDataset file;

    /**
     * The name of the {@linkplain Variable variable} to be read in a NetCDF file.
     * The first name is assigned to image index 0, the second name to image index 1,
     * <cite>etc.</cite>.
     */
    private String[] variableNames;

    /**
     * The image index of the current {@linkplain #variable variable}.
     */
    private int variableIndex;

    /**
     * The data from the NetCDF file. Should be an instance of {@link VariableDS},
     * but we will avoid casting before needed (in case we got something else for
     * some reason).
     */
    private Variable variable;

    /**
     * The converter for sample values.
     */
    private final SampleConverter converter;

    /**
     * The ranges for each image index, or {@code null} if unknown.
     */
    private NumberRange[] ranges;

    /**
     * The last error from the NetCDF library.
     */
    private String lastError;

    /**
     * {@code true} if {@link CoordSysBuilder#addCoordinateSystems} has been invoked
     * for current file.
     */
    private boolean metadataLoaded;

    /**
     * The stream metadata. Will be created only when first needed.
     */
    private IIOMetadata streamMetadata;

    /**
     * The current image metadata. Will be created only when first needed.
     */
    private IIOMetadata imageMetadata;

    /** 
     * Constructs a new NetCDF reader.
     *
     * @param spi The service provider.
     */
    public NetcdfImageReader(final Spi spi) {
        super(spi);
        this.variableNames = null;
        this.converter     = spi.converter;
    }

    /**
     * Returns the number of images available from the current input source.
     *
     * @throws IllegalStateException if the input source has not been set.
     * @throws IOException if an error occurs reading the information from the input source.
     */
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
        return variable.getDimension(variable.getRank() - X_DIMENSION).getLength();
    }

    /**
     * Returns the image height.
     */
    public int getHeight(final int imageIndex) throws IOException {
        prepareVariable(imageIndex);
        return variable.getDimension(variable.getRank() - Y_DIMENSION).getLength();
    }

    /**
     * Returns the range of values. The default implementation scans the file content.
     */
    public NumberRange getExpectedRange(final int imageIndex, final int bandIndex) throws IOException {
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
     * Ensures that metadata are loaded.
     */
    private void ensureMetadataLoaded() throws IOException {
        if (!metadataLoaded) {
            CoordSysBuilder.addCoordinateSystems(file, this);
            metadataLoaded = true;
        }
    }

    /**
     * Returns the metadata associated with the input source as a whole.
     */
    public IIOMetadata getStreamMetadata() throws IOException {
        if (streamMetadata == null && !ignoreMetadata) {
            ensureFileOpen();
            ensureMetadataLoaded();
            streamMetadata = createMetadata(file);
        }
        return streamMetadata;
    }

    /**
     * Returns the metadata associated with the image at the specified index.
     */
    public IIOMetadata getImageMetadata(final int imageIndex) throws IOException {
        if (imageMetadata == null && !ignoreMetadata) {
            prepareVariable(imageIndex);
            if (variable instanceof VariableDS) {
                ensureMetadataLoaded();
                imageMetadata = createMetadata((VariableDS) variable);
            }
        }
        return imageMetadata;
    }

    /**
     * Creates metadata for the specified NetCDF file. This method is invoked automatically
     * by {@link #getStreamMetadata} when first needed. The default implementation returns an
     * instance of {@link NetcdfMetadata}. Subclasses can override this method in order to create
     * a more specific set of metadata.
     */
    protected IIOMetadata createMetadata(final NetcdfDataset file) throws IOException {
        return new NetcdfMetadata(this, file);
    }

    /**
     * Creates metadata for the specified NetCDF variable. This method is invoked automatically
     * by {@link #getImageMetadata} when first needed. The default implementation returns an
     * instance of {@link NetcdfMetadata}. Subclasses can override this method in order to create
     * a more specific set of metadata.
     */
    protected IIOMetadata createMetadata(final VariableDS variable) throws IOException {
        return new NetcdfMetadata(this, variable);
    }

    /**
     * Returns the data type which most closely represents the "raw" internal data of the image.
     *
     * @param  imageIndex The index of the image to be queried.
     * @return The data type.
     * @throws IOException If an error occurs reading the format information from the input source.
     */
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
    public ImageReadParam getDefaultReadParam() {
        final ImageReadParam param = super.getDefaultReadParam();
        param.setSourceBands     (DEFAULT_BANDS);
        param.setDestinationBands(DEFAULT_BANDS);
        return param;
    }
    
    /**
     * Test if the current variable in the list is a dimension of an other variable in the list.
     * If it the case, this variable will be omitted and this function will return true.
     * If not, the current variable is a {@code sample dimension} and the function will return
     * false.
     *
     * @param index The index in the list for the variable to test.
     * @param listVar The list of variables.
     * @return True if the selected variables is a dimension for an other variable in the list. 
     * False otherwise.
     */
    private boolean isPresentInOtherVarDimension(final int index, final List listVar) {
        final VariableDS reference = (VariableDS) listVar.get(index);
        for (int i=0; i<listVar.size(); i++) {
            // One does not compare the same element in the list.
            if (i != index) {
                final VariableDS var = (VariableDS) listVar.get(i);
                for (int j=0; j<var.getDimensions().size(); j++) {
                    if (var.getDimension(j).getName().equals(reference.getName())) {
                        return true;
                    }
                }                
            }            
        }
        return false;
    }
    
    /**
     * Ensures that the NetCDF file is open, but do not load any variable yet.
     */
    private void ensureFileOpen() throws IOException {
        if (file == null) {
            /*
             * Clears the 'abort' flag here (instead of in 'read' method only) because
             * we pass this ImageReader instance to the NetCDF DataSet as a CancelTask.
             */
            lastError = null;
            clearAbortRequest();
            final File inputFile = getInputFile();
            // TODO: consider using NetcdfDatasetCache.acquire(...) below.
            file = NetcdfDataset.openDataset(inputFile.getPath(), false, this);
            if (file == null) {
                throw new FileNotFoundException(Errors.format(
                        ErrorKeys.FILE_DOES_NOT_EXIST_$1, inputFile));
            }
            if (variableNames == null) {
                /*
                 * ListVar is a list that contains all the variables found in the NetcdfDataset.
                 * The list result is composed by variables that are not a {@code dimension} for an 
                 * other variable in the list of variables.
                 * For example, "longitude" may be a variable found in the NetcdfDataset. But it
                 * also is a {@code Dimension} for the variable "temperature". So we don't keep this
                 * variable. On the other hand, "temperature" is not a {@code Dimension} for any other
                 * variables, we keep it as a good value.
                 */
                List listVar = file.getVariables();
                List result = new ArrayList();
                for (int i=0; i<listVar.size(); i++) {
                    if (!isPresentInOtherVarDimension(i, listVar)) {
                        result.add(((VariableDS)listVar.get(i)).getName());
                    }
                }
                /*
                 * The method java.util.List.toArray(<T> a) takes an array in parameter that has to
                 * be not null, because it will test the length of this array to determine if it has
                 * to create a new array or use the specified one.
                 * So we have to instanciate it, giving the right length for this array. 
                 */
                variableNames = new String[result.size()];
                result.toArray(variableNames);
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
            final String name = variableNames[imageIndex];
            final Variable candidate = file.findVariable(name);
            if (candidate == null) {
                throw new IIOException(Errors.format(
                        ErrorKeys.VARIABLE_NOT_FOUND_IN_FILE_$2, name, file.getLocation()));
            }
            final int rank = candidate.getRank();
            if (rank < Math.max(X_DIMENSION, Y_DIMENSION)) {
                throw new IIOException(Errors.format(
                        ErrorKeys.NOT_TWO_DIMENSIONAL_$1, new Integer(rank)));
            }
            variable      = candidate;
            variableIndex = imageIndex;
            imageMetadata = null;
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
        final int            rank   = variable.getRank();
        final int            width  = variable.getDimension(rank - X_DIMENSION).getLength();
        final int            height = variable.getDimension(rank - Y_DIMENSION).getLength();
        final BufferedImage  image  = getDestination(param, getImageTypes(imageIndex), width, height);
        final WritableRaster raster = image.getRaster();
        /*
         * Checks the band setting. If the NetCDF file is at least 3D, the
         * data along the 'z' dimension are considered as different bands.
         */
        final boolean hasZ    = (rank >= Z_DIMENSION);
        final int numSrcBands = hasZ ? variable.getDimension(rank - Z_DIMENSION).getLength() : 1;
        final int numDstBands = raster.getNumBands();
        if (param != null) {
            // Do not test when 'param == null' since our default 'srcBands'
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
        flipVertically(param, height, srcRegion);
        final Range[] ranges = new Range[rank];
        for (int i=0; i<ranges.length; i++) {
            final int first, length, stride;
            switch (rank - i) {
                case X_DIMENSION: {
                    first  = srcRegion.x;
                    length = srcRegion.width;
                    stride = strideX;
                    break;
                }
                case Y_DIMENSION: {
                    first  = srcRegion.y;
                    length = srcRegion.height;
                    stride = strideY;
                    break;
                }
                default: {
                    first  = 0;
                    length = 1;
                    stride = 1;
                    break;
                }
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
            final int srcBand = (srcBands == null) ? zi : srcBands[zi];
            final int dstBand = (dstBands == null) ? zi : dstBands[zi];
            final Array array;
            try {
                if (hasZ) {
                    ranges[rank - Z_DIMENSION] = new Range(srcBand, srcBand, 1);
                    // No need to update 'sections' since it wraps directly the 'ranges' array.
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
             * Checks for abort requests after reading. It would be a waste of a potentially
             * good image (maybe the abort request occured after we just finished the reading)
             * if we didn't implemented the 'isCancel()' method. But because of the later, which
             * is checked by the NetCDF library, we can't assume that the image is complete.
             */
            if (abortRequested()) {
                processReadAborted();
                return image;
            }
            /*
             * Reports progress here, not in the deeper loop, because the costly part is the
             * call to 'variable.read(...)' which can't report progress.  The loop that copy
             * pixel values is fast, so reporting progress there would be pointless.
             */
            processImageProgress(zi * toPercent);
        }
        if (lastError != null) {
            throw new IIOException(lastError);
        }
        processImageComplete();
        return image;
    }

    /**
     * Wraps a generic exception into a {@link IIOException}.
     */
    private IIOException netcdfFailure(final Exception e) throws IOException {
        return new IIOException(Errors.format(ErrorKeys.CANT_READ_$1, file.getLocation()), e);
    }

    /**
     * Invoked by the NetCDF library during read operation in order to check if the task has
     * been canceled.
     */
    public boolean isCancel() {
        return abortRequested();
    }

    /**
     * Invoked by the NetCDF library when an error occured during the read operation.
     */
    public void setError(final String message) {
        lastError = message;
    }
    
    /**
     *
     */
    public void setVariables(final String[] var) {
        variableNames = var;
    }
    
    /**
     * Closes the NetCDF file.
     */
    protected void close() throws IOException {
        metadataLoaded = false;
        streamMetadata = null;
        imageMetadata  = null;
        lastError      = null;
        ranges         = null;
        variable       = null;
        if (file != null) {
            file.close();
            file = null;
        }
        super.close();
    }



    /**
     * The service provider for {@link NetcdfImageReader}. This class requires the list of
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
         */
        public Spi() {
            super("NetCDF", "image/x-netcdf");
            names            = NAMES;
            suffixes         = SUFFIXES;
            vendorName       = "Geotools";
            version          = "2.4";
            pluginClassName  = "org.geotools.image.io.netcdf.NetcdfImageReader";
        }

        /**
         * Constructs a service provider for the specified variable name and color palette.
         *
         * @param palette  The name of a color palette to fetch from the
         *                 {@linkplain PaletteFactory#getDefault default palette factory}.
         * @param lower    The lowest sample value, inclusive.
         * @param upper    The highest sample value, exclusive.
         * @param padValue The pad value.
         *
         * @todo The pad value may be available in variable properties instead.
         */
        public Spi(final String palette, final int lower, 
                   final int upper, final int padValue)
        {
            this();
            paletteName = palette;
            paletteSize = upper - lower;
            converter   = new IntegerConverter(padValue, 1-lower);
        }

        /**
         * Returns a description for this provider.
         *
         * @todo Localize
         */
        public String getDescription(final Locale locale) {
            return "NetCDF image decoder";
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
            return new NetcdfImageReader(this);
        }

        /**
         * If a constant palette was specified to the constructor, returns a type specifier for it.
         * Otherwise returns {@code null}.
         */
        public ImageTypeSpecifier getForcedImageType(final int imageIndex) throws IOException {
            if (paletteName == null) {
                return super.getForcedImageType(imageIndex);
            }
            return PaletteFactory.getDefault().getPalettePadValueFirst(paletteName, paletteSize).
                    getImageTypeSpecifier();
        }
    }
}
