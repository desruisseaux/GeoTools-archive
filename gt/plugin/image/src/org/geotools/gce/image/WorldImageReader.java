/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.gce.image;

import com.sun.media.imageio.stream.FileChannelImageInputStream;
import org.geotools.coverage.Category;
import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.Parameter;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.LinearTransform1D;
import org.geotools.util.NumberRange;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;


/**
 * Reads a GridCoverage from a given source. WorldImage sources only support
 * one GridCoverage so hasMoreGridCoverages() will return true until the only
 * GridCoverage is read. No metadata is currently supported, so all related
 * methods return null. In the early future we will start (hopefully
 * supporting them).
 *
 * @author simone giannecchini
 * @author alessio fabiani
 * @author rgould
 */
public class WorldImageReader implements GridCoverageReader {
    /** crs for this coverage */
    private CoordinateReferenceSystem crs = null;

    /** Image for this coverage */
    private RenderedImage image = null;

    /** Envelope read from file */
    private Envelope readEnvelope = null;

    /** Coverage name */
    private String coverageName = null;

    /** Format for this reader */
    private Format format = new WorldImageFormat();

    /** Source to read from */
    private Object source;

    /** Number of coverages left */
    private boolean gridLeft = true;

    /** envelope requested from the user */
    private Envelope requestedEnvelope;

    /**
     * Class constructor. Construct a new ImageWorldReader to read a
     * GridCoverage from the source object. The source must point to the
     * raster file itself, not the world file. If the source is a Java URL it
     * checks if it is ponting to a file and if so it converts the url into a
     * file.
     *
     * @param source The source of a GridCoverage, can be a File, a URL or an
     *        input stream.
     */
    public WorldImageReader(Object source) {
        this.source = source;

        if (source instanceof File) { //File

            return; //do nothing if it is a file
        } else if (source instanceof URL) { //URL that point to a file

            if (((URL) source).getProtocol().compareToIgnoreCase("file") == 0) {
                this.source = new File(((URL) source).getPath());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#getFormat()
     */

    /**
     * Returns the format that this Reader accepts.
     *
     * @return a new WorldImageFormat class
     */
    public Format getFormat() {
        return this.format;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#getSource()
     */

    /**
     * Returns the source object containing the GridCoverage. Note that it
     * points to the raster, and not the world file.
     *
     * @return the source object containing the GridCoverage.
     */
    public Object getSource() {
        return source;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#getMetadataNames()
     */

    /**
     * Metadata is not suported. Returns null.
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public String[] getMetadataNames() throws IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#getMetadataValue(java.lang.String)
     */

    /**
     * Metadata is not supported. Returns null.
     *
     * @param name DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws MetadataNameNotFoundException DOCUMENT ME!
     */
    public String getMetadataValue(String name)
        throws IOException, MetadataNameNotFoundException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#listSubNames()
     */

    /**
     * WorldImage GridCoverages are not named. Returns null.
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public String[] listSubNames() throws IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#getCurrentSubname()
     */

    /**
     * WorldImage GridCoverages are not named. Returns null.
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public String getCurrentSubname() throws IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#hasMoreGridCoverages()
     */

    /**
     * Returns true until read has been called, as World Image files only
     * support one GridCoverage.
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public boolean hasMoreGridCoverages() throws IOException {
        return gridLeft;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
     */

    /**
     * Reads an image from a source stream. Loads an image from a source
     * stream, then loads the values from the world file and constructs a new
     * GridCoverage from this information. When reading from a remote stream
     * we do not look for a world fiel but we suppose those information comes
     * from a different way (xml, gml, pigeon?)
     *
     * @param parameters WorldImageReader supports no parameters, it just
     *        ignores them.
     *
     * @return a new GridCoverage read from the source.
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public GridCoverage read(GeneralParameterValue[] parameters)
        throws IllegalArgumentException, IOException {
        //do we have paramters to use for reading from the specified source
        if (parameters != null) {
            final int length = parameters.length;

            for (int i = 0; i < length; i++) {
                if ((((Parameter) parameters[i]).getDescriptor().getName()
                          .getCode().equalsIgnoreCase("RequestedEnvelope"))) {
                    requestedEnvelope = (Envelope) ((Parameter) parameters[0])
                        .getValue();
                } else
                //they will be ignored if we will find a world file
                if (((Parameter) parameters[i]).getDescriptor().getName()
                         .getCode().equalsIgnoreCase("crs")) {
                    this.format.getReadParameters().parameter("crs").setValue(((Parameter) parameters[i])
                        .getValue());
                } else if (((Parameter) parameters[i]).getDescriptor().getName()
                                .getCode().equalsIgnoreCase("envelope")) {
                    this.format.getReadParameters().parameter("crs").setValue(((Parameter) parameters[i])
                        .getValue());
                }
            }
        }

        //check for a wms request
        if (!WMSRequest(requestedEnvelope)) {
            //getting the crs.
            readCRS();

            //getting envelope
            readEnvelope = (Envelope) this.format.getReadParameters()
                                                 .parameter("envelope")
                                                 .getValue();
            ((GeneralEnvelope) readEnvelope).setCoordinateReferenceSystem(crs);

            //reading source image
            readSourceImage();

            //are we reading from a file?
            //in such a case we will look for the associated world file
            if (source instanceof File) {
                prepareWorldImageEnvelope();
            } else {
                //assuming it is an  url or an input stream
                //well it seems we are not reading from a file
                //therefore we need the parameters
                //reading the image as given
                if (source instanceof URL
                        && (((URL) source).getProtocol() == "file")) {
                    coverageName = ((URL) source).getFile();
                }

                if ((readEnvelope == null) || (crs == null)) {
                    throw new IllegalArgumentException(
                        "To read from a a source which is not a file please provided read parameters!");
                }
            }
        }

        //no more grid left
        gridLeft = false;

        //creating the coverage
        return createCoverage();
    }

    /**
     * This method is sued to check if we are connecting directly to a WMS
     * with a getmap request. In such a case we skip reading all the parameters
     * we can read from this http string.
     *
     * @return true if we are dealing with a WMS request, false otherwise.
     */
    private boolean WMSRequest(Envelope requestedEnvelope) {
    	//TODO do we need the requested envelope?
        if (source instanceof URL && (((URL) source).getProtocol().equalsIgnoreCase("http"))) {
            try {
                //getting the query
                final String query = java.net.URLDecoder.decode(((URL) source).getQuery()
                                                                 .intern(),
                        "UTF-8");

                //should we proceed? Let's look for a getmap WMS request
                if (query.intern().indexOf("GetMap") == -1) {
                    return false;
                }

                //tokenizer on $
                final String[] pairs = query.split("&");

                //parse each pair
                final int numPairs = pairs.length;
                String[] kvp = null;

                for (int i = 0; i < numPairs; i++) {
                    //splitting the pairs
                    kvp = pairs[i].split("=");

                    //checking the fields
                    //BBOX
                    if (kvp[0].equalsIgnoreCase("BBOX")) {
                        //splitting fields
                        kvp = kvp[1].split(",");
                        readEnvelope = new GeneralEnvelope(new double[] {
                                    Double.parseDouble(kvp[0]),
                                    Double.parseDouble(kvp[1])
                                },
                                new double[] {
                                    Double.parseDouble(kvp[2]),
                                    Double.parseDouble(kvp[3])
                                });
                    }

                    //SRS
                    if (kvp[0].equalsIgnoreCase("SRS")) {
                        crs = CRS.decode(kvp[1]);
                    }

                    //layers
                    if (kvp[0].equalsIgnoreCase("layers")) {
                        this.coverageName = kvp[1].replaceAll(",", "_");
                    }
                }
                //readuig the image
                readSourceImage();
            } catch (Exception e) {
                //TODO how to handle this?
                return false;
            }

            return true;
        }

        return false;
    }

    /**
     * This method is responsible for reading the CRS whhther a projection file
     * is provided. If no projection file is provided the second choice is the
     * CRS supplied via the crs paramter. If even this one is not avalaible we
     * default to EPSG:4326.
     */
    private void readCRS() {
        //first of all get the crs from the paramter
        crs = (CoordinateReferenceSystem) this.format.getReadParameters()
                                                     .parameter("crs").getValue();

        //check to see if there is a projection file
        if (source instanceof File
                || (source instanceof URL
                && (((URL) source).getProtocol() == "file"))) {
            //getting name for the prj file
            String sourceAsString = null;

            if (source instanceof File) {
                sourceAsString = ((File) source).getAbsolutePath();
            } else {
                sourceAsString = ((URL) source).getFile();
            }

            int index = sourceAsString.lastIndexOf(".");
            StringBuffer base = new StringBuffer(sourceAsString.substring(0,
                        index));
            base.append(".prj");

            //does it exist?
            final File prjFile = new File(base.toString());

            if (prjFile.exists()) {
                //it exists then we have top read it
                try {
                    FileChannel channel = new FileInputStream(prjFile)
                        .getChannel();
                    PrjFileReader projReader = new PrjFileReader(channel);
                    crs = projReader.getCoodinateSystem();
                } catch (Exception e) {
                    //warn about the error but proceed, it is not fatal
                    //we have at least the default crs to use
                    System.err.println(new StringBuffer(
                            "WorldImageReader:readCRS:").append(e.getMessage())
                                                                                    .toString());
                }
            }
        }
    }

    /**
     * This method tries to read the source image for this coverage.
     *
     * @throws IOException
     */
    private void readSourceImage() throws IOException {
        if (source == null) {
            throw new IOException(
                "WorldImage:No source set to read this coverage.");
        }

        //preparing the parameteres to read the images using a file channel
        ParameterBlockJAI readParams = new ParameterBlockJAI("ImageRead",
                "rendered");

        //reading params
        FileChannel channel = null;
        ImageInputStream stream = null;

        if (source instanceof File) {
            RandomAccessFile rFile = new RandomAccessFile(((File) source), "r");
            channel = rFile.getChannel();
            stream = new FileChannelImageInputStream(channel);
        } else if (source instanceof InputStream) {
            stream = javax.imageio.ImageIO.createImageInputStream(source);
        } else if (source instanceof URL) {
            if (((URL) source).getProtocol() == "file") {
                RandomAccessFile rFile = new RandomAccessFile(((URL) source)
                        .getFile(), "r");
                channel = rFile.getChannel();
                stream = new FileChannelImageInputStream(channel);
            } else {
                stream = new FileCacheImageInputStream(((URL) source)
                        .openStream(), null);
            }
        }

        //channel.map(FileChannel.MapMode.READ_ONLY, 0,rFile.length());
        readParams.setParameter("Input", stream);

        //setting accessory parameters
        readParams.setParameter("VerifyInput", false);
        readParams.setParameter("ReadThumbnails", false);

        /** Interpolation and tile handling */

        //tiling the original image
        ImageLayout layout = new ImageLayout();

        //changing parameters related to the tiling
        //TODO make this thing adaptive to the image size
        layout.setTileGridXOffset(0);
        layout.setTileGridYOffset(0);
        layout.setTileWidth(512);
        layout.setTileHeight(512);

        HashMap map = new HashMap();
        map.put(JAI.KEY_IMAGE_LAYOUT, layout);
        map.put(JAI.KEY_INTERPOLATION,
            Interpolation.getInstance(Interpolation.INTERP_BICUBIC_2));

        RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);

        //reading the image
        this.image = JAI.create("ImageRead", readParams, hints);
    }

    /**
     * This method is in charge for reading the metadata file and  for creating
     * a valid envelope (whether possible);
     *
     * @throws IOException
     */
    private void prepareWorldImageEnvelope() throws IOException {
        /** parsing referencing metadata for this file */

        //getting name and extension
        final String sourceAsString = ((File) source).getAbsolutePath();
        int index = sourceAsString.lastIndexOf(".");
        String base = sourceAsString.substring(0, index);
        String fileExtension = sourceAsString.substring(index + 1);

        //coiverage name
        this.coverageName = ((File) source).getName();
        index = coverageName.lastIndexOf(".");
        coverageName = (index == -1) ? coverageName
                                     : coverageName.substring(0, index);

        //We can now construct the baseURL from this string.
        File file2Parse = new File(base + ".wld");

        if (file2Parse.exists()) {
            //parse world file
            parseWorldFile(file2Parse);
        } else {
            //looking for another extension
            file2Parse = new File(base
                    + WorldImageFormat.getWorldExtension(fileExtension));

            if (file2Parse.exists()) {
                //parse world file
                parseWorldFile(file2Parse);
            } else {
                //looking for a meta file
                file2Parse = new File(base + ".meta");

                if (file2Parse.exists()) {
                    parseMetaFile(file2Parse);
                } else {
                    throw new IOException(
                        "No file with meta information found!");
                }
            }
        }
    }

    /**
     * This method is responsible for parsing a META file which is nothing more
     * than  another format of a WorldFile used by the GIDB database.
     *
     * @param file2Parse DOCUMENT ME!
     *
     * @throws NumberFormatException
     * @throws IOException
     */
    private void parseMetaFile(File file2Parse)
        throws NumberFormatException, IOException {
        double xMin = 0.0;
        double yMax = 0.0;
        double xMax = 0.0;
        double yMin = 0.0;

        //getting a buffered reader
        BufferedReader in = new BufferedReader(new FileReader(file2Parse));

        //parsing the lines
        String str = null;
        int index = 0;
        double value = 0;

        while ((str = in.readLine()) != null) {
            switch (index) {
            case 1:
                value = Double.parseDouble(str.substring(
                            "Origin Longitude = ".intern().length()));
                xMin = value;

                break;

            case 2:
                value = Double.parseDouble(str.substring(
                            "Origin Latitude = ".intern().length()));
                yMin = value;

                break;

            case 3:
                value = Double.parseDouble(str.substring(
                            "Corner Longitude = ".intern().length()));
                xMax = value;

                break;

            case 4:
                value = Double.parseDouble(str.substring(
                            "Corner Latitude = ".intern().length()));
                yMax = value;

                break;

            default:
                break;
            }

            index++;
        }

        in.close();

        //building up envelope of this coverage
        readEnvelope = new GeneralEnvelope(new double[] { xMin, yMin },
                new double[] { xMax, yMax });
    }

    /**
     * This method is responsible for parsing the world file associate  with
     * the coverage to be read.
     *
     * @param file2Parse File to parse for reading needed parameters.
     *
     * @throws IOException
     */
    private void parseWorldFile(File file2Parse) throws IOException {
        String str = null;

        double xMin = 0.0;
        double yMax = 0.0;
        double xMax = 0.0;
        double yMin = 0.0;
        float xPixelSize = 0;
        float rotation1 = 0;
        float rotation2 = 0;
        float yPixelSize = 0;
        float xLoc = 0;
        float yLoc = 0;

        int index = 0;
        float value = 0;
        BufferedReader in = new BufferedReader(new FileReader(file2Parse));

        while ((str = in.readLine()) != null) {
            value = 0;

            try {
                value = Float.parseFloat(str.trim());
            } catch (Exception e) {
                // A trick to bypass invalid lines ...
                continue;
            }

            switch (index) {
            case 0:
                xPixelSize = value;

                break;

            case 1:
                rotation1 = value;

                break;

            case 2:
                rotation2 = value;

                break;

            case 3:
                yPixelSize = value;

                break;

            case 4:
                xLoc = value;

                break;

            case 5:
                yLoc = value;

                break;

            default:
                break;
            }

            index++;
        }

        in.close();

        //building up envelope
        xMin = xLoc;
        yMax = yLoc;
        xMax = xLoc + (image.getWidth() * xPixelSize);
        yMin = yLoc + (image.getHeight() * yPixelSize);

        readEnvelope = new GeneralEnvelope(new double[] { xMin, yMin },
                new double[] { xMax, yMax });
    }

    /**
     * Creating a coverage from an Image.
     *
     * @return
     *
     * @throws MismatchedDimensionException
     * @throws IOException
     */
    private GridCoverage createCoverage()
        throws MismatchedDimensionException, IOException {
        //cropping to the requested envelope when needed
        cropAndTileImage();

        //building up a coverage
        GridCoverage coverage = null;

        //deciding the number range
        NumberRange geophysicRange = null;

        switch (image.getSampleModel().getTransferType()) {
        case DataBuffer.TYPE_BYTE:
            geophysicRange = new NumberRange(0, 255);

            break;

        case DataBuffer.TYPE_USHORT:
            geophysicRange = new NumberRange(0, 65535);

            break;

        case DataBuffer.TYPE_INT:
            geophysicRange = new NumberRange(-Integer.MAX_VALUE,
                    Integer.MAX_VALUE);

            break;

        default:
            throw new IOException(
                "Data buffer type not supported by this world image reader! Use byte, ushort or int");
        }

        try {
            //convenieience category in order to 
            Category values = new Category("values",
                    new Color[] { Color.BLACK }, geophysicRange,
                    LinearTransform1D.IDENTITY);

            //creating bands
            final int numBands = image.getSampleModel().getNumBands();
            GridSampleDimension[] bands = new GridSampleDimension[numBands];

            for (int i = 0; i < numBands; i++)
                bands[i] = new GridSampleDimension(new Category[] { values },
                        null).geophysics(true);

            //creating coverage
            coverage = FactoryFinder.getGridCoverageFactory(null).create(coverageName,
                    image, crs, readEnvelope, bands, null, null);
        } catch (NoSuchElementException e1) {
            throw new IOException(
                "Error when creating the coverage in world image"
                + e1.getMessage());
        }

        return coverage;
    }

    /**
     * This method performs a crop operation over the initial image in order to
     * load only the image inside the requested envelope
     *
     * @throws IOException
     */
    private void cropAndTileImage() throws IOException {
       

        /**
         * CROP when needed, otherwise using the read envelope but printing the
         * stack trace
         */
        if (requestedEnvelope != null) {
        	 //getting the reader used for this image
            ImageReader reader = (ImageReader) image.getProperty("JAI.ImageReader");
            ((GeneralEnvelope) requestedEnvelope).intersect((GeneralEnvelope) this.readEnvelope);

            if (!((GeneralEnvelope) requestedEnvelope).isEmpty()) {
                //getting dimensions of the raw image to evaluate the steps
                final int width = image.getWidth();
                final int height = image.getHeight();
                final double los1 = requestedEnvelope.getLowerCorner()
                                                     .getOrdinate(0);
                final double las1 = requestedEnvelope.getLowerCorner()
                                                     .getOrdinate(1);
                final double los2 = requestedEnvelope.getUpperCorner()
                                                     .getOrdinate(0);
                final double las2 = requestedEnvelope.getUpperCorner()
                                                     .getOrdinate(1);

                final double lo1 = readEnvelope.getLowerCorner().getOrdinate(0);
                final double la1 = readEnvelope.getLowerCorner().getOrdinate(1);
                final double lo2 = readEnvelope.getUpperCorner().getOrdinate(0);
                final double la2 = readEnvelope.getUpperCorner().getOrdinate(1);

                final double dX = (lo2 - lo1) / width;
                final double dY = (la2 - la1) / height;

                //we have to keep into account axis directions
                //when using the image
                final Float xIndex1 = new Float(java.lang.Math.ceil(
                            (los1 - lo1) / dX));
                final Float xIndex2 = new Float(java.lang.Math.floor(
                            (los2 - lo1) / dX));
                final Float yIndex1 = new Float(java.lang.Math.floor(
                            (la2 - las2) / dY));
                final Float yIndex2 = new Float(java.lang.Math.ceil(
                            (la2 - las1) / dY));

                /**
                 * Setting reading parameters to control various params like
                 * source region, subsampling
                 */
                ImageReadParam readParam = new ImageReadParam();

                //source region
                readParam.setSourceRegion(new Rectangle(xIndex1.intValue(),
                        xIndex2.intValue(), yIndex1.intValue(),
                        yIndex2.intValue()));

                //setting this for the reader
                image = reader.readAsRenderedImage(0, readParam);

                //				final Float newWidth = new Float(xIndex2.floatValue() - xIndex1.floatValue());
                //				final Float newHeight = new Float(yIndex2.floatValue() - yIndex1.floatValue());				
                //				ParameterBlock pb = new ParameterBlock();
                //	            pb.addSource(image);
                //	            pb.add(xIndex1);
                //	            pb.add(yIndex1);
                //	            pb.add(newWidth);
                //	            pb.add(newHeight);
                //
                //	            image = JAI.create("Crop", pb, null);
                //				pb.removeSources();
                //				
                //				//translating to have coordinates of the image starting from 0
                //	            pb = new ParameterBlock();
                //	            pb.addSource(image);
                //	            pb.add(new Float(-xIndex1.floatValue()));
                //	            pb.add(new Float(-yIndex1.floatValue()));
                //	            image = JAI.create("Translate", pb, null);
                //				pb.removeSources();
            } else {
                //throwing an exception?
            }
        }
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#skip()
     */

    /**
     * Not supported, does nothing.
     *
     * @throws IOException DOCUMENT ME!
     */
    public void skip() throws IOException {
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#dispose()
     */

    /**
     * Cleans up the Reader (currently does nothing)
     *
     * @throws IOException DOCUMENT ME!
     */
    public void dispose() throws IOException {
    }
}
