/*
 * UserLayer.java
 *
 * Created on November 3, 2003, 12:00 PM
 */

package org.geotools.styling;

import java.util.ArrayList;

import org.geotools.data.DataStore;
import org.geotools.feature.FeatureType;

/**
 * DJB: on inlinefeature support:
 * The inline features also lets you "sort of" make your WMS into a WFS-T.
 *
 *I was going to implement this after SLD POST on monday, but I was
 *expecting the definition in the spec to be a bit "nicer".  Right now
 *its just:
 *
 *<element name=”InlineFeature”>
 *  <complexType>
 *    <sequence>
 *      <element ref="gml:_Feature"
 *               maxOccurs="unbounded"/>
  *   </sequence>
  * </complexType>
 *
 *
 * (the spec hasnt been finalized)
 * 
 * I guess if we make some assumptions about the data coming in - ie. every
 * feature is the same type, and its simple (no nesting, no <choices>, and
 * no attributes), then we can parse ones that look like:
 * 
 * <Feature>
 *  <Name>David Blasby</Name>
 *  <Location> ... GML ... </Location>
 * </Feature>
 * 
 *
 *I'm not the best at reading .xsd, but I think that means you can stick
 *in ANY GML Feature.  If so, its way too general.
 *
 *My plan was to parse the first Feature (or, the given schema if there is
 *one) to find out all the property names (and which one(s) are the
 *geometry) and make a FeatureType.  (I'd assume all the properties were
 *strings)
 *
 *Then, make a MemoryDataStore and put the features in it.  I can pass
 *this off to the lite renderer as normal.
 *
 * @author  jamesm
 */
public class UserLayer extends StyledLayer 
{
    /**
     *  the (memory) datastore that will contain the inline features.
     *  The initial implementation has this as a MemoryDataStore with one FeatureType in it.
     *  You should ensure that you dont keep references to it around so it can be GCed.
     */
	private DataStore   inlineFeatureDatastore = null;
	private FeatureType inlineFeatureType = null;
	
    ArrayList styles = new ArrayList();
    
    public RemoteOWS getRemoteOWS()
    {
        return null;
    }
    
    public DataStore getInlineFeatureDatastore()
    {
    	return inlineFeatureDatastore;    
    }
	public FeatureType getInlineFeatureType()
	{
		return inlineFeatureType;
	}
	
	public void setInlineFeatureDatastore(DataStore store)
    {
    	inlineFeatureDatastore = store;    
    }
	public void setInlineFeatureType( FeatureType ft)
	{
		inlineFeatureType = ft;
	}
	
    
    public void setRemoteOWS(RemoteOWS service)
    {
    }
    
    public FeatureTypeConstraint[] getLayerFeatureConstraints(){
        return null;
    }
    public void setLayerFeatureConstraints(FeatureTypeConstraint[] constraints){
    }
    
    public Style[] getUserStyles(){
       return (Style[])styles.toArray(new Style[0]);
    }
    public void setUserStyles(Style[] styles){
        
    }
    
    public void addUserStyle(Style style){
        styles.add(style);
    }
    
    
    
}
