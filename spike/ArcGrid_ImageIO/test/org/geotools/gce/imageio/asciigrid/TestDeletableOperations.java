package org.geotools.gce.imageio.asciigrid;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class TestDeletableOperations extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public static void main(String[] args) {

		TestRunner.run(TestDeletableOperations.class);
	}

	public void testMakeFile() throws Exception {
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(
				"D:\\prova2.asc")));
		out.write("ncols 120\r\n");
		out.write("nrows 1000\r\n");
		out.write("xllcorner 122222\r\n");
		out.write("yllcorner 45001\r\n");
		out.write("cellsize 250.0\r\n");
		out.write("NODATA_value 1.70141E38\r\n\r\n");
		String s;
		Integer integer;
		final int size = 120 * 1000;
		int i = 0;
		for (i = 1; i <= size; i++) {
			if (i % 30 > 0 && i % 30 <= 15)
				out.write("-");
			out.write(Integer.toString(i));
			out.write(" ");
			if (i % 15 == 0)
				out.write("\r\n");
			if (i % 300 == 0)
				out.write("\r\n");
		}
		out.write("\r\n");
		out.close();

	}

	// public void testMakeFile() throws Exception {
	// BufferedWriter out = new BufferedWriter(new FileWriter(new File(
	// "D:\\prova.asc")));
	//
	// out.write("ncols 30\r\n");
	// out.write("nrows 294\r\n");
	// out.write("xllcorner 122222\r\n");
	// out.write("yllcorner 45001\r\n");
	// out.write("cellsize 250.0\r\n");
	// out.write("NODATA_value 1.70141E38\r\n\r\n");
	//		
	// String s;
	// Integer integer;
	// final int size=30*294;
	// int i=0;
	// for (i=1;i<=size;i++){
	// if(i%30>0 && i%30<=15)
	// out.write("-");
	// out.write(Integer.toString(i));
	// out.write(" ");
	// if (i%15==0)
	// out.write("\r\n");
	// if (i%300==0)
	// out.write("\r\n");
	// }
	// out.write("\r\n");
	// out.close();
	// }

	// public void testMakeBigFile() throws Exception {
	// BufferedWriter out = new BufferedWriter(new FileWriter(new File(
	// "D:\\strapuppa.asc")));
	//
	// final int ncols = 2000;
	// final int nrows = 8000;
	//
	// out.write("ncols " + ncols + "\r\n");
	// out.write("nrows " + nrows + "\r\n");
	// out.write("xllcorner 122222\r\n");
	// out.write("yllcorner 45001\r\n");
	// out.write("cellsize 250.0\r\n");
	// out.write("NODATA_value 1.70141E38\r\n\r\n");
	//
	// final int size = ncols * nrows;
	//
	// String s, s2;
	// int index = 0;
	// float val = 0;
	// for (int i = 0; i < nrows; i++) {
	//
	// for (int j = 0; j < ncols; j++) {
	// if ((i / 400) % 2 == 0) {
	// if ((j / 250) % 2 == 0)
	// val = 0;
	// else
	// val = 1;
	// } else {
	// if ((j / 250) % 2 == 0)
	// val = 1;
	// else
	// val = 0;
	// }
	//
	// out.write(Float.toString(val));
	// if (j < ncols - 1)
	// out.write(" ");
	//
	// }
	//
	// out.write("\r\n");
	//
	// }
	// out.write("\r\n");
	// out.close();
	// }

	// public void testOtherFile() throws Exception {
	// TileCache cache = JAI.getDefaultInstance().getTileCache();
	// cache.setMemoryCapacity(100 * 1024 * 1024);
	// cache.setMemoryThreshold(1);
	// TCTool tcTool = new TCTool();
	// final File f = new File("D:\\provv2.tif");
	// // final RandomAccessFile rf = new RandomAccessFile(f, "r");
	// // final FileChannel ch = rf.getChannel();
	// final ParameterBlockJAI pbjImageRead = new ParameterBlockJAI(
	// "ImageRead");
	// pbjImageRead.setParameter("Input", f);
	// final RenderedOp image = JAI.create("ImageRead", pbjImageRead);
	//
	// final JFrame frame = new JFrame("test");
	//
	// final ParameterBlockJAI pbjCrop = new ParameterBlockJAI("Crop");
	// pbjCrop.addSource(image);
	// pbjCrop.setParameter("x", new Float(0));
	// pbjCrop.setParameter("y", new Float(1000));
	// pbjCrop.setParameter("width", new Float(5376));
	// pbjCrop.setParameter("height", new Float(5000));
	// final RenderedOp crop = JAI.create("Crop", pbjCrop);
	//
	// final ParameterBlockJAI pbjTranslate = new ParameterBlockJAI(
	// "Translate");
	// pbjTranslate.addSource(crop);
	// Float xt = new Float(-crop.getMinX());
	// Float yt = new Float(-crop.getMinY());
	// // Float xt = new Float(0);
	// // Float yt = new Float(40);
	// pbjTranslate.setParameter("xTrans", xt);
	// pbjTranslate.setParameter("yTrans", yt);
	// final RenderedOp image2 = JAI.create("Translate", pbjTranslate);
	//
	// final DisplayJAI disp = new DisplayJAI(image2);
	// disp.setSize(new Dimension(800, 600));
	// //
	//
	// // frame.getContentPane().add(new ScrollingImagePanel(image, 1024,
	// // 768));
	// // frame.getContentPane().add(new JLabel(new
	// // ImageIcon(image.getAsBufferedImage())));
	// frame.getContentPane().add(new JScrollPane(disp));
	//
	// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	// frame.setTitle("puppa");
	// // frame.setSize(new Dimension(800, 600));
	// frame.pack();
	// frame.show();
	//
	// }

	// public void testMakeFile() throws Exception {
	// BufferedWriter out = new BufferedWriter(new FileWriter(new File(
	// "D:\\prova.asc")));
	//
	// out.write("ncols 30\r\n");
	// out.write("nrows 294\r\n");
	// out.write("xllcorner 122222\r\n");
	// out.write("yllcorner 45001\r\n");
	// out.write("cellsize 250.0\r\n");
	// out.write("NODATA_value 1.70141E38\r\n\r\n");
	//		
	// String s;
	// Integer integer;
	// final int size=30*294;
	// int i=0;
	// for (i=1;i<=size;i++){
	// if(i%30>0 && i%30<=15)
	// out.write("-");
	// out.write(Integer.toString(i));
	// out.write(" ");
	// if (i%15==0)
	// out.write("\r\n");
	// if (i%300==0)
	// out.write("\r\n");
	// }
	// out.write("\r\n");
	// out.close();
	// }

	 public void testMakeBigFile() throws Exception {
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(
				"E:\\prova.asc")));

		final int ncols = 2000;
		final int nrows = 8000;

		out.write("ncols " + ncols + "\r\n");
		out.write("nrows " + nrows + "\r\n");
		out.write("xllcorner 122222\r\n");
		out.write("yllcorner 45001\r\n");
		out.write("cellsize 250.0\r\n");
		out.write("NODATA_value 1.70141E38\r\n\r\n");


		
		float val = 0;
		for (int i = 0; i < nrows; i++) {

			for (int j = 0; j < ncols; j++) {
				if ((i / 400) % 2 == 0) {
					if ((j / 250) % 2 == 0)
						val = 0;
					else
						val = 1;
				} else {
					if ((j / 250) % 2 == 0)
						val = 1;
					else
						val = 0;
				}

				out.write(Float.toString(val));
				if (j < ncols - 1)
					out.write(" ");

			}

			out.write("\r\n");

		}
		out.write("\r\n");
		out.close();
	}

	
	
	// public void testManualFileBadHeaderReader() throws Exception {
	// try {
	// logger.info("\n\ntestManualFileBadHeaderReader\n");
	// final File f = new File("D:\\arcGridbadHeader.asc");
	// Iterator it = ImageIO.getImageReaders(f);
	// if (it.hasNext()) {
	// AsciiGridsImageReader reader = (AsciiGridsImageReader) it
	// .next();
	// reader.setInput(f);
	// reader.read(0);
	//
	// // final BufferedImage bi = reader.read(0, null);
	// // visualize(bi);
	// }
	// } catch (Exception e) {
	// Exception e1 = new Exception();
	// e1.initCause(e);
	// throw e1;
	// }
	// }
	//
	// public void testArrayCopy() {
	// byte support[] = new byte[50];
	// for (int i = 0; i < 20; i++)
	// support[i] = 0;
	// for (int i = 20; i < 30; i++)
	// support[i] = 100;
	// for (int i = 30; i < 40; i++)
	// support[i] = 80;
	// for (int i = 40; i < 50; i++)
	// support[i] = 90;
	//
	// System.arraycopy(support, 20, support, 0, 30);
	// int j = 0;
	// j++;
	//
	// }
	//
	// public void testBufferedReading() throws IOException,
	// FileNotFoundException {
	// final File f = new File("D:\\vandem2.asc");
	// ImageInputStream fiis = ImageIO.createImageInputStream(f);
	// long startTime = System.currentTimeMillis();
	// byte b[] = new byte[8192];
	// int len = -1, i = 0, j = 0;
	//
	// while ((len = fiis.read(b)) != -1) {
	// for (j = 0; j < len; j++)
	// switch (b[j]) {
	// case 48:
	// case 49:
	// case 50:
	// case 51:
	// case 52:
	// case 53:
	// case 54:
	// case 55:
	// case 56:
	// case 57:
	// case 32:
	// case 10:
	// case 13:
	// case 46:
	// case 69:
	// case 43:
	// i++;
	// break;
	// }
	// if (i > 3000000)
	// break;
	// }
	// long endTime = System.currentTimeMillis();
	// long runTime = endTime - startTime;
	// System.out.println("runTime Buffered: " + runTime);
	// }
	//
	// public void testUnbufferedReading() throws IOException,
	// FileNotFoundException {
	// final File f = new File("D:\\vandem2.asc");
	// ImageInputStream fiis = ImageIO.createImageInputStream(f);
	// int i = 0, j = -1;
	// long startTime = System.currentTimeMillis();
	//
	// while ((j = fiis.read()) != -1) {
	// switch (j) {
	// case '0':
	// case '1':
	// case '2':
	// case '3':
	// case '4':
	// case '5':
	// case '6':
	// case '7':
	// case '8':
	// case '9':
	// case ' ':
	// case '\n':
	// case '\r':
	// case '-':
	// case '.':
	// case 'E':
	// i++;
	// break;
	//
	// }
	// if (i > 3000000)
	// break;
	// }
	// long endTime = System.currentTimeMillis();
	// long runTime = endTime - startTime;
	// logger.info("runTime NotBuffered: " + runTime);
	// }
}