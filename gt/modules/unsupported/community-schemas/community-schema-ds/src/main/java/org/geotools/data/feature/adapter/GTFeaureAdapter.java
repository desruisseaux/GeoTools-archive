package org.geotools.data.feature.adapter;

import java.util.Collection;
import java.util.Iterator;

import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.iso.AttributeBuilder;
import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Attribute;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Adapter from ISO SimpleFeature to GeoTools Feature
 * 
 * @author Gabriel Roldan, Axios Engineering
 * 
 */
public class GTFeaureAdapter implements org.geotools.feature.SimpleFeature {

    private SimpleFeature adaptee;

    private FeatureType gtType;


    public GTFeaureAdapter(SimpleFeature adaptee, FeatureType gtType) {
        if(adaptee instanceof ISOFeatureAdapter){
            throw new IllegalArgumentException("No need to adapt ISOFEatureAdapter, use getAdaptee() instead");
        }
        this.adaptee = adaptee;
        this.gtType = gtType;
    }

    public Object getAttribute(String xPath) {
        Object value = adaptee.get(xPath);
        return value;
    }

    public Object getAttribute(int index) {
        Object value = adaptee.get(index);
        return value;
    }

    public Object[] getAttributes(Object[] attributes) {
        Collection atts = adaptee.attributes();
        Object[] values = new Object[atts.size()];
        int current = 0;
        for (Iterator it = atts.iterator(); it.hasNext();) {
            Attribute att = (Attribute) it.next();
            values[current] = att.get();
            current++;
        }
        return values;
    }

    public Envelope getBounds() {
        ReferencedEnvelope envelope = new ReferencedEnvelope(adaptee.getCRS());
        envelope.init(adaptee.getBounds());
        return envelope;
    }

    public Geometry getDefaultGeometry() {
        GeometryAttribute defaultGeometry = adaptee.getDefaultGeometry();
        return (Geometry) (defaultGeometry == null ? null : defaultGeometry
                .get());
    }

    public FeatureType getFeatureType() {
        return gtType;
    }

    public String getID() {
        return adaptee.getID();
    }

    public int getNumberOfAttributes() {
        return adaptee.getNumberOfAttributes();
    }

    public void setAttribute(int position, Object val)
            throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
        adaptee.set(position, val);
    }

    public void setAttribute(String xPath, Object attribute)
            throws IllegalAttributeException {
        adaptee.set(xPath, attribute);
    }

    public void setDefaultGeometry(Geometry geometry)
            throws IllegalAttributeException {
        SimpleFeatureType type = (SimpleFeatureType) adaptee.getType();
        AttributeDescriptor descriptor = type.getDefaultGeometry();
        GeometryType defGeomType = (GeometryType) descriptor.getType();

        AttributeFactoryImpl attributeFactory = new AttributeFactoryImpl();
        AttributeBuilder builder = new AttributeBuilder(attributeFactory);
        builder.setType(defGeomType);
        builder.add(geometry, defGeomType.getName());

        GeometryAttribute geometryAttribute;
        geometryAttribute = (GeometryAttribute) builder.build();
        adaptee.setDefaultGeometry(geometryAttribute);
    }

    public void setAttributes(Object[] attributes)
            throws IllegalAttributeException {

        Collection atts = adaptee.attributes();

        Object[] values = new Object[atts.size()];
        int current = 0;
        for (Iterator it = atts.iterator(); it.hasNext();) {
            Attribute att = (Attribute) it.next();
            att.set(attributes[current]);
            current++;
        }

    }

}
