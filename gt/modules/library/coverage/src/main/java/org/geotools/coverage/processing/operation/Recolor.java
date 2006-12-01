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

// J2SE and JAI dependencies
import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import javax.media.jai.util.Range;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

// Geotools dependencies
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.factory.Hints;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.parameter.DefaultParameterDescriptor;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.resources.image.ColorUtilities;


/**
 * Operation replacing the colors of a {@link GridCoverage}. This operation accepts
 * one argument, {@code ColorMaps}, which must be an array of {@link Map} objects.
 * Keys are category names as {@link String}. Values are colors as {@code Color[]}.
 * The {@code null} key is a special value meaning "any quantitative category".
 * 
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Recolor extends IndexColorOperation {
    /**
     * The parameter descriptor for the color map.
     */
    public static final ParameterDescriptor COLOR_MAPS = new DefaultParameterDescriptor(
            Citations.GEOTOOLS, "ColorMaps",
            Map[].class, // Value class (mandatory)
            null,        // Array of valid values
            new Map[] {  // Default value - a gray scale
                Collections.singletonMap(null, new Color[] {
                    new Color(16, 16, 16), new Color(240, 240, 240) }) }, 
            null,        // Minimal value
            null,        // Maximal value
            null,        // Unit of measure
            true);       // Parameter is mandatory

    /**
     * Construct a new "Recolor" operation.
     */
    public Recolor() {
        super(new DefaultParameterDescriptorGroup(Citations.GEOTOOLS, "Recolor",
                new ParameterDescriptor[] { SOURCE_0, COLOR_MAPS }));
    }

    /**
     * Transform the supplied RGB colors.
     */
    protected GridSampleDimension transformColormap(final int[] ARGB, final int band,
            final GridSampleDimension sampleDimension, final ParameterValueGroup parameters)
    {
        final Map[] colorMaps = (Map[]) parameters.parameter("ColorMaps").getValue();
        if (colorMaps == null || colorMaps.length == 0) {
            return sampleDimension;
        }
        boolean changed = false;
        final Map colorMap = colorMaps[Math.min(band, colorMaps.length - 1)];
        final Category categories[] = (Category[]) sampleDimension.getCategories().toArray();
        for (int j = categories.length; --j >= 0;) {
            Category category = categories[j];
            final InternationalString name = category.getName();
            Color[] colors = (Color[]) colorMap.get(name);
            if (colors == null) {
                // Localized name not found. Search using the unlocalized flavor
                // (locale == null, not to be confused with default locale).
                colors = (Color[]) colorMap.get(name.toString(null));
                if (colors == null) {
                    if (!category.isQuantitative()) {
                        continue;
                    }
                    colors = (Color[]) colorMap.get(null);
                    if (colors == null) {
                        continue;
                    }
                }
            }
            final Range range = category.getRange();
            int lower = ((Number) range.getMinValue()).intValue();
            int upper = ((Number) range.getMaxValue()).intValue();
            if (!range.isMinIncluded()) lower++;
            if ( range.isMaxIncluded()) upper++;
            ColorUtilities.expand(colors, ARGB, lower, upper);
            category = category.recolor(colors);
            if (!categories[j].equals(category)) {
                categories[j] = category;
                changed = true;
            }
        }
        return changed ? new GridSampleDimension(sampleDimension.getDescription(),
                categories, sampleDimension.getUnits()) : sampleDimension;
    }
}
