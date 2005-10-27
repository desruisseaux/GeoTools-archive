package org.geotools.brewer.color;

import junit.framework.TestCase;

public class PaletteTest extends TestCase {
	ColorBrewer brewer;

	public void testSequential() {
		brewer = new ColorBrewer();
		brewer.loadPalettes(ColorBrewer.SEQUENTIAL);
		String[] names = brewer.getPaletteNames();
		assertEquals(true, names.length != 0);
		assertNotNull(brewer.getDescription()); //we have a description!
		BrewerPalette palette = brewer.getPalette("YlGnBu");
		assertNotNull(palette); //we have a palette!
		assertNotNull(palette.getDescription()); //we have another description!
	}

	public void testDiverging() {
		brewer = new ColorBrewer();
		brewer.loadPalettes(ColorBrewer.DIVERGING);
		String[] names = brewer.getPaletteNames();
		assertEquals(true, names.length != 0);
		assertNotNull(brewer.getDescription()); //we have a description!
		BrewerPalette palette = brewer.getPalette("PuOr");
		assertNotNull(palette); //we have a palette!
		assertNotNull(palette.getDescription()); //we have another description!
	}

	public void testQualitative() {
		brewer = new ColorBrewer();
		brewer.loadPalettes(ColorBrewer.QUALITATIVE);
		String[] names = brewer.getPaletteNames();
		assertEquals(true, names.length != 0);
		assertNotNull(brewer.getDescription()); //we have a description!
		BrewerPalette palette = brewer.getPalette("Set3");
		assertNotNull(palette); //we have a palette!
		assertNotNull(palette.getDescription()); //we have another description!
	}
}
