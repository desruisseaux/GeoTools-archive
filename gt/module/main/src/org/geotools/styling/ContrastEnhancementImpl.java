/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2002, Centre for Computational Geography
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
 * Created on 13 November 2002, 13:52
 */
package org.geotools.styling;

import org.geotools.event.AbstractGTComponent;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;


/**
 * The ContrastEnhancement object defines contrast enhancement for a channel of
 * a false-color image or for a color image. Its format is:
 * <pre>
 * &lt;xs:element name="ContrastEnhancement"&gt;
 *   &lt;xs:complexType&gt;
 *     &lt;xs:sequence&gt;
 *       &lt;xs:choice minOccurs="0"&gt;
 *         &lt;xs:element ref="sld:Normalize"/&gt;
 *         &lt;xs:element ref="sld:Histogram"/&gt;
 *       &lt;/xs:choice&gt;
 *       &lt;xs:element ref="sld:GammaValue" minOccurs="0"/&gt;
 *     &lt;/xs:sequence&gt;
 *   &lt;/xs:complexType&gt;
 * &lt;/xs:element&gt;
 * &lt;xs:element name="Normalize"&gt;
 *   &lt;xs:complexType/&gt;
 * &lt;/xs:element&gt;
 * &lt;xs:element name="Histogram"&gt;
 *   &lt;xs:complexType/&gt;
 * &lt;/xs:element&gt;
 * &lt;xs:element name="GammaValue" type="xs:double"/&gt;
 * </pre>
 * In the case of a color image, the relative grayscale brightness of a pixel
 * color is used. ?Normalize? means to stretch the contrast so that the
 * dimmest color is stretched to black and the brightest color is stretched to
 * white, with all colors in between stretched out linearly. ?Histogram? means
 * to stretch the contrast based on a histogram of how many colors are at each
 * brightness level on input, with the goal of producing equal number of
 * pixels in the image at each brightness level on output.  This has the
 * effect of revealing many subtle ground features. A ?GammaValue? tells how
 * much to brighten (value greater than 1.0) or dim (value less than 1.0) an
 * image. The default GammaValue is 1.0 (no change). If none of Normalize,
 * Histogram, or GammaValue are selected in a ContrastEnhancement, then no
 * enhancement is performed.
 *
 * @author iant
 * @source $URL$
 */
public class ContrastEnhancementImpl extends AbstractGTComponent
    implements ContrastEnhancement {
    private FilterFactory filterFactory;
    private Expression gamma;
    private Expression type;

    public ContrastEnhancementImpl() {
        this(FilterFactoryFinder.createFilterFactory());
    }

    public ContrastEnhancementImpl(FilterFactory factory) {
        filterFactory = factory;
    }

    public void setFilterFactory(FilterFactory factory) {
        filterFactory = factory;
    }

    public Expression getGammaValue() {
        return gamma;
    }

    public Expression getType() {
        return type;
    }

    public void setGammaValue(Expression gamma) {
        this.gamma = gamma;
        fireChanged();
    }

    public void setHistogram() {
        type = filterFactory.createLiteralExpression("HISTOGRAM");
        fireChanged();
    }

    public void setNormalize() {
        type = filterFactory.createLiteralExpression("NORMALIZE");
        fireChanged();
    }

    public void setType(Expression type) {
        this.type = type;
        fireChanged();
    }
}
