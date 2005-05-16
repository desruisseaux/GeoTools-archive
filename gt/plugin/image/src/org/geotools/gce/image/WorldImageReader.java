/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, 2004 Geotools Project Managment Committee (PMC)
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

import org.geotools.coverage.grid.GridCoverage2D;

import org.geotools.factory.Hints;

import org.geotools.geometry.GeneralEnvelope;

import org.geotools.parameter.Parameter;

import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;

import org.opengis.parameter.GeneralParameterValue;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.spatialschema.geometry.MismatchedDimensionException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.renderable.ParameterBlock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.NoSuchElementException;

import javax.imageio.ImageIO;

import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;


/**
 *
 * @author simone giannecchini
 * @author alessio fabiani
 * @author rgould
 * Reads a GridCoverage from a given source. WorldImage sources
 * only support one GridCoverage so hasMoreGridCoverages() will
 * return true until the only GridCoverage is read.
 *
 * No metadata is currently supported, so all related methods return null.
 */
public class WorldImageReader implements GridCoverageReader {
    public static int WORLD_WLD = 1;
    public static int WORLD_META = 2;
    public static int WORLD_BASE = 3;

    /**Format for this reader*/
    private Format format = new WorldImageFormat();

    /*Source to read from*/
    private Object source;
    private boolean gridLeft = true;

    /**Class constructor.
     * Construct a new ImageWorldReader to read a GridCoverage from the
     * source object. The source must point to the raster file itself,
     * not the world file.
     *
     * If the source is a Java URL it checks if it is ponting to a file
     * and if so it converts the url into a file.
     *
     * @param source The source of a GridCoverage
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
     */
    public String[] getMetadataNames() throws IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#getMetadataValue(java.lang.String)
     */

    /**
     * Metadata is not supported. Returns null.
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
     */
    public String[] listSubNames() throws IOException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#getCurrentSubname()
     */

    /**
     * WorldImage GridCoverages are not named. Returns null.
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
     */
    public boolean hasMoreGridCoverages() throws IOException {
        return gridLeft;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
     */

    /**Reads an image from a source stream.
     *
     * Loads an image from a source stream, then loads the values from the world file
     * and constructs a new GridCoverage from this information.
     *
     * When reading from a remote stream we do not look for a world fiel but we suppose those information comes from
     * a different way (xml, gml, pigeon?)
     *
     * @param parameters WorldImageReader supports no parameters, it just ignores them.
     * @return a new GridCoverage read from the source.
     */
    public GridCoverage read(GeneralParameterValue[] parameters)
        throws IllegalArgumentException, IOException {
        //do we have paramters to use for reading from the specified source
        if (parameters != null) {
            //they will be ignored if we will find a world file
            this.format.getReadParameters().parameter("crs").setValue(((Parameter) parameters[0]).getValue());
            this.format.getReadParameters().parameter("envelope").setValue(((Parameter) parameters[1]).getValue());
        }

        URL sourceURL = null;

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

        CoordinateReferenceSystem crs = (CoordinateReferenceSystem) this.format.getReadParameters()
                                                                               .parameter("crs")
                                                                               .getValue();
        Envelope envelope = null;
        String coverageName = "";

        int world_type = -1;
        BufferedImage image = null;
        BufferedReader in = null;

        //are we reading from a file?
        //in such a case we will look for the associated world file
        if (source instanceof File) {
            sourceURL = ((File) source).toURL();

            //reading the image as given
            image = ImageIO.read(sourceURL);

//            if (image.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB) {
//                ColorModel rgbcm = new ComponentColorModel(ColorSpace.getInstance(
//                            ColorSpace.CS_sRGB), new int[] { 8, 8, 8 }, false,
//                        false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
//
//                ParameterBlock pb = new ParameterBlock();
//                pb.addSource(image).add(rgbcm);
//
//                ImageLayout il = new ImageLayout();
//                il.setColorModel(rgbcm);
//                il.setSampleModel(rgbcm.createCompatibleSampleModel(
//                        image.getWidth(), image.getHeight()));
//
//                RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT,
//                        il);
//
//                hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, il);
//
//                PlanarImage dst = JAI.create("ColorConvert", pb, hints);
//
//                     
//                					double[][] matrix = {
//                					       { -0.80D, 0.00D, 0.00D, 1.0D, 0.0D }, //R 
//											{ 0.00D, -0.80D, 0.00D, 1.0D, 0.0D }, //G 
//											{ 0.00D, 0.00D, -0.80D, 1.0D, 0.0D } //B 
//                            };
//                            ParameterBlock pb = new ParameterBlock();
//                            pb.addSource(image);
//                            pb.add(matrix);
//                            PlanarImage imagep = JAI.create("bandcombine", pb, null);
//                            ParameterBlockJAI pbjai = new  ParameterBlockJAI("colorconvert");
//                            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
//                            int[] bits = { 8, 8, 8 };
//                            ColorModel cm = new ComponentColorModel(cs,bits,false,false,Transparency.OPAQUE,DataBuffer.TYPE_BYTE);
//                            pbjai.addSource(imagep);
//                            pbjai.setParameter("colormodel", cm);
//                            ImageLayout il = new ImageLayout();
//                            il.setSampleModel(cm.createCompatibleSampleModel(imagep.getWidth(),imagep.getHeight()));
//                            RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, il);
//                            RenderedOp dst = JAI.create("colorconvert", pbjai, hints);
//                    
//                
//                
//                //displaying
//                JFrame frame = new JFrame();
//                JPanel topPanel = new JPanel();
//                topPanel.setLayout(new BorderLayout());
//                frame.getContentPane().add(topPanel);
//
//                frame.setBackground(Color.black);
//
//                JScrollPane pane = new JScrollPane();
//                pane.getViewport().add(new JLabel(
//                        new ImageIcon(dst.createSnapshot().getAsBufferedImage())));
//                topPanel.add(pane, BorderLayout.CENTER);
//                frame.getContentPane().add(pane);
//                frame.getContentPane().add(pane, BorderLayout.CENTER);
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                frame.pack();
//                frame.show();
//            }

            String sourceAsString = sourceURL.toExternalForm();
            int index = sourceAsString.lastIndexOf(".");
            String base = sourceAsString.substring(0, index);
            String fileExtension = sourceAsString.substring(index + 1);

            //We can now construct the baseURL from this string.
            try {
                URL worldURL = new URL(base + ".wld");

                in = new BufferedReader(new InputStreamReader(
                            worldURL.openStream()));
                world_type = WORLD_WLD;
            } catch (FileNotFoundException e1) {
                try {
                    //.wld extension not found, go for .meta.
                    URL worldURL = new URL(base + ".meta");

                    in = new BufferedReader(new InputStreamReader(
                                worldURL.openStream()));
                    world_type = WORLD_META;
                } catch (FileNotFoundException e2) {
                    //.wld & .meta extension not found, go for file based one.
                    URL worldURL = new URL(base +
                            WorldImageFormat.getWorldExtension(fileExtension));

                    in = new BufferedReader(new InputStreamReader(
                                worldURL.openStream()));
                    world_type = WORLD_BASE;
                }
            }

            String str;

            index = 0;

            while ((str = in.readLine()) != null) {
                if ((world_type == WORLD_WLD) || (world_type == WORLD_BASE)) {
                    float value = Float.parseFloat(str.trim());

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
                } else if (world_type == WORLD_META) {
                    String line = str;

                    rotation1 = 0.0f;
                    rotation2 = 0.0f;

                    double value;

                    switch (index) {
                    case 1:
                        value = Double.parseDouble(line.substring(
                                    "Origin Longitude = ".length()));
                        xMin = value;

                        break;

                    case 2:
                        value = Double.parseDouble(line.substring(
                                    "Origin Latitude = ".length()));
                        yMin = value;

                        break;

                    case 3:
                        value = Double.parseDouble(line.substring(
                                    "Corner Longitude = ".length()));
                        xMax = value;

                        break;

                    case 4:
                        value = Double.parseDouble(line.substring(
                                    "Corner Latitude = ".length()));
                        yMax = value;

                        break;

                    default:
                        break;
                    }
                }

                index++;
            }

            in.close();

            //building up envelope
            if ((world_type == WORLD_WLD) || (world_type == WORLD_BASE)) {
                xMin = xLoc;
                yMax = yLoc;
                xMax = xLoc + (image.getWidth() * xPixelSize);
                yMin = yLoc + (image.getHeight() * yPixelSize);
            }

            envelope = new GeneralEnvelope(new double[] { xMin, yMin },
                    new double[] { xMax, yMax });
        } else { //assuming it is an  url or an input stream

            //well it seems we are not reading from a file
            //therefore we need the parameters
            //getting crs
            crs = (CoordinateReferenceSystem) this.format.getReadParameters()
                                                         .parameter("crs")
                                                         .getValue();

            //getting envelope
            envelope = (Envelope) this.format.getReadParameters()
                                             .parameter("envelope").getValue();

            //reading the image as given
            if (source instanceof URL) {
                image = ImageIO.read((URL) source);
                coverageName = ((URL) source).getFile();
            } else if (source instanceof InputStream) {
                image = ImageIO.read((InputStream) source);
            }

            if ((envelope == null) || (crs == null)) {
                throw new IllegalArgumentException(
                    "To read froma a source which is not a file please provided read parameters!");
            }
        }

        //no more grid left
        gridLeft = false;

        return createCoverage(image, crs, envelope, coverageName);
    }

    /**Creating a coverage from an Image.
     * @param image
     * @param crs
     * @param envelope
     * @param coverageName
     * @return
     * @throws MismatchedDimensionException
     * @throws IOException
     */
    private GridCoverage createCoverage(BufferedImage image,
        CoordinateReferenceSystem crs, Envelope envelope, String coverageName)
        throws MismatchedDimensionException, IOException {
        //building up a coverage
        GridCoverage coverage = null;

        try {
            Hints hint = new Hints(Hints.AVOID_NON_GEOPHYSICS, Boolean.TRUE);
            coverage = new GridCoverage2D(coverageName, image, crs, envelope,
                    null);
        } catch (NoSuchElementException e1) {
            throw new IOException(e1.getMessage());
        }

        return coverage;
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#skip()
     */

    /**
     * Not supported, does nothing.
     */
    public void skip() throws IOException {
    }

    /* (non-Javadoc)
     * @see org.geotools.data.coverage.grid.GridCoverageReader#dispose()
     */

    /**
     * Cleans up the Reader (currently does nothing)
     */
    public void dispose() throws IOException {
    }
}
