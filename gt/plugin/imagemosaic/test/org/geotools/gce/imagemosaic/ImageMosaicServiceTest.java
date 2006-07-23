/**
 * 
 */
package org.geotools.gce.imagemosaic;

import java.util.Iterator;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.geotools.data.coverage.grid.GridFormatFactorySpi;
import org.geotools.data.coverage.grid.GridFormatFinder;

/**
 * @author Simone Giannecchini
 * 
 */
public class ImageMosaicServiceTest extends TestCase {

	/**
	 * 
	 */
	public ImageMosaicServiceTest() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestRunner.run(ImageMosaicServiceTest.class);

	}

	public void testIsAvailable() {
		Iterator list = GridFormatFinder.getAvailableFormats();
		boolean found = false;

		while (list.hasNext()) {
			final GridFormatFactorySpi fac = (GridFormatFactorySpi) list.next();

			if (fac instanceof ImageMosaicFormatFactory) {
				found = true;

				break;
			}
		}

		assertTrue("ImageMosaicFormatFactorySpi not registered", found);
	}
}
