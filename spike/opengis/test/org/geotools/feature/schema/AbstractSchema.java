package org.geotools.feature.schema;

import org.opengis.feature.schema.Descriptor;

public class AbstractSchema implements Descriptor {
	protected int minOccurs;
	protected int maxOccurs;
	public AbstractSchema(){
		this( 1, 1 );
	}
	public AbstractSchema( int max ){
		this( 0, max );
	}
	public AbstractSchema( int min, int max ){
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
