package org.geotools.feature.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.geotools.feature.type.AssociationDescriptorImpl;
import org.geotools.feature.type.AssociationTypeImpl;
import org.geotools.feature.type.FeatureCollectionTypeImpl;
import org.geotools.feature.type.Types;
import org.opengis.feature.simple.SimpleFeatureCollectionType;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;

public class SimpleFeatureCollectionTypeImpl extends FeatureCollectionTypeImpl
		implements SimpleFeatureCollectionType {

	public SimpleFeatureCollectionTypeImpl(Name name,
			AssociationDescriptor member, Set restrictions,
			InternationalString description) {
		super(name, Collections.EMPTY_LIST, Collections.singleton(member), null, null,
				false, restrictions, null, description);
	}

	public SimpleFeatureCollectionTypeImpl(Name name,
			SimpleFeatureType member, InternationalString description) {
		super( name, Collections.EMPTY_LIST, members(member), null, member.getCRS(), false,
				Collections.EMPTY_SET, null, description);
	}

	private static final List members(SimpleFeatureType member) {
		AssociationType aggregation = new AssociationTypeImpl(Types
				.typeName("contained"), member, false, false,
				Collections.EMPTY_SET, null, null);

		AssociationDescriptor memberOf = new AssociationDescriptorImpl(
				aggregation, Types.typeName("memberOf"), 0, Integer.MAX_VALUE);

		return Collections.singletonList(memberOf);
	}

	public Set getMemberTypes() {
	    return Collections.unmodifiableSet( MEMBERS );
	}
	public SimpleFeatureType getMemberType() {
		if (MEMBERS.isEmpty())
			return null;

		AssociationDescriptor ad = (AssociationDescriptor) MEMBERS.iterator()
				.next();

		if (ad != null) {
			return (SimpleFeatureType) ad.getType().getReferenceType();
		}
		return null;
	}

	public AttributeDescriptor getAttribute(Name name) {
		return null;
	}

	public AttributeDescriptor getAttribute(String name) {
		return null;
	}

	public AttributeDescriptor getAttribute(int index) {
		return null;
	}

	public int getAttributeCount() {
		return 0;
	}

	public List getAttributes() {
		return Collections.EMPTY_LIST;
	}

	public GeometryType getDefaultGeometryType() {
		return null;
	}

	public AttributeType getType(Name name) {
		return null;
	}

	public AttributeType getType(String name) {
		return null;
	}

	public AttributeType getType(int index) {
		return null;
	}

	public List getTypes() {
		return Collections.EMPTY_LIST;
	}

	public int indexOf(String name) {
		return -1;
	}
}