/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2005, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.styling;

import org.geotools.event.GTComponent;

/**
 * The ColorMap element defines either the colors of a palette-type raster
 * source or the mapping of  fixed-numeric pixel values to colors.
 * <pre>
 * &lt;xs:element name="ColorMap"&gt;
 *   &lt;xs:complexType&gt;
 *     &lt;xs:choice minOccurs="0" maxOccurs="unbounded"&gt;
 *       &lt;xs:element ref="sld:ColorMapEntry"/&gt;
 *     &lt;/xs:choice&gt;
 *   &lt;/xs:complexType&gt;
 * &lt;/xs:element&gt;
 * </pre>
 * For example, a DEM raster giving elevations in meters above sea level can be
 * translated to a colored  image with a ColorMap.  The quantity attributes of
 * a color-map are used for translating between numeric  matrixes and color
 * rasters and the ColorMap entries should be in order of increasing numeric
 * quantity so  that intermediate numeric values can be matched to a color (or
 * be interpolated between two colors).   Labels may be used for legends or
 * may be used in the future to match character values.   Not all systems can
 * support opacity in colormaps.  The default opacity is 1.0 (fully opaque).
 * Defaults for quantity and label are system-dependent.
 */
public interface ColorMap extends GTComponent {
    public static final int TYPE_RAMP = 1;
    public static final int TYPE_INTERVALS = 2;
    public static final int TYPE_VALUES = 3;

    public void addColorMapEntry(ColorMapEntry entry);

    public ColorMapEntry[] getColorMapEntries();

    public ColorMapEntry getColorMapEntry(int i);

    public int getType();

    public void setType(int type);
}
