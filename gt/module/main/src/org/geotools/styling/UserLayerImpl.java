/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 * Created on November 3, 2003, 12:00 PM
 */

package org.geotools.styling;

import java.util.ArrayList;
import java.util.Arrays;

import org.geotools.data.DataStore;
import org.geotools.event.GTList;
import org.geotools.feature.FeatureType;
import org.geotools.resources.Utilities;

/**
 * DJB: on inlinefeature support:
 * The inline features also lets you "sort of" make your WMS into a WFS-T.
 *
 *I was going to implement this after SLD POST on monday, but I was
 *expecting the definition in the spec to be a bit "nicer".  Right now
 *its just:
 *
 *<element name=�InlineFeature�>
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
 * @source $URL$
 */
public class UserLayerImpl extends StyledLayerImpl implements UserLayer 
{
    /**
     *  the (memory) datastore that will contain the inline features.
     *  The initial implementation has this as a MemoryDataStore with one FeatureType in it.
     *  You should ensure that you dont keep references to it around so it can be GCed.
     */
	private DataStore   inlineFeatureDatastore = null;
	private FeatureType inlineFeatureType = null;
	
	RemoteOWS remoteOWS;
    ArrayList styles = new GTList( this, "styles" );
    FeatureTypeConstraint[] constraints = new FeatureTypeConstraint[0];
    
    public RemoteOWS getRemoteOWS()
    {
        return remoteOWS;
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
    	fireChanged();
    }
	public void setInlineFeatureType( FeatureType ft)
	{
		inlineFeatureType = ft;
		fireChanged();
	}
	
    public void setRemoteOWS(RemoteOWS service)
    {
    	RemoteOWS old = this.remoteOWS;
    	this.remoteOWS = service;
    	
    	fireChildChanged("remoteOWS",this.remoteOWS,old);
    }
    
    public FeatureTypeConstraint[] getLayerFeatureConstraints(){
    	return constraints;
    }
    
    public void setLayerFeatureConstraints(FeatureTypeConstraint[] constraints){
    	this.constraints = constraints;
		fireChanged();
    }
    
    public Style[] getUserStyles(){
       return (Style[])styles.toArray(new Style[0]);
    }
    
    public void setUserStyles(Style[] styles){ 
    	this.styles.clear();
    	this.styles.addAll(Arrays.asList(styles));
		fireChanged();
    }    

    public void addUserStyle(Style style){
        styles.add(style);
        fireChanged();
    }
    
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }

	public boolean equals(Object oth) {
        if (this == oth) {
            return true;
        }

        if (oth instanceof UserLayerImpl) {
        	UserLayerImpl other = (UserLayerImpl) oth;
        	
        	if (!(Utilities.equals(inlineFeatureDatastore, other.inlineFeatureDatastore)
        	&& Utilities.equals(inlineFeatureType, other.inlineFeatureType)
        	&& Utilities.equals(remoteOWS, other.remoteOWS)
        	&& Utilities.equals(styles, other.styles))) {
        		return false;
        	}

			final int length = constraints.length;
			if (length != other.constraints.length)
				return false;
			for (int i = 0; i < length; i++) {
				if (!Utilities.equals(constraints[i], other.constraints[i]))
					return false;
			}
			return true;
        }
        
        return false;
	}
}
