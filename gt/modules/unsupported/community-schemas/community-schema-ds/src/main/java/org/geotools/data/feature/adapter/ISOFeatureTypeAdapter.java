/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.data.feature.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.iso.TypeBuilder;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.type.TypeFactoryImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.TypeFactory;
import org.opengis.feature.type.TypeName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

/**
 * Adapts a GeoTools FeatureType to an ISO SimpleFeatureType
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class ISOFeatureTypeAdapter implements FeatureType, SimpleFeatureType {

    private org.geotools.feature.FeatureType type;

    private FeatureType isoType;

    public ISOFeatureTypeAdapter(org.geotools.feature.FeatureType featureType) {
        if (featureType instanceof GTFeatureTypeAdapter) {
            throw new IllegalArgumentException(
                    "No need to adapt GTFeatureTypeAdapter, use getAdaptee() instead");
        }
        this.type = featureType;

        String nsUri = type.getNamespace() == null ? null : type.getNamespace()
                .toString();

        org.geotools.feature.AttributeType[] types = type.getAttributeTypes();

        TypeFactory tf = new TypeFactoryImpl();
        TypeBuilder builder = new TypeBuilder(tf);

        for (int i = 0; i < types.length; i++) {
            org.geotools.feature.AttributeType gtType = types[i];

            AttributeType attType = ISOAttributeTypeAdapter.adapter(nsUri,
                    gtType);

            Name name = Types.attributeName(nsUri, attType.getName().getLocalPart());
            builder.addAttribute(name, attType);
        }
        builder.defaultGeometry(featureType.getPrimaryGeometry().getLocalName());
        builder.setName(type.getTypeName());
        isoType = builder.feature();
    }

    public org.geotools.feature.FeatureType getAdaptee() {
        return type;
    }

    public TypeName getName() {
        String ns = type.getNamespace() == null ? null : type.getNamespace()
                .toString();
        String localName = type.getTypeName();
        org.geotools.feature.type.TypeName name = new org.geotools.feature.type.TypeName(
                ns, localName);
        return name;
    }

    public Object getUserData(Object key) {
        return null;
    }

    public void putUserData(Object key, Object data) {
        // do nothing
    }

    public CoordinateReferenceSystem getCRS() {
        GeometryAttributeType defGeom = type.getPrimaryGeometry();
        return defGeom == null ? null : defGeom.getCoordinateSystem();
    }

    public AttributeDescriptor getDefaultGeometry() {
        AttributeDescriptor defaultGeometry = isoType.getDefaultGeometry();
        return defaultGeometry;
    }

    public Collection associations() {
        return isoType.associations();
    }

    public Collection attributes() {
        return isoType.attributes();
    }

    public Class getBinding() {
        return isoType.getBinding();
    }

    public Collection getProperties() {
        return isoType.getProperties();
    }

    public boolean isInline() {
        return isoType.isInline();
    }

    public Collection getOperations() {
        return isoType.getOperations();
    }

    public Set getRestrictions() {
        return isoType.getRestrictions();
    }

    public AttributeType getSuper() {
        return isoType.getSuper();
    }

    public boolean isAbstract() {
        return false;
    }

    public boolean isIdentified() {
        return true;
    }

    public InternationalString getDescription() {
        return null;
    }

    public GeometryType defaultGeometry() {
        return ((SimpleFeatureType) isoType).defaultGeometry();
    }

    public AttributeType get(Name name) {
        return get(name.getLocalPart());
    }

    public AttributeType get(String name) {
        int index = type.find(name);
        return get(index);
    }

    public AttributeType get(int index) {
        return ((SimpleFeatureType) isoType).get(index);
    }

    public int getNumberOfAttribtues() {
        return type.getAttributeCount();
    }

    public int indexOf(String name) {
        return type.find(name);
    }

    public List types() {
        return ((SimpleFeatureType) isoType).types();
    }

}
