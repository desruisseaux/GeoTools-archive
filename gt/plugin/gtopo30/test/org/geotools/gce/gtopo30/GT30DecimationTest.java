/**
 * 
 */
package org.geotools.gce.gtopo30;

import java.awt.Rectangle;
import java.net.URL;

import javax.media.jai.JAI;
import javax.media.jai.RecyclingTileFactory;
import javax.media.jai.TileCache;

import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.data.coverage.grid.AbstractGridCoverage2DReader;
import org.geotools.data.coverage.grid.AbstractGridFormat;
import org.geotools.resources.TestData;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Purpose of this class is testing the ability of this plug in to read and
 * write back the in gtopo30 format.
 * 
 * @author Simone Giannecchini
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/plugin/gtopo30/test/org/geotools/gce/gtopo30/GT30ReaderWriterTest.java $
 */
public class GT30DecimationTest extends GT30TestBase {
	/**
	 * Constructor for GT30ReaderTest.
	 * 
	 * @param arg0
	 */
	public GT30DecimationTest(String arg0) {
		super(arg0);
	}

	/**
	 * Testing reader and writer for gtopo. This test first of all read an
	 * existing gtopo tessel into a coverage object, therefore it writes it back
	 * onto the disk. Once the coverage is written back\ it loads it again
	 * building a new coverage which is finally visualized.
	 * 
	 * @throws Exception
	 */
	public void test() throws Exception {

		URL statURL = TestData.url(this, (new StringBuffer(this.fileName)
				.append(".DEM").toString()));
		AbstractGridFormat format = (AbstractGridFormat) new GTopo30FormatFactory()
				.createFormat();

		// using a big tile cache
		final JAI jaiDef = JAI.getDefaultInstance();
		final TileCache cache = jaiDef.getTileCache();
		cache.setMemoryCapacity(128 * 1024 * 1024);
		cache.setMemoryThreshold(1.0f);
		// final TCTool tool= new TCTool();

		// setting JAI wide hints
		jaiDef.setRenderingHint(JAI.KEY_CACHED_TILE_RECYCLING_ENABLED,
				Boolean.TRUE);

		// tile factory and recycler
		final RecyclingTileFactory recyclingFactory = new RecyclingTileFactory();
		jaiDef.setRenderingHint(JAI.KEY_TILE_FACTORY, recyclingFactory);
		jaiDef.setRenderingHint(JAI.KEY_TILE_RECYCLER, recyclingFactory);

		if (format.accepts(statURL)) {

			/**
			 * 
			 * STEP 1 Reading the coverage into memory in order to write it down
			 * again
			 * 
			 */
			// get a reader
			AbstractGridCoverage2DReader reader = (AbstractGridCoverage2DReader) format
					.getReader(statURL);

			// get a grid coverage
			final ParameterValueGroup params = reader.getFormat()
					.getReadParameters();
			params
					.parameter(
							AbstractGridFormat.READ_GRIDGEOMETRY2D.getName()
									.toString()).setValue(
							new GridGeometry2D(new GeneralGridRange(
									new Rectangle(0, 0, 640, 480)), reader
									.getOriginalEnvelope()));
			gc = ((GridCoverage2D) reader.read((GeneralParameterValue[]) params
					.values().toArray(new GeneralParameterValue[1])));
			if(TestData.isInteractiveTest())
				gc.show();
			else
				gc.getRenderedImage().getData();

			// logging some info
			logger.info(gc.getCoordinateReferenceSystem2D().toWKT());
			logger.info(gc.toString());

		}
	}

	public static final void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(GT30DecimationTest.class);
	}

}
