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
/*
 * LegendStyleElementNodeInfo.java
 *
 * Created on 07 July 2003, 20:52
 */
package org.geotools.gui.swing.legend;

import java.awt.Color;
import java.util.logging.Logger;

import javax.swing.Icon;

import org.geotools.feature.Feature;
import org.geotools.styling.Rule;


/**
 * This class only store name of the style element node and the icon for the
 * style, it maybe will be extended to create icon itself. for creating Icon,
 * a feature sample is required to get value out of the rule.
 *
 * @author jianhuij
 */
public class LegendRuleNodeInfo extends LegendNodeInfo {
    /** The logger for the Legend module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.swing.legend");
    private Icon icon = null;
    private Rule rule = null;
    private Feature sample = null;

    /**
     * Creates a new instance of LegendStyleElementNodeInfo
     *
     * @param name title for this rule
     * @param icon icon for this rule generated by LegendIconMaker from this
     *        rule
     * @param rule rule for the cell
     * @param sample feature sample from this data using this rule
     */
    public LegendRuleNodeInfo(String name, Icon icon, Rule rule, Feature sample) {
        setName(name);
        setIcon(icon);
        setRule(rule);
        setFeatureSample(sample);
    }

    public void setFeatureSample(Feature sample) {
        this.sample = sample;
    }

    public Feature getFeatureSample() {
        return this.sample;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public Rule getRule() {
        return this.rule;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public String toString() {
        return getName();
    }

    public Icon getIcon(boolean selected) {
        return getIcon();
    }

    public Color getBackground(boolean selected) {
        return null;
    }

    public boolean isSelected() {
        return true;
    }
}
