
package org.geotools.data.ows;

import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

/**
 * <p> 
 * Represents a wfs:FeatureType ... and didn't want to use FeatureType as it 
 * could get confused with org.geotools.data.FeatureType
 * </p>
 * @author dzwiers
 *
 */
public class FeatureSetDescription {
    public static final int NO_OPERATION = 0;
    public static final int QUERY_OPERATION = 1;
    public static final int INSERT_OPERATION = 2;
    public static final int UPDATE_OPERATION = 4;
    public static final int DELETE_OPERATION = 8;
    public static final int LOCK_OPERATION = 16;
    
    private String name;
    private String title;
    private String _abstract;
    private String SRS;
    private List keywords;
    private Envelope[] latLongBoundingBox;
    private int operations;
//    private MetadataURL[] metadataURL;
    /**
     * @return Returns the abstracT.
     */
    public String getAbstract() {
        return _abstract;
    }
    /**
     * @param abstracT The abstracT to set.
     */
    public void setAbstract(String _abstract) {
        this._abstract = _abstract;
    }
    /**
     * @return Returns the keywords.
     */
    public List getKeywords() {
        return keywords;
    }
    /**
     * @param keywords The keywords to set.
     */
    public void setKeywords(List keywords) {
        this.keywords = keywords;
    }
    /**
     * @return Returns the latLongBoundingBox.
     */
    public Envelope[] getLatLongBoundingBox() {
        return latLongBoundingBox;
    }
    /**
     * @param latLongBoundingBox The latLongBoundingBox to set.
     */
    public void setLatLongBoundingBox(Envelope[] latLongBoundingBox) {
        this.latLongBoundingBox = latLongBoundingBox;
    }
    /**
     * @return Returns the metadataURL.
     */
//    public MetadataURL[] getMetadataURL() {
//        return metadataURL;
//    }
    /**
     * @param metadataURL The metadataURL to set.
     */
//    public void setMetadataURL(MetadataURL[] metadataURL) {
//        this.metadataURL = metadataURL;
//    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return Returns the operations.
     */
    public int getOperations() {
        return operations;
    }
    /**
     * @param operations The operations to set.
     */
    public void setOperations(int operations) {
        this.operations = operations;
    }
    /**
     * @return Returns the sRS.
     */
    public String getSRS() {
        return SRS;
    }
    /**
     * @param srs The sRS to set.
     */
    public void setSRS(String srs) {
        SRS = srs;
    }
    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
