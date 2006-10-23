/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.coverage.processing.operation;

 
import java.awt.Color;
import java.util.Collections;
import java.util.Map;

import javax.media.jai.util.Range;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.resources.image.ColorUtilities;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Operation replacing the colors of a {@link GridCoverage}. This operation
 * accepts one argument, <code>ColorMaps</code>, which must be an array of
 * {@link Map} objects. Keys are category names as {@link String}. Values are
 * colors as <code>Color[]</code>. The <code>null</code> key is a special
 * value meaning "any quantitative category".
 * 
 * @source $URL: http://svn.geotools.org/geotools/branches/coverages_branch/trunk/gt/module/coverage/src/org/geotools/coverage/processing/operation/Recolor.java $
 * @version $Id: Recolor.java 18352 2006-03-01 06:13:42Z desruisseaux $
 * @author Martin Desruisseaux
 */
public final class Recolor extends IndexColorOperation {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The parameter descriptor for the interpolation type.
	 */
	public static final DefaultParameterDescriptor COLOR_MAPS = new DefaultParameterDescriptor(
			Citations.OGC, "ColorMaps", Map[].class, // Value class
			// (mandatory)
			null, // Array of valid values
			new Map[] { Collections.singletonMap(null, new Color[] {
					new Color(16, 16, 16), new Color(240, 240, 240) }) }, // Default
			// value
			null, // Minimal value
			null, // Maximal value
			null, // Unit of measure
			true); // Parameter is mandatory

	/**
	 * Construct a new "Recolor" operation.
	 */
	public Recolor() {
		super(new DefaultParameterDescriptorGroup(Citations.OGC, "Recolor",
				new ParameterDescriptor[] { SOURCE_0, COLOR_MAPS }));

	}

	/**
	 * Transform the supplied RGB colors.
	 */
	protected GridSampleDimension transformColormap(final int[] ARGB,
			final int band, final GridSampleDimension sampleDimension,
			final ParameterValueGroup parameters) {
		final Map[] colorMaps = (Map[]) parameters.parameter("ColorMaps")
				.getValue();
		if (colorMaps == null || colorMaps.length == 0) {
			return sampleDimension;
		}
		boolean changed = false;
		final Map colorMap = colorMaps[Math.min(band, colorMaps.length - 1)];
		final Category categories[] = (Category[]) sampleDimension
				.getCategories().toArray();
		final int length = categories.length;
		Category category;
		Color[] colors;
		for (int j = length; --j >= 0;) {
			category = categories[j];
			colors = (Color[]) colorMap.get(category.getName());
			if (colors == null) {
				if (!category.isQuantitative()) {
					continue;
				}
				colors = (Color[]) colorMap.get(null);
				if (colors == null) {
					continue;
				}
			}
			final Range range = category.getRange();
			int lower = ((Number) range.getMinValue()).intValue();
			int upper = ((Number) range.getMaxValue()).intValue();
			if (!range.isMinIncluded())
				lower++;
			if (range.isMaxIncluded())
				upper++;
			ColorUtilities.expand(colors, ARGB, lower, upper);
			category = category.recolor(colors);
			if (!categories[j].equals(category)) {
				categories[j] = category;
				changed = true;
			}
		}
		return changed ? new GridSampleDimension(sampleDimension
				.getDescription(), categories, sampleDimension.getUnits())
				: sampleDimension;
	}

	public Coverage doOperation(ParameterValueGroup parameters, Hints hints) {
		return super.doOperation(parameters, hints);
	}

}
