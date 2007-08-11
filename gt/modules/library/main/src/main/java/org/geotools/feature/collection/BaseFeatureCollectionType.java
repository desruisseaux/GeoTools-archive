package org.geotools.feature.collection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypes;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.Name;
import org.geotools.feature.type.AssociationDescriptorImpl;
import org.geotools.feature.type.AssociationTypeImpl;
import org.geotools.feature.type.TypeName;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AssociationType;

/**
 * Limited implementation of SimpleFeatureCollectionType for internal use
 * by this class.
 * <p>
 * This type is completely empty in that it contains no attribute types, the only
 * goal is to cough up the correct member type.
 * </p>
 * @author Justin Deoliveira (The Open Planning Project)
 */
public class BaseFeatureCollectionType extends DefaultFeatureType 
	implements org.geotools.feature.FeatureType, SimpleFeatureCollectionType {
    final FeatureType memberType;
	public BaseFeatureCollectionType(FeatureType memberType) {
	    super( new TypeName( FeatureTypes.DEFAULT_NAMESPACE.toString(), "AbstractFeatureColletionType") ,
	           null, // no attribute types
	           null, // no super types (although I wish we had "AbstractFeatureColletionType"
	           null  // no default geometry
	    );		           
	    this.memberType = memberType;
		//super( new TypeName(FeatureTypes.DEFAULT_NAMESPACE.toString(), "AbstractFeatureColletionType"), memberType, null );
	}
	
	public SimpleFeatureType getMemberType() {
	    return memberType;
	}
	public Set getMemberTypes() {
	    return Collections.singleton( getMemberType() );
	}
	public Set getMembers() {
	    AssociationType contains = new AssociationTypeImpl( new TypeName("contains"), memberType, false, false, Collections.EMPTY_SET, null, null ); 
	    AssociationDescriptorImpl member = new AssociationDescriptorImpl( null, new Name("member"), 0, Integer.MAX_VALUE );
	    return Collections.singleton( member );
	}
	public Feature create(Object[] attributes) throws IllegalAttributeException {
	    throw new UnsupportedOperationException("Types of feature collection do not support feature creation");
	}

	public Feature create(Object[] attributes, String featureID) throws IllegalAttributeException {
		throw new UnsupportedOperationException("Types of feature collection do not support feature creation");
	}

	public Feature duplicate(Feature feature) throws IllegalAttributeException {
		throw new UnsupportedOperationException("Types of feature collection do not support feature creation");
	}

	public int find(AttributeType type) {
		return find( type.getLocalName() );
	}

	public int find(String attName) {
		return indexOf(attName);
	}

	public FeatureType[] getAncestors() {
		return new FeatureType[]{};
	}

	public AttributeType getAttributeType(String xPath) {
		return (AttributeType) getAttribute(name);
	}

	public AttributeType getAttributeType(int position) {
		return (AttributeType) getAttribute(position);
	}

	public AttributeType[] getAttributeTypes() {
		return new AttributeType[]{};
	}

	public URI getNamespace() {
		try {
			return new URI(getName().getNamespaceURI());
		} 
		catch (URISyntaxException e) {
			//cant happen
			return null;
		}
	}

	public GeometryAttributeType getPrimaryGeometry() {
		return (GeometryAttributeType) getDefaultGeometry();
	}

	public String getTypeName() {
		return getName().getLocalPart();
	}

	public boolean hasAttributeType(String xPath) {
		return indexOf(xPath) != -1;
	}

	public boolean isDescendedFrom(URI nsURI, String typeName) {
		return false;
	}

	public boolean isDescendedFrom(FeatureType type) {
		return false;
	}

}