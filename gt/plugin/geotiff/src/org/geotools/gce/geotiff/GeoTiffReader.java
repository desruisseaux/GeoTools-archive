/*
 * NOTICE OF RELEASE TO THE PUBLIC DOMAIN
 *
 * This work was created by employees of the USDA Forest Service's
 * Fire Science Lab for internal use.  It is therefore ineligible for
 * copyright under title 17, section 105 of the United States Code.  You
 * may treat it as you would treat any public domain work: it may be used,
 * changed, copied, or redistributed, with or without permission of the
 * authors, for free or for compensation.  You may not claim exclusive
 * ownership of this code because it is already owned by everyone.  Use this
 * software entirely at your own risk.  No warranty of any kind is given. 
 *
 * A copy of 17-USC-105 should have accompanied this distribution in the file
 * 17USC105.html.  If not, you may access the law via the US Government's
 * public websites:
 *   - http://www.copyright.gov/title17/92chap1.html#105
 *   - http://www.gpoaccess.gov/uscode/  (enter "17USC105" in the search box.)
 */
package org.geotools.gce.geotiff;


//JAI ImageIO Tools dependencies
import com.sun.media.jai.operator.ImageReadDescriptor;

// Geotools dependencies 
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.factory.Hints;
import org.geotools.referencing.operation.GeneralMatrix;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;

// Geoapi dependencies
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;

//J2SE dependencies
import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import javax.imageio.metadata.IIOMetadata;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;


/**
 * <CODE>GeoTiffReader</CODE> is responsible for exposing the data and  the
 * Georeferencing metadata available to the Geotools library.
 *
 * @author Bryce Nordgren, USDA Forest Service
 */
public class GeoTiffReader implements GridCoverageReader {
    /**
     * Number of images read from file.  read() increments this counter and
     * hasMoreGridCoverages() accesses it.
     */
    private int imagesRead = 0;

    /**
     * This contains the maximum number of grid coverages in the file/stream.
     * Until multi-image files are supported, this is going to be 0 or 1.
     */
    private int maxImages = 0;
    private GeoTiffFormat creater = null;
    private Object source = null;
    private RenderedOp image = null;
    private GeoTiffIIOMetadataAdapter metadata = null;
    private Hints hints = null;

    /**
     * Creates a new instance of GeoTiffReader
     *
     * @param creater format object creating this reader
     * @param source the GeoTiff file
     * @param hints user-supplied hints
     */
    public GeoTiffReader(Format creater, Object source, Hints hints) {
        this.creater = (GeoTiffFormat) creater;
        this.source = source;
        this.image = JAI.create("ImageRead", source);
        this.hints = hints;

        IIOMetadata temp = (IIOMetadata) (image.getProperty(ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE));
        metadata = new GeoTiffIIOMetadataAdapter(temp);
    }

    /**
     * Static method checks the given file to ensure that:
     * 
     * <ul>
     * <li>
     * It's a TIFF file.
     * </li>
     * <li>
     * It contains GeoTIFF tags.
     * </li>
     * </ul>
     * 
     * @return TRUE if the file is a GeoTiff file.
     *
     * @param file The file to check
     */
    public static boolean isGeoTiffFile(File file) {
        RenderedOp img = JAI.create("ImageRead", file);

        if (img == null) {
            return false;
        }

        // Get the metadata object.
        Object metadataImage = img.getProperty(ImageReadDescriptor.PROPERTY_NAME_METADATA_IMAGE);

        if (!(metadataImage instanceof IIOMetadata)) {
            return false;
        }

        IIOMetadata check = (IIOMetadata) metadataImage;

        GeoTiffIIOMetadataAdapter metadata = new GeoTiffIIOMetadataAdapter(check);

        // does the GeoKey Directory exist? 
        boolean geoTiffFile = false;

        try {
            metadata.getGeoKeyDirectoryVersion();
            geoTiffFile = true;
        } catch (UnsupportedOperationException ue) {
            // this state is captured by the geoTiffFile flag == false
        }

        return geoTiffFile;
    }

    public void dispose() {
        image.dispose();
    }

    /**
     * No subnames.  Always returns null.
     *
     * @return null
     */
    public String getCurrentSubname() {
        return null;
    }

    public Format getFormat() {
        return creater;
    }

    public String[] getMetadataNames() throws IOException {
        throw new UnsupportedOperationException(
            "GeoTIFF reader doesn't support metadata manipulation yet");
    }

    public String getMetadataValue(String name)
        throws IOException, MetadataNameNotFoundException {
        throw new UnsupportedOperationException(
            "GeoTIFF reader doesn't support metadata manipulation yet");
    }

    public Object getSource() {
        return source;
    }

    /**
     * Returns true if another image remains to be read.  This module currently
     * only supports one image per TIFF file, so the first read will make this
     * method return false.
     *
     * @return true if another grid coverage remains to be read.
     */
    public boolean hasMoreGridCoverages() {
        return imagesRead < maxImages;
    }

    /**
     * Always returns null.  No subnames.
     *
     * @return null
     */
    public String[] listSubNames() {
        return null;
    }

    /**
     * This method reads in the TIFF image, constructs an appropriate CRS,
     * determines the math transform from raster to the CRS model, and
     * constructs a GridCoverage.
     *
     * @param params currently ignored, potentially may be used for hints.
     *
     * @return grid coverage represented by the image
     *
     * @throws IOException on any IO related troubles
     */
    public GridCoverage read(GeneralParameterValue[] params)
        throws IOException {
        // get the raster -> model transformation
        MathTransform r2m = getRasterToModel();

        // get the coordinate reference system
        GeoTiffCoordinateSystemAdapter gtcs = new GeoTiffCoordinateSystemAdapter(hints);
        gtcs.setMetadata(metadata);

        CoordinateReferenceSystem crs = gtcs.createCoordinateSystem();

        //rescale image if needed to enhane the dynamic
        image = rescaleIfNeeded(image);

        return new GridCoverage2D(((File) source).getName(), image, crs, r2m,
            null, null, null);
    }

    /**
     * If the dynamic range of the image data is not large, rescale it so
     * that it can be seen.
     *
     * @param image2 the image to rescale
     *
     * @return the image with an &quot;expanded&quot; dynamic range.
     */
    private RenderedOp rescaleIfNeeded(RenderedOp image2) {
        //do not rescale
        if (!this.creater.getReadParameters().parameter("Rescale").booleanValue()) {
            return image2;
        }

        //rescale the initial image in order
        //to expand the dynamic

        /** EXTREMA */

        // Set up the parameter block for the source image and
        // the constants
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(image2); // The source image
        pb.add(null); // The region of the image to scan
        pb.add(1); // The horizontal sampling rate
        pb.add(1); // The vertical sampling rate

        // Perform the extrema operation on the source image
        // Retrieve both the maximum and minimum pixel value
        double[][] extrema = (double[][]) JAI.create("extrema", pb).getProperty("extrema");

        /**
         * RESCALE
         */
        pb.removeSources();
        pb.removeParameters();

        //get the transfer type and set the levels for the dynamic
        int transferType = image2.getSampleModel().getTransferType();
        double dynamicAcme = 0.0;

        switch (transferType) {
        case DataBuffer.TYPE_BYTE:
            dynamicAcme = 255;

            break;

        case DataBuffer.TYPE_USHORT:
            dynamicAcme = 65535;

            break;

        case DataBuffer.TYPE_INT:
            dynamicAcme = 16777215;

            break;

        case DataBuffer.TYPE_DOUBLE:
            dynamicAcme = Double.MAX_VALUE;

            break;

        case DataBuffer.TYPE_FLOAT:
            dynamicAcme = Float.MAX_VALUE;

            break;
        }

        pb.addSource(image2);

        //rescaling each band
        double[] scale = new double[extrema[0].length];
        double[] offset = new double[extrema[0].length];

        for (int i = 0; i < extrema[0].length; i++) {
            scale[i] = dynamicAcme / (extrema[1][i] - extrema[0][i]);
            offset[i] = -((dynamicAcme * extrema[0][i]) / (extrema[1][i]
                - extrema[0][i]));
        }

        pb.add(scale);
        pb.add(offset);

        RenderedOp image2return = JAI.create("rescale", pb);

        return image2return;
    }

    /**
     * There are no guts to this function.  Only single-image TIFF files are
     * supported.
     *
     * @throws UnsupportedOperationException always
     */
    public void skip() {
        // add support for multi image TIFF files later.
        throw new UnsupportedOperationException(
            "No support for multi-image TIFF.");
    }

    private MathTransform getRasterToModel() throws GeoTiffException {
        double[] tiePoints = metadata.getModelTiePoints();
        double[] pixScales = metadata.getModelPixelScales();
        int numTiePoints = tiePoints.length / 6;
        MathTransform xform = null;

        if (numTiePoints == 1) {
            // we would really like two independent Linear transforms for just a 
            // scale and a
            // translate, but that functionality isn't quite tied in with the 
            // Geotools framework.  For now we put in an AffineTransform that 
            // doesn't rotate.
            GeneralMatrix gm = new GeneralMatrix(3); // identity
            double scaleX = pixScales[0];
            double scaleY = pixScales[1];
            double x = tiePoints[3]; // "model" space coordinates
            double y = tiePoints[4];
            double i = tiePoints[0]; // "raster" space coordinates (indicies)
            double j = tiePoints[1];

            // compute an "offset and scale" matrix
            gm.setElement(0, 0, scaleX);
            gm.setElement(0, 2, x - (scaleX * i));
            gm.setElement(1, 1, scaleY);
            gm.setElement(1, 2, y - (scaleY * j));

            // make it a LinearTransform
            xform = ProjectiveTransform.create(gm);
        } else {
            throw new GeoTiffException(metadata,
                "Unknown Raster to Model configuration.");
        }

        return xform;
    }
}
