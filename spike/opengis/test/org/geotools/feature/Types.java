package org.geotools.feature;

import java.util.HashSet;
import java.util.Set;

import org.opengis.feature.schema.AttributeDescriptor;
import org.opengis.feature.schema.ChoiceDescriptor;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.schema.OrderedDescriptor;
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
		Set<FeatureType> all = new HashSet<FeatureType>();
		memberTypes( collectionType, all );
		return all;		
	}
	/**
	 * Collection memberType contributions.
	 * <p>
	 * Tail recursion is used so that subclasses "override" contributions made by the parent.
	 * (This depends on FeatureType identity being completly defined by their QName.
	 * </p>
	 * @param collection
	 * @param all
	 */
	static void memberTypes( FeatureCollectionType collection, Set<FeatureType> all ){
		if( collection == null ) return;
		
		ComplexType superType = collection.getSuper();
		if( superType instanceof FeatureCollectionType){
			memberTypes( (FeatureCollectionType) superType, all );	
		}
		featureTypes( collection.getMemberDescriptor(), all ); // tail recursion		
	}
	/**
	 * Process a descriptor for indicated FeatureTypes.
	 * <p>
	 * This method should not be used directly, please use
	 * memberTypes( collection ) as it will also consider types
	 * contributed by the superclass.
	 * </p>
	 * @param descriptor
	 * @param all
	 */
	static void featureTypes( Descriptor descriptor, Set<FeatureType> all ){
		if( descriptor instanceof AttributeDescriptor ){
			AttributeDescriptor attribute = (AttributeDescriptor) descriptor;
			if( attribute.getType() instanceof FeatureType ) {
				FeatureType type = (FeatureType) attribute.getType();
				all.add( type );
			}			
		}
		else if ( descriptor instanceof ChoiceDescriptor ){
			ChoiceDescriptor choice = (ChoiceDescriptor) descriptor;			
			for( Descriptor option : choice.options() ){
				featureTypes( option, all );
			}
		}
		else if ( descriptor instanceof OrderedDescriptor ){
			OrderedDescriptor list = (OrderedDescriptor) descriptor;			
			for( Descriptor option : list.sequence() ){
				featureTypes( option, all );
			}
		}
		else if ( descriptor instanceof OrderedDescriptor ){
			OrderedDescriptor list = (OrderedDescriptor) descriptor;			
			for( Descriptor option : list.sequence() ){
				featureTypes( option, all );
			}
		}
		else {
			// should not occur
		}
	}
	/**
	 * This method is about as bad as it gets, we need to wander through Descriptor
	 * detecting overrides by AttributeType.
	 * <p>
	 * Almost makes me thing Descriptor should have the attrribute name, and the QName
	 * stuff should be left on AttributeType.
	 * </p>
	 * @param complex
	 * @return Descriptor that actually describes what is valid for the ComplexType.
	 */
	public static Descriptor schema( ComplexType complex ){
		// We need to do this with tail recursion:
		// - and sequence, any, choice gets hacked differently ...
		return complex.getDescriptor();
	}
}
