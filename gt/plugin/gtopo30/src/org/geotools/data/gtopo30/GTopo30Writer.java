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

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
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
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStreamImpl;
import javax.media.jai.Histogram;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.GeneralEnvelope;
import org.opengis.coverage.MetadataNameNotFoundException;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.spatialschema.geometry.Envelope;


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
            } else if (dest instanceof ZipOutputStream) {
                this.destination = (ZipOutputStream) dest;
                ((ZipOutputStream) this.destination).setMethod(ZipOutputStream.DEFLATED);
                ((ZipOutputStream) this.destination).setLevel(Deflater.BEST_COMPRESSION);
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

        //destination
        Object dest = this.destination;

        //write DEM
        if (this.destination instanceof File) {
            fileName = ((File) this.destination).getAbsolutePath() + "/" +
                fileName;
            dest = new File(fileName + ".DEM");
        }

        this.writeDEM(coverage, dest, ByteOrder.BIG_ENDIAN);

        //write HDR
        if (this.destination instanceof File) {
            dest = new File(fileName + ".HDR");
        }

        this.writeHDR(coverage, dest);

        //write world file
        if (this.destination instanceof File) {
            dest = new File(fileName + ".DMW");
        }

        this.writeWorldFile(coverage, dest);

        //write statistics
        if (this.destination instanceof File) {
            dest = new File(fileName + ".STX");
        }

        this.writeStats(coverage, dest);

        //write projection
        if (this.destination instanceof File) {
            dest = new File(fileName + ".PRJ");
        }

        this.writePRJ(coverage, dest);

        //write gif
        if (this.destination instanceof File) {
            dest = new File(fileName + ".GIF");
        }

        this.writeGIF(coverage, dest);

        //write src
        if (this.destination instanceof File) {
            dest = new File(fileName + ".SRC");
        }

        this.writeSRC(coverage, dest, ByteOrder.BIG_ENDIAN);
    }

    /**
     * DOCUMENT ME!
     *
     * @param coverage
     * @param file
     *
     * @throws IOException
     */
    private void writeHDR(GridCoverage coverage, Object file)
        throws IOException {
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

        if (file instanceof File) {
            PrintWriter out = new PrintWriter(new FileOutputStream((File) file));

            //          output header and assign header fields
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
            out.println((int) noData);

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
        } else {
            ZipOutputStream outZ = (ZipOutputStream) this.destination;
            ZipEntry e = new ZipEntry((((GridCoverage2D) coverage).getName()).toString() +
                    ".HDR");
            outZ.putNextEntry(e);

            //          writing world file
            outZ.write("BYTEORDER".getBytes());
            outZ.write(" ".getBytes());
            outZ.write("M".getBytes());
            outZ.write("\n".getBytes());

            outZ.write("LAYoutZ".getBytes());
            outZ.write(" ".getBytes());
            outZ.write("BIL".getBytes());
            outZ.write("\n".getBytes());

            outZ.write("NROWS".getBytes());
            outZ.write(" ".getBytes());
            outZ.write(Integer.toString(height).getBytes());
            outZ.write("\n".getBytes());

            outZ.write("NCOLS".getBytes());
            outZ.write(" ".getBytes());
            outZ.write(Integer.toString(width).getBytes());
            outZ.write("\n".getBytes());

            outZ.write("NBANDS".getBytes());
            outZ.write(" ".getBytes());
            outZ.write("1".getBytes());
            outZ.write("\n".getBytes());

            outZ.write("NBITS".getBytes());
            outZ.write(" ".getBytes());
            outZ.write("16".getBytes());
            outZ.write("\n".getBytes());

            outZ.write("BANDROWBYTES".getBytes());
            outZ.write(" ".getBytes());
            outZ.write(Integer.toString(width * 2).getBytes());
            outZ.write("\n".getBytes());

            outZ.write("TOTALROWBYTES".getBytes());
            outZ.write(" ".getBytes());
            outZ.write(Integer.toString(width * 2).getBytes());
            outZ.write("\n".getBytes());

            outZ.write("BANDGAPBYTES".getBytes());
            outZ.write(" ".getBytes());
            outZ.write("0".getBytes());
            outZ.write("\n".getBytes());

            outZ.write("NODATA".getBytes());
            outZ.write(" ".getBytes());
            outZ.write(Integer.toString((int) noData).getBytes());
            outZ.write("\n".getBytes());

            outZ.write("ULXMAP".getBytes());
            outZ.write(" ".getBytes());
            outZ.write(Double.toString(xUpperLeft + (dx / 2)).getBytes());
            outZ.write("\n".getBytes());

            outZ.write("ULYMAP".getBytes());
            outZ.write(" ".getBytes());
            outZ.write(Double.toString(yUpperLeft - (dy / 2)).getBytes());
            outZ.write("\n".getBytes());

            outZ.write("XDIM".getBytes());
            outZ.write(" ".getBytes());
            outZ.write(Double.toString(dx).getBytes());
            outZ.write("\n".getBytes());

            outZ.write("YDIM".getBytes());
            outZ.write(" ".getBytes());
            outZ.write(Double.toString(dy).toString().getBytes());
            outZ.write("\n".getBytes());

            outZ.closeEntry();

            ((ZipOutputStream) file).closeEntry();
        }
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
    private void writeSRC(GridCoverage coverage, Object dest, ByteOrder bo)
        throws FileNotFoundException, IOException {
        //we cannot get all the needed information 
        GridCoverage2D gc = (GridCoverage2D) coverage;
        gc = gc.geophysics(false); //8 bit

        ImageOutputStreamImpl out = null;

        if (dest instanceof File) {
            out = new FileImageOutputStream((File) dest);
        } else {
            ZipOutputStream outZ = (ZipOutputStream) dest;
            ZipEntry e = new ZipEntry((((GridCoverage2D) coverage).getName()).toString() +
                    ".SRC");
            outZ.putNextEntry(e);

            out = new FileCacheImageOutputStream(outZ, null);
        }

        out.setByteOrder(java.nio.ByteOrder.BIG_ENDIAN);
        ImageIO.write(((PlanarImage) gc.getRenderedImage()).getAsBufferedImage(),
            "raw", out);

        if (!(dest instanceof File)) {
            ((ZipOutputStream) dest).closeEntry();
        }

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
    private void writeGIF(GridCoverage coverage, Object file)
        throws IOException {
        GridCoverage2D gc = (GridCoverage2D) coverage;
        ImageOutputStreamImpl out = null;

        if (file instanceof File) {
            //writing gif image
            out = new FileImageOutputStream((File) file);
        } else {
            ZipOutputStream outZ = (ZipOutputStream) file;
            ZipEntry e = new ZipEntry((((GridCoverage2D) coverage).getName()).toString() +
                    ".GIF");
            outZ.putNextEntry(e);

            out = new FileCacheImageOutputStream(outZ, null);
        }

        ImageIO.write(this.convertIndexColorModelAlpha4GIF((PlanarImage)gc.geophysics(false).getRenderedImage()), "GIF", out);

        if (file instanceof File) {
            out.close();
        } else {
            ((ZipOutputStream) file).closeEntry();
        }
    }
	/**
     * GIF does not support full alpha channel we need to reduce it in order to
     * provide a simple transparency index to a unique fully transparent
     * color.
     *
     * @param surrogateImage
     *
     * @return
     */
    private PlanarImage convertIndexColorModelAlpha4GIF(
        PlanarImage surrogateImage) {
		//doing nothing if the input color model is correct
        final IndexColorModel cm = (IndexColorModel) surrogateImage.getColorModel();
		if(cm.getTransparency()==Transparency.OPAQUE)
			return surrogateImage;
			

		
        byte[][] rgba = new byte[4][256]; //WE MIGHT USE LESS THAN 256 COLORS
 
		//getting all the colors
		cm.getReds(rgba[0]);
		cm.getGreens(rgba[1]);
		cm.getBlues(rgba[2]);
		


        //get the data (actually a copy of them) and prepare to rewrite them
        WritableRaster raster = surrogateImage.copyData();

        /**
         * Now we are going to use the first transparent color as it were the
         * transparent color and we point all the tranpsarent pixel to this
         * color in the color map.
         *
         *
         * NOTE Assuming we have just one band.
         */
        int transparencyIndex = -1;
        int index = -1;
        for (int i = 0; i < raster.getHeight(); i++) {
            for (int j = 0; j < raster.getWidth(); j++) {
                //index in the color map is given by a value in the raster.
                index = raster.getSample(j, i, 0);

                //check for transparency
                if ((cm.getAlpha(index)&0xff) == 0) {
                    //FULLY TRANSPARENT PIXEL
                    if (transparencyIndex == -1) {
                        //setting transparent color to this one
                        //the other tranpsarent bits will point to this one
                        transparencyIndex = cm.getAlpha(index);
  
                        //                      setting sample in the raster that corresponds to an index in the
                        //color map
                        raster.setSample(j, i, 0, transparencyIndex++);
                    } else //we alredy set the transparent color we will reuse that one
                     {
                        //basically do nothing here
                        //we do not need to add a new color because we are reusing the old on
                        //we already set
                        //                      setting sample in the raster that corresponds to an index in the
                        //color map
                        raster.setSample(j, i, 0, transparencyIndex);
                    }
                } else //NON FULLY TRANSPARENT PIXEL
                 {

                    //setting sample in the raster that corresponds to an index in the
                    //color map                    
                    //raster.setSample(j, i, 0, colorIndex++);
                }
            }
        }

        /**
         * Now all the color are opaque except one and the color map has been
         * rebuilt loosing all the tranpsarent colors except the first one.
         * The raster has been rebuilt as well, in order to make it point to the
         * right color in the color map.  We have to create the new image
         * to be returned.
         */
        IndexColorModel cm1 =transparencyIndex==-1? new IndexColorModel(
				cm.getComponentSize(0),
				256,
				rgba[0],
				rgba[1],
				rgba[2]): new IndexColorModel(
						cm.getComponentSize(0),
						256,
						rgba[0],
						rgba[1],
						rgba[2],
						transparencyIndex);
		
		BufferedImage image= new BufferedImage(raster.getWidth(),
				raster.getHeight(),
				BufferedImage.TYPE_BYTE_INDEXED,
				cm1);
        image.setData(raster);

        //disposing old image
        surrogateImage.dispose();

        return PlanarImage.wrapRenderedImage(image);
    }

    /**
     * Write a projection file using wkt
     *
     * @param coverage
     * @param file
     *
     * @throws IOException
     */
    private void writePRJ(GridCoverage coverage, Object file)
        throws IOException {
        GridCoverage2D gc = (GridCoverage2D) coverage;

        if (file instanceof File) {
            //create the file
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(
                        (File) file));

            //write information on crs
            fileWriter.write(gc.getCoordinateReferenceSystem().toWKT());
            fileWriter.close();
        } else {
            ZipOutputStream out = (ZipOutputStream) file;
            ZipEntry e = new ZipEntry((((GridCoverage2D) coverage).getName()).toString() +
                    ".PRJ");
            out.putNextEntry(e);
            out.write(gc.getCoordinateReferenceSystem().toWKT().getBytes());
            out.closeEntry();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param coverage
     * @param file
     *
     * @throws IOException
     */
    private void writeStats(GridCoverage coverage, Object file)
        throws IOException {
        GridCoverage2D gc = (GridCoverage2D) coverage;
        ParameterBlock pb = new ParameterBlock();

        //we need to evaluate stats first using jai
        double[] Max = new double[] { gc.getSampleDimension(0).getMaximumValue() };
        double[] Min = new double[] { gc.getSampleDimension(0).getMinimumValue() };

        //histogram
        pb.addSource((PlanarImage) gc.getRenderedImage());
        pb.add(null); //no roi
        pb.add(1);
        pb.add(1);
        pb.add(new int[] { (int) (Max[0] - Min[0] + 1) });
        pb.add(Min);
        pb.add(Max);
        pb.add(1);

        Histogram hist = (Histogram) ((PlanarImage) JAI.create("histogram", pb,
                null)).getProperty("histogram");

        if (file instanceof File) {
            //files destinations
            if (!((File) file).exists()) {
                ((File) file).createNewFile();
            }

            //writing world file
            PrintWriter out = new PrintWriter(new FileOutputStream(
                        ((File) file)));
            out.print(1);
            out.print(" ");
            out.print((int) Min[0]);
            out.print(" ");
            out.print((int) Max[0]);
            out.print(" ");
            out.print(hist.getMean()[0]);
            out.print(" ");
            out.print(hist.getStandardDeviation()[0]);
            out.close();
        } else {
            ZipOutputStream outZ = (ZipOutputStream) file;
            ZipEntry e = new ZipEntry((((GridCoverage2D) coverage).getName()).toString() +
                    ".STX");
            outZ.putNextEntry(e);

            //          writing world file
            outZ.write("1".getBytes());
            outZ.write(" ".getBytes());
            outZ.write(new Integer((int) Min[0]).toString().getBytes());
            outZ.write(" ".getBytes());
            outZ.write(new Integer((int) Max[0]).toString().getBytes());
            outZ.write(" ".getBytes());
            outZ.write(new Double(hist.getMean()[0]).toString().getBytes());
            outZ.write(" ".getBytes());
            outZ.write(new Double(hist.getStandardDeviation()[0]).toString()
                                                                 .getBytes());
            ((ZipOutputStream) file).closeEntry();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param coverage
     * @param worldFile
     *
     * @throws IOException
     */
    private void writeWorldFile(GridCoverage coverage, Object worldFile)
        throws IOException {
        GridCoverage2D gc = (GridCoverage2D) coverage;
        gc = (GridCoverage2D) gc.geophysics(true);

        RenderedImage image = ((PlanarImage) gc.getRenderedImage()).getAsBufferedImage();
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

        PrintWriter out = null;

        if (worldFile instanceof File) {
            //files destinations
            if (!((File) worldFile).exists()) {
                ((File) worldFile).createNewFile();
            }

            //writing world file
            out = new PrintWriter(new FileOutputStream((File) worldFile));
            out.println(xPixelSize);
            out.println(rotation1);
            out.println(rotation2);
            out.println("-" + yPixelSize);
            out.println(xLoc);
            out.println(xLoc);
            out.close();
        } else {
            ZipOutputStream outZ = (ZipOutputStream) worldFile;
            ZipEntry e = new ZipEntry((((GridCoverage2D) coverage).getName()).toString() +
                    ".DMW");
            outZ.putNextEntry(e);

            //          writing world file
            outZ.write(new Double(xPixelSize).toString().getBytes());
            outZ.write("\n".getBytes());
            outZ.write(new Double(rotation1).toString().getBytes());
            outZ.write("\n".getBytes());
            outZ.write(new Double(rotation2).toString().getBytes());
            outZ.write("\n".getBytes());
            outZ.write(new Double(xPixelSize).toString().getBytes());
            outZ.write("\n".getBytes());
            outZ.write(new Double(-yPixelSize).toString().getBytes());
            outZ.write("\n".getBytes());
            outZ.write(new Double(xLoc).toString().getBytes());
            outZ.write("\n".getBytes());
            outZ.write(new Double(yLoc).toString().getBytes());
            outZ.write("\n".getBytes());

            ((ZipOutputStream) worldFile).closeEntry();
        }
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
    private void writeDEM(GridCoverage coverage, Object dest, ByteOrder bo)
        throws FileNotFoundException, IOException {
        GridCoverage2D gc = (GridCoverage2D) coverage;
        ImageOutputStreamImpl out = null;

        if (dest instanceof File) {
            out = new FileImageOutputStream((File) dest);
        } else {
            ZipOutputStream outZ = (ZipOutputStream) dest;
            ZipEntry e = new ZipEntry((((GridCoverage2D) coverage).getName()).toString() +
                    ".DEM");
            outZ.putNextEntry(e);

            out = new FileCacheImageOutputStream(outZ, null);
        }

        out.setByteOrder(java.nio.ByteOrder.BIG_ENDIAN);
        ImageIO.write(((PlanarImage) gc.getRenderedImage()).getAsBufferedImage(),
            "raw", out);

        if (dest instanceof File) {
            out.close();
        } else {
            ((ZipOutputStream) dest).closeEntry();
        }
    }

    /**
     * @see org.opengis.coverage.grid.GridCoverageWriter#dispose()
     */
    public void dispose() throws IOException {
        // @todo Auto-generated method stub
    }
}
