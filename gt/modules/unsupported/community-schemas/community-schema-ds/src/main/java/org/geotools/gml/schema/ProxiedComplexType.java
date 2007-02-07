package org.geotools.gml.schema;

import java.util.List;
import java.util.Set;

import org.opengis.feature.AttributeName;
import org.opengis.feature.schema.Descriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.TypeFactory;

class ProxiedComplexType implements ComplexType {

	private AttributeName typeName;
	private TypeFactory repository;
	private ComplexType proxied;
	
	public ProxiedComplexType(AttributeName typeName, TypeFactory repository){
		this.typeName = typeName;
		this.repository = repository;
	}
	
	private ComplexType resolve(){
		if(proxied == null){
			proxied = (ComplexType)repository.getType(typeName);
		}
		return proxied;
	}
	public Class getBinding() {
		return List.class;
	}

	public Descriptor getDescriptor() {
		
		return resolve().getDescriptor();
	}

	public AttributeType type(String name) {
		
		return resolve().type(name);
	}

	public AttributeName getName() {
		
		return typeName;
	}

	public boolean isIdentified() {
		
		return resolve().isIdentified();
	}

	public AttributeType getSuper() {
		
		return resolve().getSuper();
	}

	public boolean isAbstract() {
		
		return resolve().isAbstract();
	}

	public Set getRestrictions() {
		
		return resolve().getRestrictions();
	}

	public Boolean isNillable() {
		
		return resolve().isNillable();
	}

	public void validate(Object value) {
		

	}

}
