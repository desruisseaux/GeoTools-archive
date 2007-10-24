package org.geotools.data;

import java.util.Iterator;

import org.geotools.data.store.FeatureIteratorIterator;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

/**
 * 
 * TODO: Merge with RTFC in main when FC proposal gets figured out.
 *
 */
public class ReTypingFeatureCollection extends DecoratingFeatureCollection {
    
    SimpleFeatureType featureType;
    
    public ReTypingFeatureCollection ( ContentFeatureCollection delegate, SimpleFeatureType featureType ) {
        super(delegate);
        this.featureType = featureType;
    }
    
    public SimpleFeatureType getSchema() {
        return featureType;
    }
    
    public Iterator iterator() {
        return new FeatureIteratorIterator( features() );
    }
    
    public void close(Iterator close) {
        if ( close instanceof FeatureIteratorIterator ) {
            FeatureIterator fi = ((FeatureIteratorIterator)close).getDelegate(); 
            close( fi );
        }
        else {
            delegate.close( close );
        }
    }
    
    public FeatureIterator features() {
        return new ReTypingIterator( delegate.features(), delegate.getSchema(), featureType );
    }
    
    public void close(FeatureIterator close) {
        if ( close instanceof ReTypingIterator ) {
            close = ((ReTypingIterator) close).getDelegate();
        }
        
        delegate.close( close );
    }
}

class ReTypingIterator implements FeatureIterator {

    /**
     * The delegate iterator
     */
    FeatureIterator delegate;
    /**
     * The target feature type
     */
    SimpleFeatureType target;
    /**
     * The matching types from target 
     */
    AttributeDescriptor[] types;
    
    public ReTypingIterator( FeatureIterator delegate, SimpleFeatureType source, SimpleFeatureType target ) {
        this.delegate = delegate;
        this.target = target;
        types = typeAttributes( source, target );
    }
    
    public FeatureIterator getDelegate() {
        return delegate;
    }

    public boolean hasNext() {
        return delegate.hasNext();
    }

    public SimpleFeature next() {
        SimpleFeature next = (SimpleFeature) delegate.next();
        String id = next.getID();

        Object[] attributes = new Object[types.length];
        String xpath;

        try {
            for (int i = 0; i < types.length; i++) {
                xpath = types[i].getLocalName();
                //attributes[i] = types[i].duplicate(next.getAttribute(xpath));
                attributes[i] = next.getAttribute(xpath);
            }

            return SimpleFeatureBuilder.build(target, attributes, id);
        } 
        catch (IllegalAttributeException e) {
            throw new RuntimeException( e );
        }
    }
    
    public void close() {
        delegate.close();
    }
    
    
     /**
     * Supplies mapping from origional to target FeatureType.
     * 
     * <p>
     * Will also ensure that origional can cover target
     * </p>
     *
     * @param target Desired FeatureType
     * @param origional Origional FeatureType
     *
     * @return Mapping from originoal to target FeatureType
     *
     * @throws IllegalArgumentException if unable to provide a mapping
     */
    protected AttributeDescriptor[] typeAttributes(SimpleFeatureType original,
        SimpleFeatureType target) {
        if (target.equals(original)) {
            throw new IllegalArgumentException(
                "FeatureReader allready produces contents with the correct schema");
        }

        if (target.getAttributeCount() > original.getAttributeCount()) {
            throw new IllegalArgumentException(
                "Unable to retype FeatureReader (origional does not cover requested type)");
        }

        String xpath;
        AttributeDescriptor[] types = new AttributeDescriptor[target.getAttributeCount()];

        for (int i = 0; i < target.getAttributeCount(); i++) {
            AttributeDescriptor attrib = target.getAttribute(i);
            xpath = attrib.getLocalName();
            types[i] = attrib;

            if (!attrib.equals(original.getAttribute(xpath))) {
                throw new IllegalArgumentException(
                    "Unable to retype FeatureReader (origional does not cover "
                    + xpath + ")");
            }
        }

        return types;
    }

}
