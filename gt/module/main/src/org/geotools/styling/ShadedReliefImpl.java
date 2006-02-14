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
 * ShadedReliefImpl.java
 *
 * Created on 13 November 2002, 13:59
 */
package org.geotools.styling;

import org.geotools.event.AbstractGTComponent;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.expression.Expression;


/**
 * DOCUMENT ME!
 *
 * @author iant
 * @source $URL$
 */
public class ShadedReliefImpl extends AbstractGTComponent
    implements ShadedRelief {
    private static FilterFactory filterFactory = FilterFactoryFinder
        .createFilterFactory();
    private Expression reliefFactor;
    private boolean brightness = false;

    /**
     * Creates a new instance of ShadedReliefImpl
     */
    public ShadedReliefImpl() {
        reliefFactor = filterFactory.createLiteralExpression(55);
    }

    /**
     * The ReliefFactor gives the amount of exaggeration to use for the height
     * of the “hills.”  A value of around 55 (times) gives reasonable results
     * for Earth-based DEMs. The default value is system-dependent.
     *
     * @return an expression which evaluates to a double.
     */
    public Expression getReliefFactor() {
        return reliefFactor;
    }

    /**
     * indicates if brightnessOnly is true or false. Default is false.
     *
     * @return boolean brightnessOn.
     */
    public boolean isBrightnessOnly() {
        return brightness;
    }

    /**
     * turns brightnessOnly on or off depending on value of flag.
     *
     * @param flag boolean
     */
    public void setBrightnessOnly(boolean flag) {
        brightness = flag;
    }

    /**
     * The ReliefFactor gives the amount of exaggeration to use for the height
     * of the “hills.”  A value of around 55 (times) gives reasonable results
     * for Earth-based DEMs. The default value is system-dependent.
     *
     * @param reliefFactor an expression which evaluates to a double.
     */
    public void setReliefFactor(Expression reliefFactor) {
        Expression old = this.reliefFactor;
        this.reliefFactor = reliefFactor;
        fireChildChanged("reliefFactor", reliefFactor, old);
    }
}
