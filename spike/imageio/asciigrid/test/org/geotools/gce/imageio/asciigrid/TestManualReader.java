package org.geotools.gce.imageio.asciigrid;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.geotools.gce.imageio.asciigrid.AsciiGridsImageReader;
import org.geotools.gce.imageio.asciigrid.spi.AsciiGridsImageReaderSpi;
import org.geotools.resources.TestData;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.sun.media.jai.widget.DisplayJAI;

public class TestManualReader extends TestCase {
	private Logger logger = Logger.getLogger(TestManualReader.class.toString());
	
	//booleans used to allows testing operations

	static final boolean _testManualMetadataOperations = !true;
	
	static final boolean _testManualDirectUseOfSPI = !true;

	static final boolean _testManualbySuffixAscReader = !true;

	static final boolean _testManualUrlReader = false;

	static final boolean _testManualFileChangingReader = !true;

	static final boolean _testManualFileReader = !true;

	static final boolean _testManualGzUrlReader = false;

	static final boolean _testManualGzFileReader = !true;

	static final boolean _testManualStreamReader = true;

	static final boolean _testManualGzStreamReader = !true;

	static final boolean _testManualOriginalGzStreamReader = !true;

	static final boolean _testManualImageInputStream = !true;

	static final boolean _testManualGzGrassReader = !true;

	static final boolean _testManualGrassReader = false;

	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {

		TestRunner.run(TestManualReader.class);
	}
	
	public void testManualMetadataOperations() throws Exception {
		if (_testManualMetadataOperations) {
			try {
				final String title = new String("testManualMetadataOperations");
				logger.info("\n\n " + title + " \n");
				AsciiGridsImageReader reader = new AsciiGridsImageReader(
						new AsciiGridsImageReaderSpi());
				final File f = TestData.file(this, "arcGrid.asc");
				reader.setInput(f);
				IIOMetadata metadata= reader.getImageMetadata(0);
				String nativeFormatName = metadata.getNativeMetadataFormatName();
				Node node = metadata.getAsTree(nativeFormatName);
				displayMetadata(node);
				
				final BufferedImage bi = reader.read(0, null);
				visualize(bi, title);
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualDirectUseOfSPI() throws Exception {
		if (_testManualDirectUseOfSPI) {
			try {
				final String title = new String("testManualDirectUseOfSPI");
				logger.info("\n\n " + title + " \n");
				AsciiGridsImageReader reader = new AsciiGridsImageReader(
						new AsciiGridsImageReaderSpi());
				final File f = TestData.file(this, "arcGrid.asc");
				reader.setInput(f);
				final BufferedImage bi = reader.read(0, null);
				visualize(bi, title+f.getPath());
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualbySuffixAscReader() throws Exception {
		if (_testManualbySuffixAscReader) {
			try {
				final String title = new String("testManualbySuffixAscReader");
				logger.info("\n\n " + title + " \n");
				Iterator it = ImageIO.getImageReadersBySuffix("asc");
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					final File f = TestData.file(this, "ArcGrid.asc");
					reader.setInput(f);
					final BufferedImage bi = reader.read(0, null);
					visualize(bi, title+f.getPath());
				}
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualUrlReader() throws Exception {
		if (_testManualUrlReader) {
			try {
				final String title = new String("testManualURLReader");
				logger.info("\n\n " + title + " \n");
				final URL url = new URL(
						"**INSERT HERE A VALID URL**");//TODO Set a Valid URL
				Iterator it = ImageIO.getImageReaders(url);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					reader.setInput(url);
					final BufferedImage bi = reader.read(0, null);
					visualize(bi, title);
				}
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualFileChangingReader() throws Exception {
		if (_testManualFileChangingReader) {
			try {
				final String title = new String("testManualFileChangingReader");
				logger.info("\n\n " + title + " \n");
				final File f = TestData.file(this, "ArcGrid.asc");
				final File f2 = TestData.file(this, "spearfish.asc.gz");
				Iterator it = ImageIO.getImageReaders(f);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					reader.setInput(f2);
					final BufferedImage bi = reader.read(0, null);
					visualize(bi, title+f2.getPath());
				}
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualFileReader() throws Exception {
		if (_testManualFileReader) {
			try {
				final String title = new String("testManualFileReader");
				logger.info("\n\n " + title + " \n");
				final File f = TestData.file(this, "ArcGrid.asc");
				Iterator it = ImageIO.getImageReaders(f);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					reader.setInput(f);
					final BufferedImage bi = reader.read(0, null);
					visualize(bi, title+f.getPath());
				}
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualGzUrlReader() throws Exception {
		if (_testManualGzUrlReader) {
			try {
				final String title = new String("testManualGzURLReader");
				logger.info("\n\n " + title + " \n");
				final URL url = new URL(
						"**INSERT HERE A VALID URL//GZipped.asc.gz**");//TODO Set a Valid URL
				Iterator it = ImageIO.getImageReaders(url);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					reader.setInput(url);
					final BufferedImage bi = reader.read(0, null);
					visualize(bi, title);
				}
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualGzFileReader() throws Exception {
		if (_testManualGzFileReader) {
			try {
				final String title = new String("testManualGzFileReader");
				logger.info("\n\n " + title + " \n");
				final File f = TestData.file(this, "spearfish.asc.gz");
				Iterator it = ImageIO.getImageReaders(f);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					reader.setInput(f);
					final BufferedImage bi = reader.read(0, null);
					visualize(bi, title+f.getPath());
				}
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualStreamReader() throws Exception {
		if (_testManualStreamReader) {
			try {
				final String title = new String("testManualStreamReader");
				logger.info("\n\n " + title + " \n");
				
				final File f = TestData.file(this, "ArcGrid.asc");
				InputStream is = new FileInputStream(f);
				Iterator it = ImageIO.getImageReaders(is);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					reader.setInput(is);
					final BufferedImage bi = reader.read(0, null);
					visualize(bi, title+f.getPath());
				}
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualGzStreamReader() throws Exception {
		if (_testManualGzStreamReader) {
			try {
				final String title = new String("testManualGzStreamReader");
				logger.info("\n\n " + title + " \n");
				final File f = TestData.file(this, "spearfish.asc.gz");
				InputStream is = new FileInputStream(f);
				Iterator it = ImageIO.getImageReaders(is);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					reader.setInput(is);
					final BufferedImage bi = reader.read(0, null);
					visualize(bi, title+f.getPath());
				}
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualOriginalGzStreamReader() throws Exception {
		if (_testManualOriginalGzStreamReader) {
			try {
				final String title = new String(
						"testManualOriginalGzStreamReader");
				logger.info("\n\n " + title + " \n");
				final File f = TestData.file(this, "spearfish.asc.gz");
				final GZIPInputStream stream = new GZIPInputStream(
						new FileInputStream(f));
				Iterator it = ImageIO.getImageReaders(stream);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					reader.setInput(stream);
					final BufferedImage bi = reader.read(0, null);
					visualize(bi, title+f.getPath());
				}
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualImageInputStream() throws Exception {
		if (_testManualImageInputStream) {
			try {
				final String title = new String("testManualImageInputStream");
				logger.info("\n\n " + title + " \n");
				final File f = TestData.file(this, "ArcGrid.asc");
				ImageInputStream iis = new FileImageInputStream(f);
				Iterator it = ImageIO.getImageReaders(iis);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();

					reader.setInput(iis);
					final BufferedImage bi = reader.read(0, null);
					visualize(bi, title+f.getPath());
				}
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualGzGrassReader() throws Exception {
		if (_testManualGzGrassReader) {
			try {
				final String title = new String("testManualGzGrassReader");
				logger.info("\n\n " + title + " \n");
				final File f = TestData.file(this, "spearfish.asc.gz");
				Iterator it = ImageIO.getImageReaders(f);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					reader.setInput(f);
					final BufferedImage bi = reader.read(0, null);
					visualize(bi, title+f.getPath());
				}
			} catch (Exception e) {
				Exception e1 = new Exception();
				e1.initCause(e);
				throw e1;
			}
		}
	}

	public void testManualGrassReader() throws Exception {
		if (_testManualGrassReader) {
			try {
				final String title = new String("testManualGrassReader");
				logger.info("\n\n " + title + " \n");
				final File f = TestData.file(this, "spearfish_dem.arx");
				Iterator it = ImageIO.getImageReaders(f);
				if (it.hasNext()) {
					AsciiGridsImageReader reader = (AsciiGridsImageReader) it
							.next();
					reader.setInput(f);
					final BufferedImage bi = reader.read(0, null);
					visualize(bi, title);
				}
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
	
	
	public void displayMetadata(Node root) {
		displayMetadata(root, 0);
	}

	void indent(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("  ");
		}
	} 

	void displayMetadata(Node node, int level) {
		indent(level); // emit open tag
		System.out.print("<" + node.getNodeName());
		NamedNodeMap map = node.getAttributes();
		if (map != null) { // print attribute values
			int length = map.getLength();
			for (int i = 0; i < length; i++) {
				Node attr = map.item(i);
				System.out.print(" " + attr.getNodeName() +
				                 "=\"" + attr.getNodeValue() + "\"");
			}
		}

		Node child = node.getFirstChild();
		if (child != null) {
			System.out.println(">"); // close current tag
			while (child != null) { // emit child tags recursively
				displayMetadata(child, level + 1);
				child = child.getNextSibling();
			}
			indent(level); // emit close tag
			System.out.println("</" + node.getNodeName() + ">");
		} else {
			System.out.println("/>");
		}
	}

}
