/*
 * Created on Sep 1, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.data;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;

import org.opengis.catalog.CatalogEntry;
import org.opengis.catalog.MetadataEntity;


final class DataStoreCatalogEntry implements CatalogEntry {
    private final AbstractDataStore store;
    private final URI namespace;
    private final String typeName;

    public DataStoreCatalogEntry(AbstractDataStore store, URI namespace, String typeName) {
        this.store = store;
        this.namespace = namespace;
        this.typeName = typeName;
    }

    public Object getResource() {
        try {
            return this.store.getFeatureSource( typeName );
        } catch (IOException e) {
            return null;
        }
    }

    public String getDataName() {
        return typeName;
    }

    public int getNumMetadata() {
        return 1;
    }

    public String[] getMetadataNames() {
        return new String[]{ "default", };
    }

    public MetadataEntity getMetadata(int index) {
        if( index == 1){
            return this.store.metadata( typeName );
        }
        return null;
    }

    public MetadataEntity getMetadata(String metadataName ) {
        if( "default".equals( metadataName )){
            return this.store.metadata( typeName );
        }
        return null;
    }

    public Iterator iterator() {
        return Collections.singleton( this.store.metadata( typeName) ).iterator();                
    }

    public boolean equals(Object obj){
	if( !(obj instanceof DataStoreCatalogEntry) ){
		return false;
	}
	DataStoreCatalogEntry entry=(DataStoreCatalogEntry) obj;
	return typeName.equals(entry.typeName) && store==entry.store;
    }
}
