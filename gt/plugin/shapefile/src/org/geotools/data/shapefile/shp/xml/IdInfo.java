package org.geotools.data.shapefile.shp.xml;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Bean of idinfo element of shp.xml.
 */
public class IdInfo {
    
    /** spdom/bounding represents */
    Envelope bounding;
    
    /** spdom/lbounding represents */
    Envelope lbounding;
    
    /**
     * @return Returns the bounding.
     */
    public Envelope getBounding() {
        return bounding;
    }
    /**
     * @param bounding The bounding to set.
     */
    public void setBounding( Envelope bounding ) {
        this.bounding = bounding;
    }
    
    /**
     * @return Returns the lbounding.
     */
    public Envelope getLbounding() {
        return lbounding;
    }
    /**
     * @param lbounding The lbounding to set.
     */
    public void setLbounding( Envelope lbounding ) {
        this.lbounding = lbounding;
    }
}
