package org.geotools.data.property;

import java.io.*;

import org.geotools.data.*;
import org.geotools.feature.*;

import com.vividsolutions.jts.geom.Geometry;
/**
 * Simple AttributeWriter that produces Java properties files.
 * <p>
 * This AttributeWriter is part of the geotools2 DataStore tutorial, and
 * should be considered a Toy.
 * </p>
 * <p>
 * The content produced witll start with the property "_" with the
 * value being the typeSpec describing the featureType. Thereafter each line
 * will represent a Features with FeatureID as the property and the attribtues
 * as the value separated by | characters.
 * </p> 
 * <pre><code>
 * _=id:Integer|name:String|geom:Geometry
 * fid1=1|Jody|<i>well known text</i>
 * fid2=2|Brent|<i>well known text</i>
 * fid3=3|Dave|<i>well known text</i>
 * </code></pre>
 * @author jgarnett
 */
public class PropertyAttributeWriter implements AttributeWriter {
    BufferedWriter writer;    
    FeatureType type;    
    public PropertyAttributeWriter( File file, FeatureType featureType ) throws IOException {
        writer = new BufferedWriter( new FileWriter( file ) );
        type = featureType;                
        writer.write( "_=" );
        writer.write( DataUtilities.spec( type ) );                                        
    }
    public int getAttributeCount() {
        return type.getAttributeCount();
    }
    public AttributeType getAttributeType(int index) throws ArrayIndexOutOfBoundsException {
        return type.getAttributeType(index);
    }
    public void echoLine( String line ) throws IOException{
        if( writer == null ){
            throw new IOException("Writer has been closed");
        }
        if( line == null ){
            return;
        }
        writer.write( line );
    }
    public void writeFeatureID( String fid ) throws IOException{
        if( writer == null){
            throw new IOException("Writer has been closed");
        }        
        writer.write( fid );                
    }
    public void write(int position, Object attribute) throws IOException {
        if( writer == null){
            throw new IOException("Writer has been closed");
        }
        String text;
        if( position == 0 ){
            writer.write("=");
        }
        else {
            writer.write("|");
        }
        if( attribute instanceof Geometry){
            writer.write( ((Geometry)attribute).toText() );
        }
        else {
            writer.write( attribute.toString() );
        }                                
    }
    public void close() throws IOException {
        if( writer == null){
            throw new IOException("Writer has already been closed");            
        }
        writer.close();
        writer = null;
        type = null;        
    }
    public void next() throws IOException {
        if( writer == null){
            throw new IOException("Writer has been closed");
        }
        writer.newLine();
        writer.flush();
    }
    public boolean hasNext() throws IOException {
        return false;
    }            
}
