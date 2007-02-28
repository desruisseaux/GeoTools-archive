package org.geotools.data.complex.config;

import java.util.Collection;
import java.util.Map;

import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.TypeName;

public class ComplexTypeProxy extends AttributeTypeProxy implements ComplexType {

	public ComplexTypeProxy(TypeName typeName, Map registry) {
		super(typeName, registry);
	}

	public Collection associations() {
		return ((ComplexType)getSubject()).associations();
	}

	public Collection attributes() {
		return ((ComplexType)getSubject()).attributes();
	}

	public Collection getProperties() {
		return ((ComplexType) getSubject()).getProperties();
	}

	public boolean isInline() {
		return ((ComplexType)getSubject()).isInline();
	}

}
