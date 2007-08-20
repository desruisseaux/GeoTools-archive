/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    Created on Aug 5, 2004
 */
package org.geotools.data.vpf;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;


/**
 * A VPF feature type. Note that feature classes may contain one
 * or more feature types. However, all of the feature types of a 
 * feature class share the same schema. A feature type will therefore
 * delegate its schema related operations to its feature class.
 *
 * @author <a href="mailto:jeff@ionicenterprise.com">Jeff Yutzler</a>
 * @source $URL$
 */
public class VPFFeatureType implements FeatureType {
    /**
     * The feature class that this feature type belongs to
     */
    private final VPFFeatureClass featureClass;
    /**
     * The type name for this specific feature type
     */
    private final String typeName; 
    /**
     * The FACC code, a two-letter, 3-number code
     * identifying the feature type
     */
    private final String faccCode;
    /**
     * Constructor
     * @param cFeatureClass The owning feature class
     * @param cFeature A <code>Feature</code> from the char.vdt file 
     * with more detailed information for this feature type
     */
    public VPFFeatureType(VPFFeatureClass cFeatureClass, Feature cFeature){
        featureClass = cFeatureClass;
        faccCode = cFeature.getAttribute("value").toString().trim();
        String mainTableFileName = cFeature.getAttribute("table").toString().trim();

        String tempTypeName = cFeature.getAttribute("description").toString().trim();

        // This block helps us give tables a distinguishing suffix
        try
        {
            int index = mainTableFileName.lastIndexOf(".") + 1;
            String dimensionality = mainTableFileName.substring(index, index + 1).toLowerCase();
            if (dimensionality.equals("a"))
            {
                tempTypeName = tempTypeName.concat(" Area");
            } else if (dimensionality.equals("l"))
            {
                tempTypeName = tempTypeName.concat(" Line");
            } else if (dimensionality.equals("p"))
            {
                tempTypeName = tempTypeName.concat(" Point");
            } else if (dimensionality.equals("t"))
            {
                tempTypeName = tempTypeName.concat(" Text");
            }
        } catch (RuntimeException e)
        {
            // If this does not work, no big deal
        }
        tempTypeName = tempTypeName.toUpperCase();
        tempTypeName = tempTypeName.replace(' ', '_');
        tempTypeName = tempTypeName.replace('/', '_');
        tempTypeName = tempTypeName.replace('(', '_');
        tempTypeName = tempTypeName.replace(')', '_');
        typeName = tempTypeName;
    }
    /**
     * A constructor for feature types with no information
     * in a char.vdt file.
     * @param cFeatureClass The owning feature class
     */
    public VPFFeatureType(VPFFeatureClass cFeatureClass){
        featureClass = cFeatureClass;
        faccCode = null;
        typeName = cFeatureClass.getTypeName().toUpperCase();
        
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureFactory#create(java.lang.Object[])
     */
    public Feature create(Object[] attributes) throws IllegalAttributeException {
        return featureClass.create(attributes);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureFactory#create(java.lang.Object[], java.lang.String)
     */
    public Feature create(Object[] attributes, String featureID)
        throws IllegalAttributeException {
        return featureClass.create(attributes, featureID);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#duplicate(org.geotools.feature.Feature)
     */
    public Feature duplicate(Feature feature) throws IllegalAttributeException {
        return featureClass.duplicate(feature);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#find(org.geotools.feature.AttributeType)
     */
    public int find(AttributeType type) {
        return featureClass.find(type);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getAncestors()
     */
    public FeatureType[] getAncestors() {
        return featureClass.getAncestors();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getAttributeCount()
     */
    public int getAttributeCount() {
        return featureClass.getAttributeCount();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getAttributeType(int)
     */
    public AttributeType getAttributeType(int position) {
        return featureClass.getAttributeType(position);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getAttributeType(java.lang.String)
     */
    public AttributeType getAttributeType(String xPath) {
        return featureClass.getAttributeType(xPath);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getAttributeTypes()
     */
    public AttributeType[] getAttributeTypes() {
        return featureClass.getAttributeTypes();
    }
    /**
     * @return The <code>VPFCoverage</code> that this <code>FeatureType</code> 
     *         belongs to.
     */
    public VPFCoverage getCoverage() {
        return featureClass.getCoverage();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getPrimaryGeometry()
     */
    public GeometryAttributeType getPrimaryGeometry() {
    	return featureClass.getPrimaryGeometry();
    }
    
    /**
     * @return The <code>String</code> path for the directory containing the
     *         <code>VPFFeatureClass</code> that this <code>FeatureType</code> 
     *         belongs to.
     */
    public String getDirectoryName() {
        return featureClass.getDirectoryName();
    }
    /**
     * @return Returns the featureClass.
     */
    public VPFFeatureClass getFeatureClass() {
        return featureClass;
    }
    /**
     * Returns a list of file objects
     *
     * @return A <code>List</code> containing <code>VPFFile</code> objects.
     */
    public List getFileList() {
        return featureClass.getFileList();
    }
    /**
     * @return A <code>List</code> containing the <code>ColumnPair</code>
     *         objects which identify the file joins for the 
     *         <code>VPFFeatureClass</code> that this <code>FeatureType</code> 
     *         belongs to.
     */
    public List getJoinList() {
        return featureClass.getJoinList();
    }
    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getNamespace()
     */
    public URI getNamespace() {
        return featureClass.getNamespace();
    }
//    /* (non-Javadoc)
//     * @see org.geotools.feature.FeatureType#getNamespace()
//     */
//    public URI getNamespaceURI() {
//        return featureClass.getNamespaceURI();
//    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#getTypeName()
     */
    public String getTypeName() {
        return typeName;
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#hasAttributeType(java.lang.String)
     */
    public boolean hasAttributeType(String xPath) {
        return featureClass.hasAttributeType(xPath);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#isAbstract()
     */
    public boolean isAbstract() {
        return featureClass.isAbstract();
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#isDescendedFrom(org.geotools.feature.FeatureType)
     */
    public boolean isDescendedFrom(FeatureType type) {
        return featureClass.isDescendedFrom(type);
    }

    /* (non-Javadoc)
     * @see org.geotools.feature.FeatureType#isDescendedFrom(java.lang.String, java.lang.String)
     */
    public boolean isDescendedFrom(URI nsURI, String typeName) {
        return featureClass.isDescendedFrom(nsURI, typeName);
    }
    /**
     * The FACC code, a two-letter, 3-number code
     * identifying the feature type
     * @return Returns the FACC Code.
     */
    public String getFaccCode() {
        return faccCode;
    }
	/* (non-Javadoc)
	 * @see org.geotools.feature.FeatureType#find(java.lang.String)
	 */
    public int find(String attName) {
        return featureClass.find(attName);
    }

    public boolean equals(Object obj) {
        return featureClass.equals(obj);
    }
    
    public int hashCode() {
        return featureClass.hashCode();
    }

    
    public AttributeDescriptor getAttribute(Name name) {
		return featureClass.getAttribute(name);
	}

	public AttributeDescriptor getAttribute(String name) {
		return featureClass.getAttribute(name);
	}

	public AttributeDescriptor getAttribute(int index) {
		return featureClass.getAttribute(index);
	}

	public List getAttributes() {
		return featureClass.getAttributes();
	}

	public GeometryType getDefaultGeometryType() {
		return featureClass.getDefaultGeometryType();
	}

	public org.opengis.feature.type.AttributeType getType(Name name) {
		return featureClass.getType( name );
	}

	public org.opengis.feature.type.AttributeType getType(String name) {
		return featureClass.getType( name );
	}

	public org.opengis.feature.type.AttributeType getType(int index) {
		return featureClass.getType( index );
	}

	public List getTypes() {
		return featureClass.getTypes();
	}

	public CoordinateReferenceSystem getCRS() {
		return featureClass.getCRS();
	}

	public AttributeDescriptor getDefaultGeometry() {
		return featureClass.getDefaultGeometry();
	}

	public Collection associations() {
		return featureClass.associations(); 
	}

	public Collection attributes() {
		return featureClass.attributes();
	}

	public Class getBinding() {
		return featureClass.getBinding();
	}

	public Collection getProperties() {
		return featureClass.getProperties();
	}

	public boolean isInline() {
		return featureClass.isInline();
	}

	public Collection getOperations() {
		return featureClass.getOperations();
	}

	public Set getRestrictions() {
		return featureClass.getRestrictions();
	}

	public org.opengis.feature.type.AttributeType getSuper() {
		return featureClass.getSuper();
	}

	public boolean isIdentified() {
		return featureClass.isIdentified();
	}

	public InternationalString getDescription() {
		return featureClass.getDescription();
	}

	public TypeName getName() {
		return featureClass.getName();
	}

	public Object getUserData(Object key) {
		return featureClass.getUserData(key);
	}

	public void putUserData(Object key, Object data) {
		featureClass.putUserData(key, data);
	}

	public int indexOf(String name) {
		return featureClass.indexOf(name);
	}
}
