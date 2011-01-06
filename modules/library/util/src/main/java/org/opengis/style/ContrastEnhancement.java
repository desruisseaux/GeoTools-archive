/*$************************************************************************************************
 **
 ** $Id: ContrastEnhancement.java 1391 2009-05-09 13:24:19Z Jive $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/style/ContrastEnhancement.java $
 **
 ** Copyright (C) 2008 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.style;

import org.opengis.annotation.Extension;
import org.opengis.annotation.XmlElement;
import org.opengis.filter.expression.Expression;


/**
 * <p>The ContrastEnhancement object defines contrast enhancement for a channel of
 * a false-color image or for a color image.
 * </p>
 *
 * <p>In the case of a color image, the relative grayscale brightness of a pixel
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
 * </p>
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/symbol">Symbology Encoding Implementation Specification 1.1.0</A>
 * @author Open Geospatial Consortium
 * @author Ian Turton, CCG
 * @author Johann Sorel (Geomatys)
 * @since GeoAPI 2.2
 */
@XmlElement("ContrastEnhancement")
public interface ContrastEnhancement {

    /**
     * We use a codeList to enable more enchancement type possibilities.
     */
    @XmlElement("Normalize,Histogram")
    public ContrastMethod getMethod();
    
    /**
     * A "GammaValue" tells how much to brighten (values
     * greater than 1.0) or dim (values less than 1.0) an image. The default GammaValue is 1.0
     * (no change).
     *
     * @return Expression to control gamma adjustment, null or Expression.NIL handled as the value 1.0
     */
    @XmlElement("GammaValue")
    Expression getGammaValue();

    /**
     * calls the visit method of a StyleVisitor
     *
     * @param visitor the style visitor
     */
    @Extension
    Object accept(StyleVisitor visitor, Object extraData);
    
}
