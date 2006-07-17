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
package org.geotools.gce.imageio.asciigrid;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
import javax.media.jai.TileCache;
import javax.media.jai.operator.ScaleDescriptor;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.geotools.resources.TestData;

public class TestJaiOperations extends TestCase implements WindowListener {
	// Booleans used to allows testing Operations
	static final boolean _testJaiImage_GiantIMAGEReadOperation = !true;

	static final boolean _testJaiImage_TestSubsamplingOperation = true;

	static final boolean _GiantIMAGEScaled = !true;

	static final boolean _GiantIMAGECropped = !true;

	static final boolean _testJaiImage_CheckerBoardReadOperation = !true;

	static final boolean _SubSampling = !true;

	static final boolean _testJaiImage_ReadFileOperation = !true;

	static final boolean _testJaiImage_ReadWriteFileOperation = !true;

	static final boolean _GZIPPED = !true;

	static final boolean _testJaiImage_ReadGrassGzWriteFileOperation = !true;

	static final boolean _testJaiImage_ReadUrlOperation = !true;

	static final boolean _testJaiImage_ReadGzStreamOperation = !true;

	static final boolean _testJaiImage_ReadGrassGzOperation = !true;

	static final boolean _testJaiImage_ReadStreamOperation = !true;

	private Logger logger = Logger
			.getLogger(TestJaiOperations.class.toString());

	private RenderedImage usedImage;

	protected void setUp() throws Exception {
		ImageIO.setUseCache(false);
		final TileCache cache = JAI.getDefaultInstance().getTileCache();
		cache.setMemoryCapacity(200 * 1024 * 1024);
		cache.setMemoryThreshold(1.0f);
		super.setUp();
	}

	public static void main(String[] args) {
		TestRunner.run(TestJaiOperations.class);
	}

	/**
	 * Giant Image
	 */
	public void testJaiImageGiantIMAGEReadOperation()
			throws FileNotFoundException, IOException {
		if (_testJaiImage_GiantIMAGEReadOperation) {
			final String title = new String(
					"testJaiImageGiantImageReadOperation");
			logger.info("\n\n " + title + " \n");

			final File f = TestData.file(this, "giantrowsimage.asc");

			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			pbjImageRead.setParameter("Input", f);

			final RenderedOp image = JAI.create("ImageRead", pbjImageRead);

			if (_GiantIMAGEScaled) {
				visualizeScaled(image, title);
			} else if (_GiantIMAGECropped) {
				visualizeCropped(image, title);
			} else {
				visualize(image, title);
			}
		}
	}

	/**
	 * Test Subsampling Image
	 */
	public void testJaiImageTestSubsamplingOperation()
			throws FileNotFoundException, IOException {
		if (_testJaiImage_TestSubsamplingOperation) {
			// final TCTool tct = new TCTool();
			final String title = new String("TestSubsamplingOperation");
			logger.info("\n\n " + title + " \n");

			final File f = TestData.file(this, "af0500ag.asc");
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			RenderedOp image;
			// pbjImageRead.setParameter("Input", f);
			//
			// image = JAI.create("ImageRead", pbjImageRead);
			// visualize(image, title + " Original Image");

			// ImageReadParam irp1 = new ImageReadParam();
			// irp1.setSourceSubsampling(2, 2, 0, 0);
			// pbjImageRead.setParameter("readParam", irp1);
			// pbjImageRead.setParameter("Input", f);
			// image = JAI.create("ImageRead", pbjImageRead);
			// visualize(image, title + " xfactor=2 & yfactor=2");

			ImageReadParam irp2 = new ImageReadParam();
			irp2.setSourceRegion(new Rectangle(4000, 4000, 3000, 3000));
			irp2.setSourceSubsampling(10, 10, 0, 0);
			pbjImageRead.setParameter("readParam", irp2);
			pbjImageRead.setParameter("Input", f);
			image = JAI.create("ImageRead", pbjImageRead);
			visualize(image, title + " xfactor=3 & yfactor=9");
			//
			// ImageReadParam irp3 = new ImageReadParam();
			// irp3.setSourceSubsampling(11, 2, 0, 0);
			// pbjImageRead.setParameter("readParam", irp3);
			// pbjImageRead.setParameter("Input", f);
			// image = JAI.create("ImageRead", pbjImageRead);
			// visualize(image, title + " xfactor=11 & yfactor=2");

		}
	}

	/**
	 * CheckerBoard Image
	 */
	public void testJaiImage_CheckerBoardReadOperation()
			throws FileNotFoundException, IOException {
		if (_testJaiImage_CheckerBoardReadOperation) {
			final String title = new String(
					"testJaiImageCheckerBoardReadOperation");
			logger.info("\n\n " + title + " \n");

			final File f = TestData.file(this, "checkerboard.asc");
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			pbjImageRead.setParameter("Input", f);

			if (_SubSampling) {
				ImageReadParam irp = new ImageReadParam();
				irp.setSourceSubsampling(3, 1, 0, 0);
				pbjImageRead.setParameter("readParam", irp);
			}

			final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			visualize(image, title);
		}
	}

	/**
	 * Read File
	 */
	public void testJaiImageReadFileOperation() throws FileNotFoundException,
			IOException {
		if (_testJaiImage_ReadFileOperation) {
			final String title = new String("testJaiImageReadFileOperation");
			logger.info("\n\n " + title + " \n");

			final File f = TestData.file(this, "arcGrid.asc");
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			pbjImageRead.setParameter("Input", f);

			final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			visualize(image, title);
		}
	}

	/**
	 * Read-Write File
	 */
	public void testJaiImageReadWriteFileOperation()
			throws FileNotFoundException, IOException {
		if (_testJaiImage_ReadWriteFileOperation) {
			// TCTool tct=new TCTool();
			final String title1 = new String(
					"testJaiImageReadWriteFileOperation");
			logger.info("\n\n " + title1 + " \n");

			// ////////////////////////////////////////////////////////////////
			//
			// reading source image
			//
			// ////////////////////////////////////////////////////////////////
			final File f = TestData.file(this, "checkerboard.asc");
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			pbjImageRead.setParameter("Input", f);
			final RenderedOp image = JAI.create("ImageRead", pbjImageRead);

			// ////////////////////////////////////////////////////////////////
			//
			// preparing to write
			//
			// ////////////////////////////////////////////////////////////////
			final File foutput = TestData.temp(this, "provetta.asc");
			final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
					"ImageWrite");
			pbjImageWrite.setParameter("Output", foutput);
			pbjImageWrite.addSource(image);

			if (_GZIPPED) {
				Iterator it = ImageIO.getImageWritersBySuffix("asc");
				ImageWriter writer;

				if (it.hasNext()) {
					writer = (ImageWriter) it.next();

					ImageWriteParam param = writer.getDefaultWriteParam();
					param.setCompressionMode(ImageWriteParam.MODE_DEFAULT);
					pbjImageWrite.setParameter("WriteParam", param);
				}
			}

			final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
			// final ImageWriter w = (ImageWriter) op
			// .getProperty(ImageWriteDescriptor.PROPERTY_NAME_IMAGE_WRITER);
			// w.dispose();

			// ////////////////////////////////////////////////////////////////
			//
			// Reading back the just written image
			//
			// ////////////////////////////////////////////////////////////////
			final ParameterBlockJAI pbjImageReRead = new ParameterBlockJAI(
					"ImageRead");
			pbjImageReRead.setParameter("Input", foutput);
			final RenderedOp image2 = JAI.create("ImageRead", pbjImageReRead);
			visualize(image2, title1 + "_");
		}
	}

	/**
	 * Write File
	 */
	public void testJaiImage_ReadGrassGzWriteFileOperation()
			throws FileNotFoundException, IOException {
		if (_testJaiImage_ReadGrassGzWriteFileOperation) {
			final String title = new String("testJaiImageReadGrassGzOperation");
			logger.info("\n\n " + title + " \n");

			final File f = TestData.file(this, "spearfish.asc.gz");
			final GZIPInputStream stream = new GZIPInputStream(
					new FileInputStream(f));
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			pbjImageRead.setParameter("Input", stream);

			final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			visualize(image, "AA");
			final File foutput = new File(new String("c:\\provetta.asc"));
			final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
					"ImageWrite");
			pbjImageWrite.setParameter("Output", foutput);
			pbjImageWrite.addSource(image);

			JAI.create("ImageWrite", pbjImageWrite);
		}
	}

	/**
	 * Read Url
	 */
	public void testJaiImageReadUrlOperation() throws FileNotFoundException,
			IOException {
		if (_testJaiImage_ReadUrlOperation) {
			final String title = new String("testJaiImageReadUrlOperation");
			logger.info("\n\n " + title + " \n");

			final URL url = new URL("**INSERT HERE A VALID URL**"); // TODO Set

			// a Valid URL
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			pbjImageRead.setParameter("Input", url);

			final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			visualize(image, title);
		}
	}

	/**
	 * Read GZ Stream
	 */
	public void testJaiImageReadGzStreamOperation()
			throws FileNotFoundException, IOException {
		if (_testJaiImage_ReadGzStreamOperation) {
			final String title = new String("testJaiImageReadGzStreamOperation");
			logger.info("\n\n " + title + " \n");

			final File f = TestData.file(this, "vandem.asc.gz");
			final GZIPInputStream stream = new GZIPInputStream(
					new FileInputStream(f));
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			pbjImageRead.setParameter("Input", stream);

			final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			visualize(image, title);
		}
	}

	/**
	 * Read GrassGZ
	 */
	public void testJaiImageReadGrassGzOperation()
			throws FileNotFoundException, IOException {
		if (_testJaiImage_ReadGrassGzOperation) {
			final String title = new String("testJaiImageReadGrassGzOperation");
			logger.info("\n\n " + title + " \n");

			final File f = TestData.file(this, "spearfish.asc.gz");
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			pbjImageRead.setParameter("Input", f);

			final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			visualize(image, title);
		}
	}

	/**
	 * Read Stream
	 */
	public void testJaiImageReadStreamOperation() throws FileNotFoundException,
			IOException {
		if (_testJaiImage_ReadStreamOperation) {
			final String title = new String("testJaiImageReadStreamOperation");
			logger.info("\n\n " + title + " \n");

			final File f = TestData.file(this, "arcGrid.asc");
			final InputStream stream = new FileInputStream(f);
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			pbjImageRead.setParameter("Input", stream);
			pbjImageRead.setParameter("VerifyInput", Boolean.FALSE);

			final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			visualize(image, title);
		}
	}

	/**
	 * Visualization Methods
	 */
	private void visualize(final RenderedImage bi, String test) {
		final JFrame frame = new JFrame(test);
		frame.getContentPane().add(new ScrollingImagePanel(bi, 800, 600));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(test);
		frame.setSize(new Dimension(400, 300));
		frame.pack();
		frame.show();
	}

	private void visualizeCropped(final RenderedImage bi, String test) {
		final JFrame frame = new JFrame("test");

		final ParameterBlockJAI pbjCrop = new ParameterBlockJAI("Crop");
		pbjCrop.addSource(bi);
		pbjCrop.setParameter("x", new Float(125));
		pbjCrop.setParameter("y", new Float(125));
		pbjCrop.setParameter("width", new Float(500));
		pbjCrop.setParameter("height", new Float(4000));

		final RenderedOp crop = JAI.create("Crop", pbjCrop);
		crop.getWidth();

		frame.getContentPane().add(
				new ScrollingImagePanel(crop.getCurrentRendering(), 1024, 768));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(test);
		frame.pack();
		frame.show();
	}

	private void visualizeScaled(final RenderedImage bi, String test) {
		final JFrame frame = new JFrame("test");
		this.usedImage = bi;

		final RenderedOp image = ScaleDescriptor.create(bi, new Float(0.1),
				new Float(0.1), new Float(0), new Float(0),
				new InterpolationNearest(), null);
		image.getWidth();

		frame.getContentPane()
				.add(
						new ScrollingImagePanel(image.getCurrentRendering(),
								1024, 768));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(test);
		frame.addWindowListener(this);
		frame.pack();
		frame.show();
	}

	private void visualizeBuffered(final RenderedImage bi, String test) {
		final JFrame frame = new JFrame("test");

		frame.getContentPane().add(
				new JLabel(new ImageIcon(((PlanarImage) bi)
						.getAsBufferedImage())));

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(test);
		frame.pack();
		frame.show();
	}

	public RuntimeException windowClosingDelivered(WindowEvent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public RuntimeException windowClosingNotify(WindowEvent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void windowClosed(WindowEvent arg0) {
		((PlanarImage) this.usedImage).dispose();
	}

	public void windowClosing(WindowEvent arg0) {
		((PlanarImage) this.usedImage).dispose();
	}

	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
	}
}
