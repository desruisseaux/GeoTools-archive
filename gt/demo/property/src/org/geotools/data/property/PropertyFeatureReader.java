package org.geotools.data.property;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;

public class PropertyFeatureReader implements FeatureReader {
    PropertyAttributeReader reader;    
    public PropertyFeatureReader( File directory, String typeName ) throws IOException {
        File file = new File( directory, typeName+".properties");
        reader = new PropertyAttributeReader( file );                        
    }
    public FeatureType getFeatureType() {
        return reader.type;
    }
    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        reader.next();            
        FeatureType type = reader.type;
        String fid = reader.getFeatureID();
        Object values[] = new Object[ reader.getAttributeCount() ];
        for( int i=0; i< reader.getAttributeCount(); i++){
            values[i]=reader.read( i );
        }
        return type.create( values, fid );                
    }
    public boolean hasNext() throws IOException {
        return reader.hasNext();
    }
    public void close() throws IOException {
        reader.close();
        reader = null;        
    }
}
