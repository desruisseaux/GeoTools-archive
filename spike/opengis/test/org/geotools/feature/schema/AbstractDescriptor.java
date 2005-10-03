package org.geotools.feature.schema;

import org.opengis.feature.schema.Descriptor;

public class AbstractDescriptor implements Descriptor {
	protected int minOccurs;
	protected int maxOccurs;
	
	/**
	 * Creates a descriptor with default cardinality
	 * of 1..1
	 *
	 */
	public AbstractDescriptor(){
		this( 1, 1 );
	}
	
	/**
	 * Creates a descriptor with cardinality 1..<code>max</code>
	 * 
	 * @param max
	 */
	public AbstractDescriptor( int max ){
		this( 1, max );
	}
	
	public AbstractDescriptor( int min, int max ){
		if( (min < 0) || (max < 0) || (max < min) )
			throw new IllegalArgumentException(
			"min and max must be positive integers and max must be greater than or equal to min");
		
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
