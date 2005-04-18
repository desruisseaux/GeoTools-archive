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
package org.geotools.data.gtopo30;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.spatialschema.geometry.Envelope;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.ByteOrder;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.media.jai.Histogram;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;


/**
 * DOCUMENT ME!
 *
 * @author jeichar
 */
public class GTopo30Writer implements GridCoverageWriter {
    private Object destination;

    /**
     * DOCUMENT ME!
     *
     * @param dest
     */
    public GTopo30Writer(Object dest) {
        this.destination = dest;

        if (dest == null) {
            return;
        }

        File temp = null;
        URL url = null;

        try {
            //we only accept a directory as a path
            if (dest instanceof String) {
                temp = new File((String) dest);

                //if it exists and it is not a directory that 's not good
                if ((temp.exists() && !temp.isDirectory()) || !temp.exists()) {
                    this.destination = null; //we cannot write
                } else if (!temp.exists()) {
                    //well let's create it!
                    if (!temp.mkdir()) {
                        this.destination = null;
                    } else {
                        this.destination = temp.getAbsolutePath();
                    }
                }
            } else if (dest instanceof File) {
                temp = (File) dest;

                if (temp.exists() && !temp.isDirectory()) {
                    this.destination = null;
                } else if (!temp.exists()) {
                    //let's create it
                    if (temp.mkdir()) {
                        this.destination = temp.getAbsolutePath();
                    } else {
                        this.destination = null;
                    }
                }
            } else if (dest instanceof URL) {
                url = (URL) dest;

                if (url.getProtocol().compareToIgnoreCase("file") != 0) {
                    this.destination = null;
                }

                temp = new File(url.getFile());

                if (temp.exists() && !temp.isDirectory()) {
                    this.destination = null;
                } else if (!temp.exists()) {
                    //let's create it
                    if (temp.mkdir()) {
                        this.destination = temp.getAbsolutePath();
                    } else {
                        this.destination = null;
                    }
                }
            }
        } catch (Exception e) {
            this.destination = null;
        }
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageWriter#getFormat()
     */
    public Format getFormat() {
        // @todo Auto-generated method stub
        return null;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageWriter#getDestination()
     */
    public Object getDestination() {
        // @todo Auto-generated method stub
        return null;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageWriter#getMetadataNames()
     */
    public String[] getMetadataNames() {
        // @todo Auto-generated method stub
        return null;
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageWriter#setMetadataValue(java.lang.String,
     *      java.lang.String)
     */
    public void setMetadataValue(String name, String value)
        throws IOException, MetadataNameNotFoundException {
        // @todo Auto-generated method stub
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageWriter#setCurrentSubname(java.lang.String)
     */
    public void setCurrentSubname(String name) throws IOException {
        // @todo Auto-generated method stub
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageWriter#write(org.opengis.coverage.grid.GridCoverage,
     *      org.opengis.parameter.GeneralParameterValue[])
     */
    public void write(GridCoverage coverage, GeneralParameterValue[] parameters)
        throws java.lang.IllegalArgumentException, java.io.IOException {
        //GETTING FILENAME
        String fileName = (((GridCoverage2D) coverage).getName()).toString();

        //write DEM
        this.writeDEM(coverage,
            new File(((File) this.destination).getAbsolutePath() + "/"
                + fileName + ".DEM"), ByteOrder.BIG_ENDIAN);

        //write HDR
        this.writeHDR(coverage,
            new File(((File) this.destination).getAbsolutePath() + "/"
                + fileName + ".HDR"));

        //write world file
        this.writeWorldFile(coverage,
            new File(((File) this.destination).getAbsolutePath() + "/"
                + fileName + ".DMW"));

        //write statistics
        this.writeStats(coverage,
            new File(((File) this.destination).getAbsolutePath() + "/"
                + fileName + ".STX"));

        //write projection
        this.writePRJ(coverage,
            new File(((File) this.destination).getAbsolutePath() + "/"
                + fileName + ".PRJ"));

        //write gif
        this.writeGIF(coverage,
            new File(((File) this.destination).getAbsolutePath() + "/"
                + fileName + ".GIF"));

        //write src
        this.writeSRC(coverage,
            new File(((File) this.destination).getAbsolutePath() + "/"
                + fileName + ".SRC"), ByteOrder.BIG_ENDIAN);
    }

    /**
     * DOCUMENT ME!
     *
     * @param coverage
     * @param file
     *
     * @throws FileNotFoundException
     */
    private void writeHDR(GridCoverage coverage, File file)
        throws FileNotFoundException {
        GridCoverage2D gc = (GridCoverage2D) coverage;

        GeneralEnvelope envelope = (GeneralEnvelope) gc.getEnvelope();
        String[] metadataNames = gc.getMetadataNames();
        double noData = Double.NaN;

        if (gc.geophysics(true).getSampleDimension(0).getNoDataValues() != null) {
            noData = gc.geophysics(true).getSampleDimension(0).getNoDataValues()[0];
        } else if (metadataNames != null) {
            for (int i = 0; i < metadataNames.length; i++)
                if (metadataNames[i].compareToIgnoreCase("nodata") == 0) {
                    noData = Double.parseDouble(gc.getMetadataValue(
                                metadataNames[i]));
                }
        }

        double xUpperLeft = envelope.getLowerCorner().getOrdinate(0);
        double yUpperLeft = envelope.getUpperCorner().getOrdinate(1);
        int width = gc.getGridGeometry().getGridRange().getLength(0);
        double dx = envelope.getLength(0) / width;

        int height = gc.getGridGeometry().getGridRange().getLength(1);
        double dy = envelope.getLength(1) / height;
        PrintWriter out = new PrintWriter(new FileOutputStream(file));

        // output header and assign header fields
        out.print("BYTEORDER");
        out.print(" ");
        out.println("M");

        out.print("LAYOUT");
        out.print(" ");
        out.println("BIL");

        out.print("NROWS");
        out.print(" ");
        out.println(height);

        out.print("NCOLS");
        out.print(" ");
        out.println(width);

        out.print("NBANDS");
        out.print(" ");
        out.println("1");

        out.print("NBITS");
        out.print(" ");
        out.println("16");

        out.print("BANDROWBYTES");
        out.print(" ");
        out.println(width * 2);

        out.print("TOTALROWBYTES");
        out.print(" ");
        out.println(width * 2);

        out.print("BANDGAPBYTES");
        out.print(" ");
        out.println(0);

        out.print("NODATA");
        out.print(" ");
        out.println((int)noData);

        out.print("ULXMAP");
        out.print(" ");
        out.println(xUpperLeft + (dx / 2));

        out.print("ULYMAP");
        out.print(" ");
        out.println(yUpperLeft - (dy / 2));

        out.print("XDIM");
        out.print(" ");
        out.println(dx);

        out.print("YDIM");
        out.print(" ");
        out.println(dy);

        out.flush();
        out.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param coverage
     * @param dest
     * @param bo DOCUMENT ME!
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeSRC(GridCoverage coverage, File dest, ByteOrder bo)
        throws FileNotFoundException, IOException {
        //we cannot get all the needed information 
        GridCoverage2D gc = (GridCoverage2D) coverage;
        gc = gc.geophysics(false);

        FileImageOutputStream out = new FileImageOutputStream(dest);
        out.setByteOrder(java.nio.ByteOrder.BIG_ENDIAN);
        ImageIO.write(((PlanarImage) gc.getRenderedImage()).getAsBufferedImage(),
            "raw", out);

        out.flush();
        out.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param coverage
     * @param file
     *
     * @throws IOException
     */
    private void writeGIF(GridCoverage coverage, File file)
        throws IOException {
        GridCoverage2D gc = (GridCoverage2D) coverage;

        //writing gif image
        ImageIO.write(((PlanarImage) gc.geophysics(false).getRenderedImage())
            .getAsBufferedImage(), "GIF", file);
    }

    /**
     * Write a projection file using wkt
     *
     * @param coverage
     * @param file
     *
     * @throws IOException
     */
    private void writePRJ(GridCoverage coverage, File file)
        throws IOException {
        GridCoverage2D gc = (GridCoverage2D) coverage;

        //create the file
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));

        //write information on crs
        fileWriter.write(gc.getCoordinateReferenceSystem().toWKT());
        fileWriter.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param coverage
     * @param file
     *
     * @throws IOException
     */
    private void writeStats(GridCoverage coverage, File file)
        throws IOException {
        GridCoverage2D gc = (GridCoverage2D) coverage;

        //we need to evaluate stats first using jai
        ParameterBlock pb = new ParameterBlock();
        pb.addSource((PlanarImage) gc.getRenderedImage());
        pb.add(null); //no roi
        pb.add(1);
        pb.add(1);

        //getting histogram back
        RenderedOp op = JAI.create("extrema", pb);

        double[][] extrema = (double[][]) op.getProperty("extrema");

        //histogram
        pb = new ParameterBlock();
        pb.addSource((PlanarImage) gc.getRenderedImage());
        pb.add(null); //no roi
        pb.add(1);
        pb.add(1);
        pb.add(new int[] { (int) (extrema[1][0] - extrema[0][0] + 1) });
        pb.add(extrema[0]);
        pb.add(extrema[1]);
        pb.add(1);

        Histogram hist = (Histogram) ((PlanarImage) JAI.create("histogram", pb,
                null)).getProperty("histogram");

        //files destinations
        if (!file.exists()) {
            file.createNewFile();
        }

        //writing world file
        PrintWriter out = new PrintWriter(new FileOutputStream(file));

        out.print(1);
        out.print(" ");
        out.print((int)extrema[0][0]);
        out.print(" ");
        out.print((int)extrema[1][0]);
        out.print(" ");
        out.print(hist.getMean()[0]);
        out.print(" ");
        out.print(hist.getStandardDeviation()[0]);
        out.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param coverage
     * @param worldFile
     *
     * @throws IOException
     */
    private void writeWorldFile(GridCoverage coverage, File worldFile)
        throws IOException {
        GridCoverage2D gc = (GridCoverage2D) coverage;
        gc = (GridCoverage2D) gc.geophysics(true);

        RenderedImage image = ((PlanarImage) gc.getRenderedImage())
            .getAsBufferedImage();
        Envelope env = gc.getEnvelope();
        double xMin = env.getMinimum(0);
        double yMin = env.getMinimum(1);
        double xMax = env.getMaximum(0);
        double yMax = env.getMaximum(1);

        double xPixelSize = (xMax - xMin) / image.getWidth();
        double rotation1 = 0;
        double rotation2 = 0;
        double yPixelSize = (yMax - yMin) / image.getHeight();
        double xLoc = xMin;
        double yLoc = yMax;

        //files destinations
        if (!worldFile.exists()) {
            worldFile.createNewFile();
        }

        //writing world file
        PrintWriter out = new PrintWriter(new FileOutputStream(worldFile));

        out.println(xPixelSize);
        out.println(rotation1);
        out.println(rotation2);
        out.println("-" + yPixelSize);
        out.println(xLoc);
        out.println(yLoc);
        out.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param coverage
     * @param dest DOCUMENT ME!
     * @param bo DOCUMENT ME!
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeDEM(GridCoverage coverage, File dest, ByteOrder bo)
        throws FileNotFoundException, IOException {
        GridCoverage2D gc = (GridCoverage2D) coverage;

        FileImageOutputStream out = new FileImageOutputStream(dest);
        out.setByteOrder(java.nio.ByteOrder.BIG_ENDIAN);
        ImageIO.write(((PlanarImage) gc.getRenderedImage()).getAsBufferedImage(),
            "raw", out);

        out.flush();
        out.close();
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageWriter#dispose()
     */
    public void dispose() throws IOException {
        // @todo Auto-generated method stub
    }
}
