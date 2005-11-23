package org.geotools.catalog.defaults;

import java.net.URI;

import org.geotools.catalog.CatalogInfo;


public class DefaultCatalogInfo implements CatalogInfo {
    protected String title, description;
    protected URI source;
    protected String[] keywords;

    public DefaultCatalogInfo() {
        // for sub-classes
    }

    public DefaultCatalogInfo( String title, String description, URI source, String[] keywords ) {
        this.title = title;
        this.description = description;
        this.source = source;
        this.keywords = keywords;
    }

    /* (non-Javadoc)
	 * @see org.geotools.catalog.CatalogInfo#getTitle()
	 */
    public String getTitle() {
        return title;
    }

    /* (non-Javadoc)
	 * @see org.geotools.catalog.CatalogInfo#getKeywords()
	 */
    public String[] getKeywords() { // aka Subject
        return keywords;
    }

    /* (non-Javadoc)
	 * @see org.geotools.catalog.CatalogInfo#getDescription()
	 */
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
	 * @see org.geotools.catalog.CatalogInfo#getSource()
	 */
    public URI getSource() { // aka server
        return source;
    }
    
    /**
     * @param desc The desc to set.
     */
    public void setDesc( String desc ) {
        this.description = desc;
    }
    /**
     * @param keywords The keywords to set.
     */
    public void setKeywords( String[] keywords ) {
        this.keywords = keywords;
    }
    /**
     * @param source The source to set.
     */
    public void setSource( URI source ) {
        this.source = source;
    }
    /**
     * @param title The title to set.
     */
    public void setTitle( String title ) {
        this.title = title;
    }
}
