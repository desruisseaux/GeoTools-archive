/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.brewer.color;

import java.awt.Color;


/**
 * DOCUMENT ME!
 *
 * @author James
 */
public class BrewerPalette {
    public PaletteSuitability suitability;
    public SampleScheme sampler;
    private int numColors = 0;
    Color[] colors = new Color[15];

    /** Holds value of property name. */
    private String name;

    /** Holds value of property description. */
    private String description;

    /** Holds value of property type. */
    private String type;

    /**
     * Creates a new instance of BrewerPalette
     */
    public BrewerPalette() {
    }

    /**
     * Getter for property name.
     *
     * @return Value of property name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for property name.
     *
     * @param name New value of property name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for property type.
     *
     * @return Value of property type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Setter for property type.
     *
     * @param type New value of property type.
     */
    public void setType(String type) {
        this.type = type;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;

        int count = 0;

        for (int i = 0; i < colors.length; i++)
            if (colors[i] != null) {
                count++;
            }

        this.numColors = count;
    }

    public Color[] getColors(int length) {
        int[] lookup = sampler.getSampleScheme(length);
        Color[] result = new Color[length];

        for (int i = 0; i < length; i++) {
            result[i] = colors[lookup[i]];
        }

        return result;
    }

    public Color getColor(int index, int length) {
        return getColors(length)[index];
    }

    /**
     * Getter for the colour count
     *
     * @return the most colours this palette currently supports
     */
    public int getMaxColors() {
        int countSampler = sampler.getMaxCount();

        //return the lesser of countSampler and numColors
        if (countSampler < numColors) {
            return countSampler;
        } else {
            return numColors;
        }
    }

    /**
     * Getter for the colour count
     *
     * @return the minimum number of colours this palette currently supports
     */
    public int getMinColors() {
        return sampler.getMinCount();
    }
}
