package org.geotools.feature;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

/**
This is a sample implementation of the FeatureType API.
 * <p>
 * The suffix implementation is used to indicat this is a direct, straight forward
 * implementation of the associated interface.
 * </p>
 * <p>
 * This serves as, my reference implementation, if you have any questions please
 * contact me (jgarnett@refractions.net) or jump on the geotools-devel email list.
 * There is a good chance my understanding is wrong, I expect and want feedback on
 * this implementation.
 * </p>
 * <p>
 * I especially am interested in:
 * <ul>
 * <li>XPATH - xpath is used to locate AttributeTyp instances. Does this even work?
 *     FeatureType captures schema information, you cannot do an xpath query based on
 *     @attribute for example?
 * <li>Split between information captured by a FeatureType and the complete description
 * of a Schema indicated by a FeatureType and its parents.
 * <li>Single or Multiple inhieratance. This implementation is based on the idea of single
 * inhieratance although the "OpenGIS" Reference model allows seems to indicate multiple is
 * preferable.
 * </ul>
 * </p>
 * <p>
 * <b>Note:</b> javadoc methods are provided on methods, they describe the implementation for
 * those interested in extendeding this class. All useage information is captude by the
 * FeatureType information. (So if you are reading this it is because you are working on a
 * subclass).
 * </p>
 * @author Jody Garnett, Refractions Research
 */
public class FeatureTypeImpl implements FeatureType {
    final private URI namespace;
    final private String typeName;
    final private List/*<FeatureType>*/ ancestors;
    final private boolean isAbstract;
    final private List /*AttributeType*/ attribtues;
    final private GeometryAttributeType deafultGeometry;
    /**
     * No direct construction is "Allowed", please use a FeatureTypeBuilder as per 
     * design.
     * <p>
     * This interface is *not* marked as final, however every last field is bolted down
     * as private final. Any subclass is stuck using the API, this is negotiable - things
     * are locked down so you have to email me (jgarnett@refractions.net), cause I suspect
     * if you need to change something we should have an API discussion and consider broader
     * changes. (Consider this a volunteer recuritment practice).
     * </p>
     * <p>
     * Responsibilities:
     * <ul>
     * <li>Implement FeatureType
     * <li>Document the needs of people extending FeatureTypeImpl for a "custom use".
     * <li>Generate email discussion about FeatureType API issues.
     * </ul>
     * </p>
     * @author Jody Garnett
     * @since 2.1.0
     */
    protected FeatureTypeImpl( URI namespace, String typeName, boolean isAbstract, AttributeType attributeTypes[], GeometryAttributeType defaultGeometry, FeatureType ancestors[] ){
        this.namespace = namespace;
        this.typeName = typeName;
        this.ancestors = Arrays.asList( ancestors );
        this.isAbstract = isAbstract;
        this.attribtues = Arrays.asList( attributeTypes );
        this.deafultGeometry = defaultGeometry;
    }

    public boolean equals( Object arg0 ) {
        return false;
    }
    
    /*
     * @see org.geotools.feature.FeatureType#getNamespace()
     */
    public URI getNamespace() {
        return namespace;
    }

    /*
     * @see org.geotools.feature.FeatureType#getTypeName()
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * This method is implemented by a straight reference to FeatureTypes
     * isDecendedFrom( type, namespace, typename ).
     * <p>
     * Please consider this a query of this FeatureType's ancestors. For specific modules
     * like GML they may want to do a bit more <i>magic</i> here, not especially encouraged
     * but the API allows for it.
     * @see org.geotools.feature.FeatureType#isDescendedFrom(java.net.URI, java.lang.String)
     */
    public boolean isDescendedFrom( URI namespace, String typeName ) {
        return FeatureTypes.isDecendedFrom( this, namespace, typeName );
    }

    /**
     * This method is implemented by a straight reference to FeatureTypes
     * isDecendedFrom( typeA, typeB ).
     * 
     * @see org.geotools.feature.FeatureType#isDescendedFrom(org.geotools.feature.FeatureType)
     */
    public boolean isDescendedFrom( FeatureType type ) {
        return FeatureTypes.isDecendedFrom( this, type );
    }

    /*
     * @see org.geotools.feature.FeatureType#isAbstract()
     */
    public boolean isAbstract() {
        return isAbstract;
    }

    /*
     * @see org.geotools.feature.FeatureType#getAncestors()
     */
    public FeatureType[] getAncestors() {
        return (FeatureType[]) ancestors.toArray( new FeatureType[ ancestors.size() ]);
    }

    /*
     * @see org.geotools.feature.FeatureType#getDefaultGeometry()
     */
    public GeometryAttributeType getDefaultGeometry() {
        return deafultGeometry;
    }

    /**
     * This produces the number of distinct featuretypes avaialble via this schema.
     * 
     * @see org.geotools.feature.FeatureType#getAttributeCount()
     */
    public int getAttributeCount() {
        return Schema.attribtueCount( this );
    }

    /*
     * @see org.geotools.feature.FeatureType#getAttributeType(java.lang.String)
     */
    public AttributeType getAttributeType( String xPath ) {
        return Schema.xpath( this, xPath );
    }

    /*
     * @see org.geotools.feature.FeatureType#find(org.geotools.feature.AttributeType)
     */
    public int find( AttributeType type ) {
        return Schema.find( this, type.getName() );
    }

    /*
     * @see org.geotools.feature.FeatureType#find(java.lang.String)
     */
    public int find( String attName ) {
        return Schema.find( this, attName );
    }

    /*
     * @see org.geotools.feature.FeatureType#getAttributeType(int)
     */
    public AttributeType getAttributeType( int position ) {
        return Schema.attribute( this, position );
    }

    /*
     * @see org.geotools.feature.FeatureType#getAttributeTypes()
     */
    public AttributeType[] getAttributeTypes() {
        return (AttributeType[]) attribtues.toArray( new AttributeType[ attribtues.size() ]);
    }

    /*
     * @see org.geotools.feature.FeatureType#hasAttributeType(java.lang.String)
     */
    public boolean hasAttributeType( String xPath ) {
        return false;
    }

    /*
     * @see org.geotools.feature.FeatureType#duplicate(org.geotools.feature.Feature)
     */
    public Feature duplicate( Feature feature ) throws IllegalAttributeException {
        throw new UnsupportedOperationException();
    }

    /*
     * @see org.geotools.feature.FeatureType#create(java.lang.Object[])
     */
    public Feature create( Object[] attributes ) throws IllegalAttributeException {
        throw new UnsupportedOperationException();
    }

    /*
     * @see org.geotools.feature.FeatureType#create(java.lang.Object[], java.lang.String)
     */
    public Feature create( Object[] attributes, String featureID ) throws IllegalAttributeException {
        throw new UnsupportedOperationException();
    }

}
