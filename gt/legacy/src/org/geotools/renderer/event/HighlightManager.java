/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004, Geotools Project Managment Committee (PMC)
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
package org.geotools.renderer.event;

// Geotools dependencies
import org.geotools.feature.Feature;


/**
 * Describes operations that are necessary for highlighting and selection. Every
 * {@link Feature} can either have or not have a {@link FeatureModifier}. After
 * initialisation no <code>Feature</code> has a <code>FeatureModifier</code> at all.
 * 
 * <code>FeatureModifier</code> objects can be added with the
 * <code>addFeatureModifier(...)</code> method and removed with
 * the <code>removeFeatureModifier(...)</code> method. 
 * 
 * In addition, {@link MouseFeatureListener} objects can be
 * registered with the <code>HighlightManager</code>
 *
 * @version $Id: HighlightManager.java 5670 2004-05-16 17:35:30Z desruisseaux $
 * @author Julian Elliott
 */
public interface HighlightManager {
    /**
     * Adds a <code>FeatureModifier</code> to a particular feature,
     * or replaces the existing one if there already is one
     *
     * @param feature the <code>Feature</code> to which to add the <code>FeatureModifier</code>.
     * @param modifier the <code>FeatureModifier</code> to add
     */
    public void addFeatureModifier(Feature feature, FeatureModifier modifier);

    /**
     * Removes the <code>FeatureModifier</code> from a particular feature.
     *
     * @param feature the <code>Feature</code> from which to remove the <code>FeatureModifier</code>.
     */
    public void removeFeatureModifier(Feature feature);

    /**
     * Registers a new <code>MouseFeatureListener</code>.
     *
     * @param listener the <code>MouseFeatureListener</code> to be registered.
     */
    public void addMouseFeatureListener(MouseFeatureListener listener);
}
