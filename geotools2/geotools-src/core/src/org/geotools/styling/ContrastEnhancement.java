/*
 * ContrastEnhancement.java
 *
 * Created on 08 November 2002, 14:42
 */

package org.geotools.styling;

import org.geotools.filter.Expression;

/** The ContrastEnhancement object defines contrast enhancement for a channel of a false-color image or
 * for a color image.
 * Its format is:
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
 * In the case of a color image, the relative grayscale brightness of a pixel color is used.
 * “Normalize” means to stretch the contrast so that the dimmest color is stretched to black and
 * the brightest color is stretched to white, with all colors in between stretched out linearly.
 * “Histogram” means to stretch the contrast based on a histogram of how many colors are at
 * each brightness level on input, with the goal of producing equal number of pixels in the image
 * at each brightness level on output.  This has the effect of revealing many subtle ground features.
 * A “GammaValue” tells how much to brighten (value greater than 1.0) or dim (value less than 1.0) an image.
 * The default GammaValue is 1.0 (no change). If none of Normalize, Histogram, or GammaValue are selected
 * in a ContrastEnhancement, then no enhancement is performed.
 *
 *
 * @author  iant
 */
public interface ContrastEnhancement {
    
    public void setType(Expression type);
    public Expression getType();
    public void setGammaValue(Expression gamma);
    public Expression getGammaValue();

    
    public void setNormalize();
    public void setHistogram();
}
