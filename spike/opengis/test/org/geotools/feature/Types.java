package org.geotools.feature;

import java.util.HashSet;
import java.util.Set;

import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;

/**
 * This is a set of utility methods for dealing with types.
 * <p>
 * This set of classes captures the all important how does it work questions, particularly with respect to
 * super types.
 * </p>
 * @author Jody Garnett
 */
public class Types {
	
	/** Wander up getSuper gathering all memberTypes */
	public static Set<FeatureType> memberTypes( FeatureCollectionType collectionType ){
		Set<FeatureType> memberTypes = new HashSet<FeatureType>();
		while( collectionType != null ){
			memberTypes.addAll( collectionType.getMemberDescriptor() );
			ComplexType superType = collectionType.getSuper();
			if( superType instanceof FeatureCollectionType ){
				collectionType = (FeatureCollectionType) superType;
			}
			else {
				collectionType = null;
			}
		}
		return memberTypes;
	}
	/**
	 * This method is about as bad as it gets, we need to wander through Descriptor
	 * detecting overrides by AttributeType. Almost makes me thing Descriptor should have the
	 * attrribute name, and the GenericName stuff should be left on AttributeType.
	 * 
	 * @param complex
	 * @return Descriptor that actually describes what is valid for the ComplexType.
	 */
	public static Descriptor schema( ComplexType complex ){
		// We need to do this with tail recursion:
		// - and sequence, any, choice gets hacked differently ...
		return complex.getDescriptor();
	}
}
