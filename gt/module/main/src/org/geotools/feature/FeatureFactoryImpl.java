package org.geotools.feature;

import java.util.Collection;

import org.opengis.feature.Association;
import org.opengis.feature.Attribute;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureCollectionType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.spatialschema.geometry.geometry.GeometryFactory;

/**
 * Factory for creating instances of the Attribute family of classes.
 * 
 * @author Ian Schneider
 * @author Gabriel Roldan
 * @author Justin Deoliveira
 * 
 * @version $Id$
 */
public class FeatureFactoryImpl implements FeatureFactory {
 
	/**
	 * Factory used to create CRS objects
	 */
    CRSFactory crsFactory;
    /**
     * Factory used to create geomtries
     */
    GeometryFactory  geometryFactory;
    
    public CRSFactory getCRSFactory() {
        return crsFactory;
    }

    public void setCRSFactory(CRSFactory crsFactory) {
        this.crsFactory = crsFactory;
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    public void setGeometryFactory(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }
    
    public Association createAssociation(Attribute related, AssociationDescriptor descriptor) {
        throw new UnsupportedOperationException();
    	//return new AssociationImpl(related,descriptor);
    }
	
	public Attribute createAttribute( Object value, AttributeDescriptor descriptor, String id ) {
		throw new UnsupportedOperationException();
		//return new AttributeImpl(value,descriptor,id);
	}
	
	public GeometryAttribute createGeometryAttribute(
		Object value, AttributeDescriptor desc, String id, CoordinateReferenceSystem crs
	) {
	
		throw new UnsupportedOperationException();
		//return new GeometricAttribute(value,desc,id,crs);
	}
	
	public ComplexAttribute createComplexAttribute( 
		Collection value, AttributeDescriptor desc, String id
	) {
		throw new UnsupportedOperationException();
		//return new ComplexAttributeImpl(value, desc, id );
	}

	public ComplexAttribute createComplexAttribute( Collection value, ComplexType type, String id ) 
	{
		throw new UnsupportedOperationException();
		//return new ComplexAttributeImpl(value, type, id );
	}
	
	public Feature createFeature(Collection value, AttributeDescriptor desc, String id) {
		throw new UnsupportedOperationException();
		//return new FeatureImpl(value, desc, id);
	}

	public Feature createFeature(Collection value, FeatureType type, String id) {
		throw new UnsupportedOperationException();
		//return new FeatureImpl(value, type, id);
	}
	
	public FeatureCollection createFeatureCollection(
		Collection value, AttributeDescriptor desc, String id
	) {
		throw new UnsupportedOperationException();
		//return new FeatureCollectionImpl(value, desc, id);
	}
	
	public FeatureCollection createFeatureCollection(
		Collection value, FeatureCollectionType type, String id
	) {
		throw new UnsupportedOperationException();
		//return new FeatureCollectionImpl(value, type, id);
	}
   
}

