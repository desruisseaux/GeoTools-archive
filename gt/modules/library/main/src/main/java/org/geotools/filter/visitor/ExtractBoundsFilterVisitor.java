package org.geotools.filter.visitor;

import java.util.logging.Logger;

import org.geotools.filter.DefaultExpression;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Extract a a bounding box from the provided Filter.
 * 
 * This is a replacement for FilterConsumer.
 * @author Jody Garnett
 */
public class ExtractBoundsFilterVisitor extends DefaultFilterVisitor {
    private static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.index.rtree");
    
    private ReferencedEnvelope bounds;
    
    public ExtractBoundsFilterVisitor( CoordinateReferenceSystem crs ){
        bounds = new ReferencedEnvelope( crs );
    }
    
    public Envelope getBounds() {
        return this.bounds;
    }
    
    @Override
    public Object visit( ExcludeFilter filter, Object data ) {
        bounds.setToNull();        
        return bounds;
    }
    
    @Override
    public Object visit( IncludeFilter filter, Object data ) {
        // also consider making use of CRS extent?
        bounds.expandToInclude(new Envelope(Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY));
        return bounds;
    }
    
    @Override
    public Object visit( BBOX filter, Object data ) {
        // consider doing reprojection here into data CRS?
        Envelope other = new Envelope(filter.getMinX(),
                filter.getMaxX(), filter.getMinY(),
                filter.getMinY());        
        bounds.expandToInclude(other);
        return bounds;
    }
    
    @Override
    public Object visit( Literal expression, Object data ) {
        Object value = expression.getValue();
        if( value instanceof Geometry ){
            Geometry geometry = (Geometry) value;
            this.bounds.expandToInclude( geometry.getEnvelopeInternal() ); 
        }
        else {
            LOGGER.warning("LiteralExpression ignored!");
        }        
        return bounds;
    }

}
