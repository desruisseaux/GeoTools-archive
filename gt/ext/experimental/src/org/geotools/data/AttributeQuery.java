package org.geotools.data;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.IllegalFilterException;

/**
 * Provide a defined of derived AttribtueType during a Query (immutable).
 * <p>
 * In practive this works as an "As" statement in SQL. This class is inmutable.
 * <p>
 * @author jgarnett
 * @since 0.6.0
 */
public class AttributeQuery {
    /** The attribtueType of that this AttributeQuery produces */
    final public AttributeType type;
    
    /** The expression used to obtain the value */
    final public Expression expr;
    
    /** Default Factory - because that is all anyone ever does */
    static FilterFactory filterfactory = FilterFactory.createFilterFactory();
    
    /** Default Factory - because that is all anyone ever does */
    static AttributeTypeFactory attributeFactory = AttributeTypeFactory.defaultInstance();
    
    /**
     * Construct <code>AttributeQuery</code>, basically a no-op that just copies attributeType across.
     * 
     * @param schema Schema to query against
     * @param attributeName Simple attribute name, unsure if xpath is allowed
     * @throws IllegalFilterException
     */
    public AttributeQuery( FeatureType schema, String attributeName ) throws IllegalFilterException {        
        this( schema.getAttributeType( attributeName ), filterfactory.createAttributeExpression( schema, attributeName ) );       
    }
    
    /**
     * Construct <code>AttributeQuery</code>.
     * <p>
     * You can use this for generic mapping from an expression to an attributeType.
     * </p>
     * @param attributeName
     * @param expression
     */
    public AttributeQuery( AttributeType type, Expression expression ){
        this.type = type;
        expr = expression;
    }        
}