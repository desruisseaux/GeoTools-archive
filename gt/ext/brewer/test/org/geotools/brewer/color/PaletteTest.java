package org.geotools.brewer.color;

import junit.framework.TestCase;

public class PaletteTest extends TestCase {
	ColorBrewer brewer;

	public void testSequential() {
		brewer = new ColorBrewer();
		brewer.loadPalettes(ColorBrewer.SEQUENTIAL);
		String[] names = brewer.getPaletteNames();
		assertEquals(true, names.length != 0);
	}
}
