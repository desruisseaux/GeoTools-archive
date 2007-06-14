package it.geosolutions.imageio.plugins.jhdf;

import it.geosolutions.imageio.plugins.jhdf.aps.APSImageMetadata;
import it.geosolutions.imageio.plugins.jhdf.aps.APSStreamMetadata;
import it.geosolutions.imageio.stream.output.FileImageOutputStreamExtImpl;
import it.geosolutions.resources.TestData;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.widget.ScrollingImagePanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriterSpi;
import com.sun.media.jai.operator.ImageReadDescriptor;

public class JHDFTest extends TestCase {
	public JHDFTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		JAI.getDefaultInstance().getTileCache().setMemoryCapacity(
				64 * 1024 * 1024);
		JAI.getDefaultInstance().getTileCache().setMemoryThreshold(1.0f);
		JAI.getDefaultInstance().getTileScheduler().setParallelism(1);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchPriority(2);
		JAI.getDefaultInstance().getTileScheduler().setPrefetchParallelism(1);
	}

	public void testMetadata() throws IOException {
		final File file = TestData.file(this,"MODPM2007027121858.L3_000_EAST_MED");
		final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
		"ImageRead");
		pbjImageRead.setParameter("Input", file);
		final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		ImageReader reader = (ImageReader) image
				.getProperty(ImageReadDescriptor.PROPERTY_NAME_IMAGE_READER);
	
		final IIOMetadata metadata = reader.getImageMetadata(2);
		IIOMetadataNode imageNode = (IIOMetadataNode) metadata
				.getAsTree(APSImageMetadata.nativeMetadataFormatName);
		System.out
				.println(MetadataDisplay.buildMetadataFromNode(imageNode));

		final IIOMetadata streamMetadata = reader.getStreamMetadata();
		IIOMetadataNode streamNode = (IIOMetadataNode) streamMetadata
				.getAsTree(APSStreamMetadata.nativeMetadataFormatName);
		System.out.println(MetadataDisplay
				.buildMetadataFromNode(streamNode));
	}
	
	public void testJaiRead() throws IOException {
		final File file = TestData.file(this,"MODPM2007027121858.L3_000_EAST_MED");
		for (int i = 3; i < 4; i++) {
			final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
					"ImageRead");
			ImageReadParam irp = new ImageReadParam();
			irp.setSourceSubsampling(2, 2, 0, 0);
			pbjImageRead.setParameter("Input", file);
			pbjImageRead.setParameter("readParam", irp);
			pbjImageRead.setParameter("imageChoice", Integer.valueOf(i));
			
			final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
			
			final File outputFile = TestData.temp(this, "WriteHDFData"+i, false);
			final ParameterBlockJAI pbjImageWrite = new ParameterBlockJAI(
					"ImageWrite");
			pbjImageWrite.setParameter("Output",
					new FileImageOutputStreamExtImpl(outputFile));
			ImageWriter writer = new TIFFImageWriterSpi()
					.createWriterInstance();
			pbjImageWrite.setParameter("Writer", writer);

			// Specifying image source to write
			pbjImageWrite.addSource(image);

			// Writing
			final RenderedOp op = JAI.create("ImageWrite", pbjImageWrite);
		}

	}

	public void testJaiMultithreadingRead() throws IOException {
		// SETS JHDFImageReaderSpi as SPI in
		// META-INF/services/javax.imageio.spi.ImageReaderSpi

		final File file = TestData.file(this,
				"MODPM2007027122358.L3_000_EAST_MED");
		ImageReadParam irp = new ImageReadParam();
		irp.setSourceSubsampling(1, 1, 0, 0);
		// irp.setSourceRegion(new Rectangle(0, 512, 1024, 1024));
		int i = 2;
		final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
				"ImageRead");

		pbjImageRead.setParameter("Input", file);
		pbjImageRead.setParameter("readParam", irp);
		pbjImageRead.setParameter("imageChoice", Integer.valueOf(i));
		final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
		visualize(image, "");
	}

	public void testManualRead() throws IOException {
		final File file = TestData.file(this,
				"MODPM2007027122358.L3_000_EAST_MED");

		final JHDFImageReader reader = new JHDFImageReader(
				new JHDFImageReaderSpi());
		reader.setInput(file);

		for (int i = 0; i < 5; i++) {
			ImageReadParam irp2 = new ImageReadParam();
			irp2.setSourceSubsampling(1, 2, 0, 0);
			BufferedImage bi = null;
			try {
				bi = reader.read(i, irp2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			visualize(bi, "");
		}
	}

	/**
	 * Visualization Methods
	 */
	private static void visualize(final RenderedImage bi, String test) {
		final JFrame frame = new JFrame(test);
		frame.getContentPane().add(new ScrollingImagePanel(bi, 800, 600));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(test);
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				frame.pack();
				frame.show();
			}
		});
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTest(new JHDFTest("testJaiRead"));
		
		suite.addTest(new JHDFTest("testMetadata"));

		// suite.addTest(new JHDFTest("testJaiMultithreadingRead"));

		// suite.addTest(new JHDFTest("testManualRead"));

		return suite;
	}

	public static void main(java.lang.String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
