package org.geotools.feature.type;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyType;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;

public class PropertyTypeImpl implements PropertyType {

	protected final Name name;
	protected final Class<?> binding;
	protected final boolean isAbstract;
	protected final PropertyType superType;
	protected final List<Filter> restrictions;
	protected final InternationalString description;
	protected final Map<Object,Object> userData;
	
	public PropertyTypeImpl(
		Name name, Class<?> binding, boolean isAbstract, List<Filter> restrictions, 
		PropertyType superType, InternationalString description 
	) {
		if(name== null){
			throw new NullPointerException("name");
		}
		if(binding == null) {
		    throw new NullPointerException("binding");
		}
		this.name = name;
		this.binding = binding;
		this.isAbstract = isAbstract;
		
		if (restrictions == null) {
			this.restrictions = restrictions;
		} else {
			this.restrictions = Collections.unmodifiableList(restrictions);
		}
		
		this.superType = superType;
		this.description = description;
		this.userData = new HashMap<Object,Object>();		
	}
	
	public Name getName() {
		return name;
	}

	public Class<?> getBinding() {
	    return binding;
	}
	
	public boolean isAbstract() {
		return isAbstract;
	}

	public List<Filter> getRestrictions() {
		return restrictions;
	}

    public PropertyType getSuper() {
        return superType;
    }
	    
	public InternationalString getDescription() {
		return description;
	}
	
	public int hashCode() {
		return getName().hashCode() ^ getBinding().hashCode()
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

		if (!Utilities.equals(binding, prop.getBinding())) {
		    return false;
		}
		
		if (isAbstract != prop.isAbstract()) {
			return false;
		}

		if (!Utilities.equals(restrictions, prop.getRestrictions())) {
			return false;
		}
		
		if (!Utilities.equals(superType, prop.getSuper())) {
		    return false;
		}
		
		if (!Utilities.equals(description,prop.getDescription())) {
			return false;
		}

		return true;
	}

	public Map<Object,Object> getUserData() {
	    return userData;
	}
	
	public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getName()).append(":");
        sb.append("name=").append(name)
            .append("; binding=").append(binding)
            .append("; isAbstrsact=, ").append(isAbstract)
            .append("; restrictions=").append(restrictions)
            .append("; description=").append(description)
            .append("; super=[").append(superType).append("]");
            
        return sb.toString();
    }

}
