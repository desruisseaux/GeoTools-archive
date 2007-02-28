package org.geotools.data.complex.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeName;
import org.opengis.util.InternationalString;

public class AttributeTypeProxy implements AttributeType {

	private TypeName typeName;

	private Map registry;

	public AttributeTypeProxy(TypeName typeName, Map registry) {
		this.typeName = typeName;
		this.registry = registry;
	}

	public AttributeType getSubject() {
		AttributeType subject = (AttributeType) registry.get(typeName);
		if (subject == null) {
			throw new IllegalStateException("Subject type not loaded yet");
		}
		return subject;
	}

	public Class getBinding() {
		return getSubject().getBinding();
	}

	public Collection getOperations() {
		return null;
	}

	public Set getRestrictions() {
		return getSubject().getRestrictions();
	}

	public AttributeType getSuper() {
		return getSubject().getSuper();
	}

	public boolean isAbstract() {
		return getSubject().isAbstract();
	}

	public boolean isIdentified() {
		return getSubject().isIdentified();
	}

	public InternationalString getDescription() {
		return getSubject().getDescription();
	}

	public TypeName getName() {
		return typeName;
	}

	public Object getUserData(Object key) {
		return getSubject().getUserData(key);
	}

	public void putUserData(Object key, Object value) {
		getSubject().putUserData(key, value);
	}

}
