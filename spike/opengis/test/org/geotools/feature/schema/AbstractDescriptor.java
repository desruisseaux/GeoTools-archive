package org.geotools.feature.schema;

import org.opengis.feature.schema.Descriptor;

public class AbstractDescriptor implements Descriptor {
	protected int minOccurs;
	protected int maxOccurs;
	public AbstractDescriptor(){
		this( 1, 1 );
	}
	public AbstractDescriptor( int max ){
		this( 0, max );
	}
	public AbstractDescriptor( int min, int max ){
		minOccurs = min;
		maxOccurs = max;
	}
	public int getMinOccurs() {
		return minOccurs;
	}
	public int getMaxOccurs() {
		return maxOccurs;
	}
}
