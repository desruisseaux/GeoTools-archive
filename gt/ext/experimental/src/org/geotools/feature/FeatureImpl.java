
package org.geotools.feature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * This is a sample implementation of the FeatureType API.
 * <p>
 * The suffix implementation is used to indicat this is a direct, straight forward
 * implementation of the associated interface.
 * </p>
 * <p>
 * This serves as, my reference implementation, if you have any questions please
 * contact me (jgarnett@refractions.net) or jump on the geotools-devel email list.
 * There is a good chance my understanding is wrong, I expect and want feedback on
 * this implementation.
 * </p>
 * <p>
 * I especially am interested in:
 * <ul>
 * <li>XPATH - xpath is used to locate AttributeTyp instances. Does this even work?
 *     FeatureType captures schema information, you cannot do an xpath query based on
 *     @attribute for example?
 * <li>Split between information captured by a FeatureType and the complete description
 * of a Schema indicated by a FeatureType and its parents.
 * <li>Single or Multiple inhieratance. This implementation is based on the idea of single
 * inhieratance although the "OpenGIS" Reference model allows seems to indicate multiple is
 * preferable.
 * </ul>
 * </p>
 * <p>
 * <b>Note:</b> No javadoc methods are provided on methods, implementation should be considered
 * un interesting. It is the API we are interested in.
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public class FeatureImpl implements Feature {

    /*
     * @see org.geotools.feature.Feature#getParent()
     */
    public FeatureCollection getParent() {
        return null;
    }

    /*
     * @see org.geotools.feature.Feature#setParent(org.geotools.feature.FeatureCollection)
     */
    public void setParent( FeatureCollection collection ) {
    }

    /*
     * @see org.geotools.feature.Feature#getFeatureType()
     */
    public FeatureType getFeatureType() {
        return null;
    }

    /*
     * @see org.geotools.feature.Feature#getID()
     */
    public String getID() {
        return null;
    }

    /*
     * @see org.geotools.feature.Feature#getAttributes(java.lang.Object[])
     */
    public Object[] getAttributes( Object[] attributes ) {
        return null;
    }

    /*
     * @see org.geotools.feature.Feature#getAttribute(java.lang.String)
     */
    public Object getAttribute( String xPath ) {
        return null;
    }

    /*
     * @see org.geotools.feature.Feature#getAttribute(int)
     */
    public Object getAttribute( int index ) {
        return null;
    }

    /*
     * @see org.geotools.feature.Feature#setAttribute(int, java.lang.Object)
     */
    public void setAttribute( int position, Object val ) throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
    }

    /*
     * @see org.geotools.feature.Feature#getNumberOfAttributes()
     */
    public int getNumberOfAttributes() {
        return 0;
    }

    /*
     * @see org.geotools.feature.Feature#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute( String xPath, Object attribute ) throws IllegalAttributeException {
    }

    /*
     * @see org.geotools.feature.Feature#getDefaultGeometry()
     */
    public Geometry getDefaultGeometry() {
        return null;
    }

    /*
     * @see org.geotools.feature.Feature#setDefaultGeometry(com.vividsolutions.jts.geom.Geometry)
     */
    public void setDefaultGeometry( Geometry geometry ) throws IllegalAttributeException {
    }

    /*
     * @see org.geotools.feature.Feature#getBounds()
     */
    public Envelope getBounds() {
        return null;
    }

}
