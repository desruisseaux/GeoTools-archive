package org.geotools.brewer.color;

import junit.framework.TestCase;

public class PaletteTest extends TestCase {
	ColorBrewer brewer;

	public void testSequential() {
		brewer = new ColorBrewer();
		brewer.loadPalettes("SEQUENTIAL");
		String[] names = brewer.getPaletteNames();
		assertEquals(true, names.length != 0);
		assertNotNull(brewer.getDescription()); //we have a description!
		BrewerPalette palette = brewer.getPalette("YlGnBu");
		assertNotNull(palette); //we have a palette!
		assertNotNull(palette.getDescription()); //we have another description!
	}

	public void testDiverging() {
		brewer = new ColorBrewer();
		brewer.loadPalettes("DIVERGING");
		String[] names = brewer.getPaletteNames();
		assertEquals(true, names.length != 0);
		assertNotNull(brewer.getDescription()); //we have a description!
		BrewerPalette palette = brewer.getPalette("PuOr");
		assertNotNull(palette); //we have a palette!
		assertNotNull(palette.getDescription()); //we have another description!
	}

	public void testQualitative() {
		brewer = new ColorBrewer();
		brewer.loadPalettes("QUALITATIVE");
		String[] names = brewer.getPaletteNames();
		assertEquals(true, names.length != 0);
		assertNotNull(brewer.getDescription()); //we have a description!
		BrewerPalette palette = brewer.getPalette("Set3");
		assertNotNull(palette); //we have a palette!
		assertNotNull(palette.getDescription()); //we have another description!
	}
	
	public void testAll() throws Exception {
		//load all palettes (defaults)
		brewer = new ColorBrewer();
		brewer.loadPalettes();
		BrewerPalette[] pal = brewer.getPalettes();  
		assertEquals(35, pal.length);
		BrewerPalette[] palettes;
		
		//test palette filtering abilities
		palettes = brewer.getPalettes(ColorBrewer.SUITABLE_RANGED);
		assertEquals(27, palettes.length);
		palettes = brewer.getPalettes(ColorBrewer.SUITABLE_UNIQUE);
		assertEquals(8, palettes.length);
		palettes = brewer.getPalettes(ColorBrewer.DIVERGING);
		assertEquals(9, palettes.length);
		palettes = brewer.getPalettes(ColorBrewer.SEQUENTIAL);
		assertEquals(18, palettes.length);
		palettes = brewer.getPalettes(ColorBrewer.QUALITATIVE);
		assertEquals(8, palettes.length);
		palettes = brewer.getPalettes(ColorBrewer.ALL);
		assertEquals(35, palettes.length);
		
		palettes = brewer.getPalettes(new PaletteType(true, false));
		assertEquals(27, palettes.length);
		palettes = brewer.getPalettes(new PaletteType());
		assertEquals(35, palettes.length);
		palettes = brewer.getPalettes(new PaletteType(false, false));
		assertEquals(0, palettes.length);
		
		palettes = brewer.getPalettes(ColorBrewer.QUALITATIVE, 9);
		assertEquals(4, palettes.length);
	}
}
