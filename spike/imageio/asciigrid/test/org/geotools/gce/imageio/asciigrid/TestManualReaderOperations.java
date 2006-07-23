package org.geotools.gce.imageio.asciigrid;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.geotools.gce.imageio.asciigrid.AsciiGridsImageReader;
import org.geotools.resources.TestData;

import com.sun.media.jai.widget.DisplayJAI;

public class TestManualReaderOperations extends TestCase {
	private Logger logger = Logger.getLogger(TestManualReaderOperations.class
			.toString());

	// booleans used to allows testing operations

	static final boolean _test_ReadTile = false;

	static final boolean _test_ReadAsRenderedImage = false;

	static final boolean _test_ReadRaster = false;

	static final boolean _test_ReadTileRaster = false;

	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {

		TestRunner.run(TestManualReaderOperations.class);
	}

	public void testReadTile() throws Exception {
		if (_test_ReadTile) {
			try {
				final String title = new String("testReadTile");
				logger.info("\n\n " + title + " \n");
				final File f = new File("D:\\testing.asc");
				Iterator it = ImageIO.getImageReaders(f);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					reader.setInput(f);
					final BufferedImage bi = reader.readTile(0, 0, 3);
					visualize(bi, title);
				}

			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testReadAsRenderedImage() throws Exception {
		if (_test_ReadAsRenderedImage) {
			try {
				final String title = new String("testReadAsRenderedImage");
				logger.info("\n\n " + title + " \n");
				final File f = TestData.file(this,"testing.asc");
				Iterator it = ImageIO.getImageReaders(f);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					reader.setInput(f);
					final RenderedImage ri = reader
							.readAsRenderedImage(0, null);
					visualize(ri, title);
				}

			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testReadRaster() throws Exception {
		if (_test_ReadRaster) {
			try {
				final String title = new String("testReadRaster");
				logger.info("\n\n " + title + " \n");

			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testReadTileRaster() throws Exception {
		if (_test_ReadTileRaster) {
			try {
				final String title = new String("testReadTileRaster");
				logger.info("\n\n " + title + " \n");

			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	private void visualize(final RenderedImage bi, String test) {
		final JFrame frame = new JFrame("test");
		final DisplayJAI disp = new DisplayJAI(bi);
		disp.setSize(new Dimension(640, 480));
		frame.getContentPane().add(new JScrollPane(disp));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(test);
		frame.setSize(new Dimension(640, 480));
		frame.pack();
		frame.show();
	}
}
