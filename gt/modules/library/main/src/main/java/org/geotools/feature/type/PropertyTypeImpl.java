package org.geotools.feature.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.PropertyType;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;

public class PropertyTypeImpl implements PropertyType {

	protected final Name name;
	protected final boolean isAbstract;
	protected final Set restrictions;
	protected final InternationalString description;
	protected final Map userData;
	
	public PropertyTypeImpl(
		Name name, boolean isAbstract, Set restrictions, 
		InternationalString description 
	) {
		if(name== null){
			throw new NullPointerException("name");
		}
		this.name = name;
		this.isAbstract = isAbstract;
		if (restrictions == null) {
			this.restrictions = restrictions;
		} else {
			this.restrictions = Collections.unmodifiableSet(restrictions);
		}
		this.description = description;
		this.userData = new HashMap();		
	}
	
	public Name getName() {
		return name;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public Set getRestrictions() {
		return restrictions;
	}

	public InternationalString getDescription() {
		return description;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append("[name=").append(name)
			.append(", abstrsct=, ").append(isAbstract)
			.append(", restrictions=").append(restrictions)
			.append(", description=").append(description);

		return sb.toString();
	}

	public int hashCode() {
		return getName().hashCode()
				^ (getDescription() != null ? getDescription().hashCode() : 17);
	}

	
	public boolean equals(Object other) {
		if (!(other instanceof PropertyType)) {
			return false;
		}
		
		PropertyType prop = (PropertyType) other;
		if (!Utilities.equals(name,prop.getName())) {
			return false;
		}

		if (isAbstract != prop.isAbstract()) {
			return false;
		}

		if (!Utilities.equals(restrictions, prop.getRestrictions())) {
			return false;
		}
		
		if (!Utilities.equals(description,prop.getDescription())) {
			return false;
		}

		return true;

	}

	public Object getUserData(Object key) {
		return userData.get( key );
	}

	public void putUserData(Object key, Object value) {
		if( value == null && userData.containsKey( key )){
			userData.remove( key );
		}
		else if (value != null ){
			userData.put( key, value );
		}
	}
}
