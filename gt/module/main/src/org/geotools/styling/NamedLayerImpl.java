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
 * NamedLayer.java
 *
 * Created on November 3, 2003, 10:10 AM
 */
package org.geotools.styling;

import java.util.ArrayList;
import java.util.List;


/**
 * DOCUMENT ME!
 *
 * @author jamesm
 * @source $URL$
 */
public class NamedLayerImpl extends StyledLayerImpl implements NamedLayer {
    List styles = new ArrayList();

    FeatureTypeConstraint[] featureTypeConstraints = new FeatureTypeConstraint[0];
    
    public FeatureTypeConstraint[] getLayerFeatureConstraints() {
        return featureTypeConstraints;
    }

    public void setLayerFeatureConstraints(FeatureTypeConstraint[] featureTypeConstraints) {
    	this.featureTypeConstraints = featureTypeConstraints;
    	fireChanged();
    }
    
    public Style[] getStyles() {
        return (Style[]) styles.toArray(new Style[0]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param sl may be a StyleImpl or a NamedStyle
     */
    public void addStyle(Style sl) {
        styles.add(sl);
        fireChanged();
    }

    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
}
