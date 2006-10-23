/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

import org.geotools.event.AbstractGTComponent;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;


/**
 * DOCUMENT ME!
 * @source $URL$
 */
public class SelectedChannelTypeImpl extends AbstractGTComponent
    implements SelectedChannelType {
    private static FilterFactory filterFactory = FilterFactoryFinder
        .createFilterFactory();

    //private Expression contrastEnhancement;
    private ContrastEnhancement contrastEnhancement;
    private String name = "channel";

    /**
     * Creates a new instance of SelectedChannelImpl
     */
    public SelectedChannelTypeImpl() {
        contrastEnhancement = contrastEnhancement(filterFactory
                .createLiteralExpression(1.0));
    }

    public String getChannelName() {
        return name;
    }

    public ContrastEnhancement getContrastEnhancement() {
        return contrastEnhancement;
    }

    public void setChannelName(String name) {
        this.name = name;
        fireChanged();
    }

    public void setContrastEnhancement(ContrastEnhancement enhancement) {
        ContrastEnhancement old = this.contrastEnhancement;
        this.contrastEnhancement = enhancement;

        fireChildChanged("contrastEnhancement", contrastEnhancement, old);
    }

    public void setContrastEnhancement(Expression gammaValue) {
        contrastEnhancement.setGammaValue(gammaValue);
    }

    protected ContrastEnhancement contrastEnhancement(Expression expr) {
        ContrastEnhancement enhancement = new ContrastEnhancementImpl();
        enhancement.setGammaValue(filterFactory.createLiteralExpression(1.0));

        return enhancement;
    }
}
