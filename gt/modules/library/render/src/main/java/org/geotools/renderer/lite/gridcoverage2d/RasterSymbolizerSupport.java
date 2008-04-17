/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.renderer.lite.gridcoverage2d;

// J2SE dependencies
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.Category;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.util.NumberRange;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.filter.expression.Expression;

/**
 * A helper class for {@link GridCoverage} objects rendering SLD stylers
 * support.
 * 
 * @author Alessio Fabiani
 * @author Simone Giannecchini
 * 
 * @task Optimize and complete
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/coverages_branch/trunk/gt/module/render/src/org/geotools/renderer/lite/RasterSymbolizerSupport.java $
 */
public final class RasterSymbolizerSupport {
	/** The Styled Layer Descriptor * */
	private final RasterSymbolizer symbolizer;

	/**
	 * Constructor
	 * 
	 * @param symbolizer
	 * 
	 */
	public RasterSymbolizerSupport(final RasterSymbolizer symbolizer) {
		this.symbolizer = symbolizer;
	}

	public GridCoverage recolorCoverage(GridCoverage grid)
			throws IllegalArgumentException {
		if (!(grid instanceof GridCoverage2D)) {
			throw new IllegalArgumentException(
					"Cannot Recolor GridCoverage: GridCoverage2D is needed.");
		}

		// final ColorMap map = symbolizer.getColorMap();
		// if (map == null)
		// throw new IllegalArgumentException(
		// "Cannot Recolor GridCoverage: ColorMap is needed.");
		// final ColorMapEntry[] entries = map.getColorMapEntries();
		// if (entries == null || entries.length <= 0)
		// throw new IllegalArgumentException(
		// "Cannot Recolor GridCoverage: ColorMapEntries are needed.");
		// final int length = entries.length;
		// final Map colorMap = new HashMap();
		// for (int i = 0; i < length; i++)
		// colorMap.put(entries[i].getQuantity().getValue(null), entries[i]
		// .getQuantity().getValue(null));
		// return Operations.DEFAULT.recolor(grid, new Map[] { Collections
		// .singletonMap(null, colorMap) });

		final GridCoverage2D gridCoverage = (GridCoverage2D) grid;
		final int numBands = gridCoverage.getNumSampleDimensions();
		final GridSampleDimension[] targetBands = new GridSampleDimension[numBands];
		final Map[] colorMaps = new Map[numBands];
		for (int band = 0; band < numBands; band++) { // TODO get separated
			// R,G,B colorMaps from
			// symbolizer
			final Map categories = getCategories(band);
			colorMaps[band] = categories;

			/**
			 * Temporary solution, until the Recolor Operation is ported ...
			 */
			targetBands[band] = (GridSampleDimension) transformColormap(band,
					gridCoverage.getSampleDimension(band), colorMaps);
		}

		return CoverageFactoryFinder.getGridCoverageFactory(null).create(
				gridCoverage.getName(), gridCoverage.getRenderedImage(),
                (GridGeometry2D) gridCoverage.getGridGeometry(), targetBands,
				new GridCoverage[] { gridCoverage }, null);
	}

	/**
	 * HELPER FUNCTIONS
	 */
	public float getOpacity() {
		float alpha = 1.0f;
		Expression exp = this.symbolizer.getOpacity();
		if (exp == null){
			return alpha;
        }
		Number number = (Number) exp.evaluate(null,Float.class);
		if (number == null){
			return alpha;
        }
		return number.floatValue();
	}

	public Map getCategories(final int band) {
		final String[] labels = getLabels(band);
		final Color[] colors = getColors(band);

		final Map categories = new HashMap();

		/**
		 * Checking Categories
		 */
		final int labelsLength = labels.length;
		Color[] oldCmap;
		int length;
		Color[] newCmap;
		for (int i = 0; i < labelsLength; i++) {
			if (!categories.containsKey(labels[i])) {
				categories.put(labels[i], new Color[] { colors[i] });
			} else {
				oldCmap = (Color[]) categories.get(labels[i]);
				length = oldCmap.length;
				newCmap = new Color[length + 1];
				System.arraycopy(oldCmap, 0, newCmap, 0, length);
				newCmap[length] = colors[i];
				categories.put(labels[i], newCmap);
			}
		}

		return categories;
	}

	public String[] getLabels(final int band) {
		String[] labels = null;
		if (this.symbolizer.getColorMap() != null) {
			final ColorMapEntry[] colors = this.symbolizer.getColorMap()
					.getColorMapEntries();
			final int numColors = colors.length;
			labels = new String[numColors];
			for (int ci = 0; ci < numColors; ci++) {
				labels[ci] = colors[ci].getLabel();
			}
		}

		return labels;
	}

	public Color[] getColors(final int band) {
		Color[] colorTable = null;
		if (this.symbolizer.getColorMap() != null) {
			final ColorMapEntry[] colors = this.symbolizer.getColorMap()
					.getColorMapEntries();
			final int numColors = colors.length;
			colorTable = new Color[numColors];
			double opacity;
			for (int ci = 0; ci < numColors; ci++) {
                opacity = toOpacity( colors[ci].getOpacity() );
				colorTable[ci] = toColor( colors[ci].getColor(), opacity );
				if (colorTable[ci] == null){
					return null;
                }
			}
		}

		return colorTable;
	}
    public static Color toColor( Expression exp, double opacity ){
        if (exp == null){
            return null;
        }
        Color color = (Color) exp.evaluate(null, Color.class);
        int alpha = new Double(Math.ceil(255.0 * opacity)).intValue();        
        if( color != null ){                       
            return new Color( color.getRed(), color.getGreen(), color.getBlue(), alpha );            
        }
        else {
            // the value morphing code failed us .. let's try by hand
            Object obj = exp.evaluate( null );
            if (obj == null){
                return null;
            }
            Integer  intval = Integer.decode((String) obj);
            int i = intval.intValue();            
            return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF, alpha);
        }
    }
    public static double toOpacity( Expression opacity ) {
        if (opacity == null) {
            return 1.0;
        }
         
        Double value = (Double) opacity.evaluate(null, Double.class);
        if (value == null) {
            return 1.0;
        }
        return value.doubleValue();
//
//        opacity = (colors[ci].getOpacity() != null
//                ? (colors[ci].getOpacity().getValue(null) instanceof String
//                        ? Double.valueOf((String) colors[ci].getOpacity().getValue(null))
//                        : (Double) colors[ci].getOpacity().getValue(null))
//                : new Double(1.0));
    }

	/**
     * Transform the supplied RGB colors.
     */
	public SampleDimension transformColormap(final int band,
			SampleDimension dimension, final Map[] colorMaps) {
		if (colorMaps == null || colorMaps.length == 0) {
			return dimension;
		}
		boolean changed = false;
		final Map colorMap = colorMaps[Math.min(band, colorMaps.length - 1)];
		final List categoryList = ((GridSampleDimension) dimension)
				.getCategories();
		if (categoryList == null) {
			return dimension;
		}
		final Category categories[] = (Category[]) categoryList.toArray();
		Category category;
		Color[] colors;
		final int length = categories.length;
		NumberRange range;
		int lower;
		int upper;
		for (int j = length; --j >= 0;) {
			category = categories[j];
			colors = (Color[]) colorMap.get(category.getName().toString());
			if (colors == null) {
				if (!category.isQuantitative()) {
					continue;
				}
				colors = (Color[]) colorMap.get(null);
				if (colors == null) {
					continue;
				}
			}
			range = category.getRange();
			lower = ((Number) range.getMinValue()).intValue();
			upper = ((Number) range.getMaxValue()).intValue();
			if (!range.isMinIncluded())
				lower++;
			if (range.isMaxIncluded())
				upper++;
			category = category.recolor(colors);
			if (!categories[j].equals(category)) {
				categories[j] = category;
				changed = true;
			}
		}
		return changed ? new GridSampleDimension(dimension.getDescription(),
				categories, dimension.getUnits()) : dimension;
	}
}
