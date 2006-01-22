package org.geotools.data.shapefile.shp.xml;


/**
 * Bean capturing shapefile metadata information.
 * <p>
 * To create please use the included ShpXmlFileReader, this is
 * only a data object.
 * </p><p>
 * Note: This bean currently extends MetadataEntity to allow for uses
 * with Discovery.search( QueryRequest ).  When QueryRequest can actually
 * handle normal java beans we can remove this restrictions.
 * </p>
 * @author jgarnett
 * @since 0.3
 */
public class Metadata {
    
    /** identification information */
    IdInfo idinfo;

    /**
     * @return Returns the idinfo.
     */
    public IdInfo getIdinfo() {
        return idinfo;
    }
    /**
     * @param idinfo The idinfo to set.
     */
    public void setIdinfo( IdInfo idinfo ) {
        this.idinfo = idinfo;
    }
}