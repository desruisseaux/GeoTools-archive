/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
package org.geotools.xml.wfs;

import org.geotools.data.Query;
import org.geotools.data.wfs.LockRequest;
import org.geotools.xml.PrintHandler;
import org.geotools.xml.gml.GMLSchema;
import org.geotools.xml.ogc.FilterSchema;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeValue;
import org.geotools.xml.schema.Choice;
import org.geotools.xml.schema.DefaultChoice;
import org.geotools.xml.schema.DefaultFacet;
import org.geotools.xml.schema.DefaultSequence;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Facet;
import org.geotools.xml.schema.Sequence;
import org.geotools.xml.schema.SimpleType;
import org.geotools.xml.wfs.WFSBasicComplexTypes.FeatureCollectionType;
import org.geotools.xml.wfs.WFSBasicComplexTypes.QueryType;
import org.geotools.xml.wfs.WFSSchema.WFSAttribute;
import org.geotools.xml.wfs.WFSSchema.WFSComplexType;
import org.geotools.xml.wfs.WFSSchema.WFSElement;
import org.geotools.xml.xsi.XSISimpleTypes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import javax.naming.OperationNotSupportedException;


/**
 * <p>
 * DOCUMENT ME!
 * </p>
 *
 * @author dzwiers
 */
public class WFSTransactionComplexTypes {
    /**
     * <p>
     * This class represents an TransactionType within the WFS Schema.  This
     * includes both the data and parsing functionality associated with a
     * TransactionType.
     * </p>
     *
     * @see WFSComplexType
     */
    static class TransactionType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new TransactionType();

        public static WFSComplexType getInstance() {
            return instance;
        }
        
//        <xsd:complexType name="TransactionType">
//	      <xsd:annotation>
//	         <xsd:documentation>
//	            The TranactionType defines the Transaction operation.  A
//	            Transaction element contains one or more Insert, Update
//	            Delete and Native elements that allow a client application
//	            to create, modify or remove feature instances from the 
//	            feature repository that a Web Feature Service controls.
//	         </xsd:documentation>
//	      </xsd:annotation>
//	      <xsd:sequence>
//	         <xsd:element ref="wfs:LockId" minOccurs="0">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  In order for a client application to operate upon locked
//	                  feature instances, the Transaction request must include
//	                  the LockId element.  The content of this element must be
//	                  the lock identifier the client application obtained from
//	                  a previous GetFeatureWithLock or LockFeature operation.
//
//	                  If the correct lock identifier is specified the Web
//	                  Feature Service knows that the client application may
//	                  operate upon the locked feature instances.
//
//	                  No LockId element needs to be specified to operate upon
//	                  unlocked features.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	         <xsd:choice minOccurs="0" maxOccurs="unbounded">
//	            <xsd:element ref="wfs:Insert"/>
//	            <xsd:element ref="wfs:Update"/>
//	            <xsd:element ref="wfs:Delete"/>
//	            <xsd:element ref="wfs:Native"/>
//	         </xsd:choice>
//	      </xsd:sequence>
//	      <xsd:attribute name="version"
//	                     type="xsd:string" use="required" fixed="1.0.0"/>
//	      <xsd:attribute name="service"
//	                     type="xsd:string" use="required" fixed="WFS"/>
//	      <xsd:attribute name="handle"
//	                     type="xsd:string" use="optional"/>
//	      <xsd:attribute name="releaseAction"
//	                     type="wfs:AllSomeType" use="optional">
//	         <xsd:annotation>
//	            <xsd:documentation>
//	               The releaseAction attribute is used to control how a Web
//	               Feature service releases locks on feature instances after
//	               a Transaction request has been processed.
//
//	               Valid values are ALL or SOME.
//
//	               A value of ALL means that the Web Feature Service should
//	               release the locks of all feature instances locked with the
//	               specified lockId, regardless or whether or not the features
//	               were actually modified.
//
//	               A value of SOME means that the Web Feature Service will 
//	               only release the locks held on feature instances that 
//	               were actually operated upon by the transaction.  The lockId
//	               that the client application obtained shall remain valid and
//	               the other, unmodified, feature instances shall remain locked.
//	               If the expiry attribute was specified in the original operation 
//	               that locked the feature instances, then the expiry counter
//	               will be reset to give the client application that same amount
//	               of time to post subsequent transactions against the locked
//	               features.
//	            </xsd:documentation>
//	         </xsd:annotation>
//	      </xsd:attribute>
//	   </xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		new WFSElement("LockId",XSISimpleTypes.String.getInstance(),0,1,false,null),
				new WFSElement("Insert",InsertElementType.getInstance()),
				new WFSElement("Update",UpdateElementType.getInstance()),
				new WFSElement("Delete",DeleteElementType.getInstance()),
				new WFSElement("Native",NativeType.getInstance())
        };
        private static Sequence child = new DefaultSequence(new ElementGrouping[]{elems[0],new DefaultChoice(null, 0, Integer.MAX_VALUE,new Element[]{elems[1],elems[2],elems[3],elems[4]})});
        private static Attribute[] attrs = new Attribute[]{
        		new WFSAttribute("version",XSISimpleTypes.String.getInstance(),Attribute.REQUIRED){
                	public String getFixed(){
                		return "1.0.0";
                	}
                },
				new WFSAttribute("service",XSISimpleTypes.String.getInstance(),Attribute.REQUIRED){
                	public String getFixed(){
                		return "WFS";
                	}
                },
				new WFSAttribute("handle",XSISimpleTypes.String.getInstance(),Attribute.OPTIONAL),
				new WFSAttribute("lockAction",AllSomeType.getInstance(),Attribute.OPTIONAL)
        };

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attrs;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "TransactionType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class GetFeatureWithLockType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new FeatureCollectionType();

        public static WFSComplexType getInstance() {
            return instance;
        }
        
//        <xsd:complexType name="GetFeatureWithLockType">
//	      <xsd:annotation>
//	         <xsd:documentation>
//	            A GetFeatureWithLock request operates identically to a
//	            GetFeature request expect that it attempts to lock the
//	            feature instances in the result set and includes a lock
//	            identifier in its response to a client.  A lock identifier
//	            is an identifier generated by a Web Feature Service that 
//	            a client application can use, in subsequent operations,
//	            to reference the locked set of feature instances.
//	         </xsd:documentation>
//	      </xsd:annotation>
//	      <xsd:sequence>
//	         <xsd:element ref="wfs:Query" maxOccurs="unbounded"/>
//	      </xsd:sequence>
//	      <xsd:attribute name="version"
//	                     type="xsd:string" use="required" fixed="1.0.0"/>
//	      <xsd:attribute name="service"
//	                     type="xsd:string" use="required" fixed="WFS"/>
//	      <xsd:attribute name="handle"
//	                     type="xsd:string" use="optional"/>
//	      <xsd:attribute name="expiry"
//	                     type="xsd:positiveInteger" use="optional"/>
//	      <xsd:attribute name="outputFormat"
//	                     type="xsd:string" use="optional" default="GML2"/>
//	      <xsd:attribute name="maxFeatures"
//	                     type="xsd:positiveInteger" use="optional"/>
//	   </xsd:complexType>
        
        private static Element[] elems = new Element[]{new WFSElement("Query", QueryType.getInstance(),1,Integer.MAX_VALUE,false,null)};
        private static Sequence child = new DefaultSequence(elems);
        private static Attribute[] attrs = new Attribute[]{
        new WFSAttribute("version",XSISimpleTypes.String.getInstance(),Attribute.REQUIRED){
        	public String getFixed(){
        		return "1.0.0";
        	}
        },
		new WFSAttribute("service",XSISimpleTypes.String.getInstance(),Attribute.REQUIRED){
        	public String getFixed(){
        		return "WFS";
        	}
        },
		new WFSAttribute("handle",XSISimpleTypes.String.getInstance(),Attribute.OPTIONAL),
		new WFSAttribute("outputFormat",XSISimpleTypes.String.getInstance(),Attribute.OPTIONAL,"GML2"),
		new WFSAttribute("maxFeatures",XSISimpleTypes.PositiveInteger.getInstance(),Attribute.OPTIONAL)};

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attrs;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "GetFeatureWithLockType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return Query.class;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            if(element.getType()!=null && getName().equals(element.getType().getName())){
                return (value == null || value instanceof Query);
            }
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            if(canEncode(element,value,hints)){
                AttributesImpl attributes = new AttributesImpl();
                attributes.addAttribute(WFSSchema.NAMESPACE.toString(),attrs[0].getName(),null,"string",attrs[0].getFixed());
                attributes.addAttribute(WFSSchema.NAMESPACE.toString(),attrs[1].getName(),null,"string",attrs[1].getFixed());
                attributes.addAttribute(WFSSchema.NAMESPACE.toString(),attrs[2].getName(),null,"string",attrs[3].getDefault());
                Query query = (Query)value;
                if(query!=null && query.getMaxFeatures()!=Query.DEFAULT_MAX)
                    attributes.addAttribute(WFSSchema.NAMESPACE.toString(),elems[3].getName(),null,"integer",""+query.getMaxFeatures());
                if(hints!=null){
                String lockId = (String)hints.get(WFSBasicComplexTypes.LOCK_KEY);
                if(lockId!=null){
                	attributes.addAttribute(WFSSchema.NAMESPACE.toString(),elems[2].getName(),null,"string",lockId);
                }}
                output.startElement(element.getNamespace(),element.getName(),attributes);
                elems[0].getType().encode(elems[0],value,output,hints);
                output.endElement(element.getNamespace(),element.getName());
            }else{
                throw new OperationNotSupportedException("not a valid value/element for a DescribeFeatureTypeType.");
            }
        }
    }

    static class LockFeatureType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new FeatureCollectionType();

        public static WFSComplexType getInstance() {
            return instance;
        }

//        <xsd:complexType name="LockFeatureType">
//           <xsd:annotation>
//              <xsd:documentation>
//                 This type defines the LockFeature operation.  The LockFeature
//                 element contains one or more Lock elements that define
//                 which features of a particular type should be locked.  A lock
//                 identifier (lockId) is returned to the client application which
//                 can be used by subsequent operations to reference the locked
//                 features.
//              </xsd:documentation>
//           </xsd:annotation>
//           <xsd:sequence>
//              <xsd:element name="Lock" type="wfs:LockType" maxOccurs="unbounded">
//                 <xsd:annotation>
//                    <xsd:documentation>
//                       The lock element is used to indicate which feature 
//                       instances of particular type are to be locked.
//                    </xsd:documentation>
//                 </xsd:annotation>
//              </xsd:element>
//           </xsd:sequence>
//           <xsd:attribute name="version"
//                          type="xsd:string" use="required" fixed="1.0.0"/>
//           <xsd:attribute name="service"
//                          type="xsd:string" use="required" fixed="WFS"/>
//           <xsd:attribute name="expiry"
//                          type="xsd:positiveInteger" use="optional"/>
//           <xsd:attribute name="lockAction"
//                          type="wfs:AllSomeType" use="optional">
//              <xsd:annotation>
//                 <xsd:documentation>
//                    The lockAction attribute is used to indicate what
//                    a Web Feature Service should do when it encounters
//                    a feature instance that has already been locked by
//                    another client application.
//
//                    Valid values are ALL or SOME.
//
//                    ALL means that the Web Feature Service must acquire
//                    locks on all the requested feature instances.  If it
//                    cannot acquire those locks then the request should
//                    fail.  In this instance, all locks acquired by the
//                    operation should be released.
//      
//                    SOME means that the Web Feature Service should lock
//                    as many of the requested features as it can.
//                 </xsd:documentation>
//              </xsd:annotation>
//           </xsd:attribute>
//        </xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		new WFSElement("Lock",LockType.getInstance(),1,Integer.MAX_VALUE,false,null),
        };
        private Sequence child = new DefaultSequence(elems);
        private Attribute[] attrs = new Attribute[]{
        		new WFSAttribute("version",XSISimpleTypes.String.getInstance(),Attribute.REQUIRED){
                	public String getFixed(){
                		return "1.0.0";
                	}
                },
				new WFSAttribute("service",XSISimpleTypes.String.getInstance(),Attribute.REQUIRED){
                	public String getFixed(){
                		return "WFS";
                	}
                },
				new WFSAttribute("expiry",XSISimpleTypes.PositiveInteger.getInstance(),Attribute.OPTIONAL),
				new WFSAttribute("lockAction",AllSomeType.getInstance(),Attribute.OPTIONAL)
        		
        };
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attrs;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "LockFeatureType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            return LockRequest.class;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class LockType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new LockType();

        public static WFSComplexType getInstance() {
            return instance;
        }
        
//        <xsd:complexType name="LockType">
//           <xsd:annotation>
//              <xsd:documentation>
//                 This type defines the Lock element.  The Lock element
//                 defines a locking operation on feature instances of 
//                 a single type. An OGC Filter is used to constrain the
//                 scope of the operation.  Features to be locked can be
//                 identified individually by using their feature identifier
//                 or they can be locked by satisfying the spatial and 
//                 non-spatial constraints defined in the filter.
//              </xsd:documentation>
//           </xsd:annotation>
//           <xsd:sequence>
//              <xsd:element ref="ogc:Filter" minOccurs="0" maxOccurs="1"/>
//           </xsd:sequence>
//           <xsd:attribute name="handle" 
//                          type="xsd:string" use="optional"/>
//           <xsd:attribute name="typeName" 
//                          type="xsd:QName" use="required"/>
//        </xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		new WFSElement(FilterSchema.getInstance().getElements()[2].getName(),FilterSchema.getInstance().getElements()[2].getType(),0,1,false,FilterSchema.getInstance().getElements()[2].getSubstitutionGroup()){
        	        public URI getNamespace() {
        	            return FilterSchema.NAMESPACE;
        	        }
        		},
        };
        private static Sequence child = new DefaultSequence(elems);
        private static Attribute[] attrs = new Attribute[]{
        		new WFSAttribute("handle",XSISimpleTypes.String.getInstance(),Attribute.OPTIONAL),
				new WFSAttribute("typeName",XSISimpleTypes.QName.getInstance(),Attribute.REQUIRED)
        };

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attrs;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "LockType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class InsertElementType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new InsertElementType();

        public static WFSComplexType getInstance() {
            return instance;
        }
        
//        <xsd:complexType name="InsertElementType">
//           <xsd:sequence>
//              <xsd:element ref="gml:_Feature" maxOccurs="unbounded"/>
//           </xsd:sequence>
//           <xsd:attribute name="handle" type="xsd:string" use="optional"/>
//        </xsd:complexType>
        private static Element[] elems = new Element[]{
        		new WFSElement(GMLSchema.getInstance().getElements()[0].getName(),GMLSchema.getInstance().getElements()[0].getType(),1,Integer.MAX_VALUE,GMLSchema.getInstance().getElements()[0].isAbstract(),GMLSchema.getInstance().getElements()[0].getSubstitutionGroup()){
        			public URI getNamespace(){
        				return GMLSchema.NAMESPACE;
        			}
        		}
        };
        private static Sequence child = new DefaultSequence(elems);
        private static Attribute[] attrs= new Attribute[]{
        		new WFSAttribute("handler",XSISimpleTypes.String.getInstance(),Attribute.OPTIONAL),
        };

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attrs;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "InsertElementType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class UpdateElementType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new UpdateElementType();

        public static WFSComplexType getInstance() {
            return instance;
        }
        
//        <xsd:complexType name="UpdateElementType">
//	      <xsd:sequence>
//	         <xsd:element ref="wfs:Property" maxOccurs="unbounded" />
//	         <xsd:element ref="ogc:Filter" minOccurs="0" maxOccurs="1">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  The Filter element is used to constrain the scope
//	                  of the update operation to those features identified
//	                  by the filter.  Feature instances can be specified
//	                  explicitly and individually using the identifier of
//	                  each feature instance OR a set of features to be
//	                  operated on can be identified by specifying spatial
//	                  and non-spatial constraints in the filter.
//	                  If no filter is specified, then the update operation 
//	                  applies to all feature instances.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	      </xsd:sequence>
//	      <xsd:attribute name="handle" type="xsd:string" use="optional"/>
//	      <xsd:attribute name="typeName" type="xsd:QName" use="required"/>
//	   </xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		new WFSElement("Property",PropertyType.getInstance(),0,Integer.MAX_VALUE,true,null),
        		new WFSElement(FilterSchema.getInstance().getElements()[2].getName(),FilterSchema.getInstance().getElements()[2].getType(),0,1,false,FilterSchema.getInstance().getElements()[2].getSubstitutionGroup()){
        	        public URI getNamespace() {
        	            return FilterSchema.NAMESPACE;
        	        }
        		}
        };
        private static Sequence child = new DefaultSequence(elems);
        private static Attribute[] attrs = new Attribute[]{
        		new WFSAttribute("handler",XSISimpleTypes.String.getInstance(),Attribute.OPTIONAL),
        		new WFSAttribute("typeName",XSISimpleTypes.QName.getInstance(),Attribute.REQUIRED),
        };

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attrs;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "UpdateElementType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class DeleteElementType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new DeleteElementType();

        public static WFSComplexType getInstance() {
            return instance;
        }

//        <xsd:complexType name="DeleteElementType">
//	      <xsd:sequence>
//	         <xsd:element ref="ogc:Filter" minOccurs="1" maxOccurs="1">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  The Filter element is used to constrain the scope
//	                  of the delete operation to those features identified
//	                  by the filter.  Feature instances can be specified
//	                  explicitly and individually using the identifier of
//	                  each feature instance OR a set of features to be
//	                  operated on can be identified by specifying spatial
//	                  and non-spatial constraints in the filter.
//	                  If no filter is specified then an exception should
//	                  be raised since it is unlikely that a client application
//	                  intends to delete all feature instances.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	      </xsd:sequence>
//	      <xsd:attribute name="handle" type="xsd:string" use="optional"/>
//	      <xsd:attribute name="typeName" type="xsd:QName" use="required"/>
//	   </xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		new WFSElement(FilterSchema.getInstance().getElements()[2].getName(),FilterSchema.getInstance().getElements()[2].getType(),0,1,false,FilterSchema.getInstance().getElements()[2].getSubstitutionGroup()){
        	        public URI getNamespace() {
        	            return FilterSchema.NAMESPACE;
        	        }
        		}
        };
        private static Sequence child = new DefaultSequence(elems);
        private static Attribute[] attrs = new Attribute[]{
        		new WFSAttribute("handler",XSISimpleTypes.String.getInstance(),Attribute.OPTIONAL),
        		new WFSAttribute("typeName",XSISimpleTypes.QName.getInstance(),Attribute.REQUIRED),
        };

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attrs;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "DeleteElementType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class NativeType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new NativeType();

        public static WFSComplexType getInstance() {
            return instance;
        }

//        <xsd:complexType name="NativeType">
//	      <xsd:attribute name="vendorId" type="xsd:string" use="required">
//	         <xsd:annotation>
//	            <xsd:documentation>
//	               The vendorId attribute is used to specify the name of
//	               vendor who's vendor specific command the client
//	               application wishes to execute.
//	            </xsd:documentation>
//	         </xsd:annotation>
//	      </xsd:attribute>
//	      <xsd:attribute name="safeToIgnore" type="xsd:boolean" use="required">
//	         <xsd:annotation>
//	            <xsd:documentation>
//	               In the event that a Web Feature Service does not recognize
//	               the vendorId or does not recognize the vendor specific command,
//	               the safeToIgnore attribute is used to indicate whether the 
//	               exception can be safely ignored.  A value of TRUE means that
//	               the Web Feature Service may ignore the command.  A value of
//	               FALSE means that a Web Feature Service cannot ignore the
//	               command and an exception should be raised if a problem is 
//	               encountered.
//	            </xsd:documentation>
//	         </xsd:annotation>
//	      </xsd:attribute>
//	   </xsd:complexType>
        
        private static Attribute[] attrs = new Attribute[]{
        		new WFSAttribute("vendorId",XSISimpleTypes.String.getInstance(),Attribute.REQUIRED),
        		new WFSAttribute("safeToIgnore",XSISimpleTypes.Boolean.getInstance(),Attribute.REQUIRED),
        };

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attrs;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "NativeType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class PropertyType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new PropertyType();

        public static WFSComplexType getInstance() {
            return instance;
        }
        
//        <xsd:complexType name="PropertyType">
//	      <xsd:sequence>
//	         <xsd:element name="Name" type="xsd:string">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  The Name element contains the name of a feature property
//	                  to be updated.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	         <xsd:element name="Value" minOccurs="0">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  The Value element contains the replacement value for the
//	                  named property.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	      </xsd:sequence>
//	   </xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		new WFSElement("Name",XSISimpleTypes.String.getInstance()),
        		new WFSElement("Value",WFSEmptyType.getInstance(),0,1,true,null){
        			public boolean isMixed(){return true;}
        		},
        };
        private static Sequence child = new DefaultSequence(elems);

        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "PropertyType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class WFS_LockFeatureResponseType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new WFS_LockFeatureResponseType();

        public static WFSComplexType getInstance() {
            return instance;
        }

//        <xsd:complexType name="WFS_LockFeatureResponseType">
//	      <xsd:annotation>
//	         <xsd:documentation>
//	            The WFS_LockFeatureResponseType is used to define an
//	            element to contains the response to a LockFeature
//	            operation.
//	         </xsd:documentation>
//	      </xsd:annotation>
//	      <xsd:sequence>
//	         <xsd:element ref="wfs:LockId">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  The WFS_LockFeatureResponse includes a LockId element
//	                  that contains a lock identifier.  The lock identifier
//	                  can be used by a client, in subsequent operations, to
//	                  operate upon the locked feature instances.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	         <xsd:element name="FeaturesLocked"
//	                      type="wfs:FeaturesLockedType" minOccurs="0">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  The LockFeature or GetFeatureWithLock operations
//	                  identify and attempt to lock a set of feature 
//	                  instances that satisfy the constraints specified 
//	                  in the request.  In the event that the lockAction
//	                  attribute (on the LockFeature or GetFeatureWithLock
//	                  elements) is set to SOME, a Web Feature Service will
//	                  attempt to lock as many of the feature instances from
//	                  the result set as possible.
//
//	                  The FeaturesLocked element contains list of ogc:FeatureId
//	                  elements enumerating the feature instances that a WFS
//	                  actually managed to lock.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	         <xsd:element name="FeaturesNotLocked"
//	                      type="wfs:FeaturesNotLockedType" minOccurs="0">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  In contrast to the FeaturesLocked element, the
//	                  FeaturesNotLocked element contains a list of 
//	                  ogc:Filter elements identifying feature instances
//	                  that a WFS did not manage to lock because they were
//	                  already locked by another process.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	      </xsd:sequence>
//	   </xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		new WFSElement("LockId",XSISimpleTypes.String.getInstance()),
				new WFSElement("FeaturesLocked",FeaturesLockedType.getInstance(),0,1,true,null),
				new WFSElement("FeaturesNotLocked",FeaturesNotLockedType.getInstance(),0,1,true,null)
        };
        
        private static Sequence child = new DefaultSequence(elems);
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "WFS_LockFeatureResponseType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class FeaturesLockedType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new FeaturesLockedType();

        public static WFSComplexType getInstance() {
            return instance;
        }

//     <xsd:complexType name="FeaturesLockedType">
//	     <xsd:sequence maxOccurs="unbounded">
//	       <xsd:element ref="ogc:FeatureId"/>
//	     </xsd:sequence>
//	   </xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		FilterSchema.getInstance().getElements()[1],
        };
        private static Sequence child = new DefaultSequence(null,elems,1,Integer.MAX_VALUE);
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "FeaturesLockedType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class FeaturesNotLockedType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new FeaturesNotLockedType();

        public static WFSComplexType getInstance() {
            return instance;
        }

//        <xsd:complexType name="FeaturesNotLockedType">
//	     <xsd:sequence maxOccurs="unbounded">
//	       <xsd:element ref="ogc:FeatureId"/>
//	     </xsd:sequence>
//	   </xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		FilterSchema.getInstance().getElements()[1],
        };
        private static Sequence child = new DefaultSequence(null,elems,1,Integer.MAX_VALUE);
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "FeaturesNotLockedType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class WFS_TransactionResponseType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new WFS_TransactionResponseType();

        public static WFSComplexType getInstance() {
            return instance;
        }

//        <xsd:complexType name="WFS_TransactionResponseType">
//	      <xsd:annotation>
//	         <xsd:documentation>
//	            The WFS_TransactionResponseType defines the format of
//	            the XML document that a Web Feature Service generates 
//	            in response to a Transaction request.  The response 
//	            includes the completion status of the transaction 
//	            and the feature identifiers of any newly created
//	            feature instances.
//	         </xsd:documentation>
//	      </xsd:annotation>
//	      <xsd:sequence>
//	         <xsd:element name="InsertResult"
//	                      type="wfs:InsertResultType"
//	                      minOccurs="0" maxOccurs="unbounded">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  The InsertResult element contains a list of ogc:FeatureId
//	                  elements that identify any newly created feature instances.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	         <xsd:element name="TransactionResult"
//	                      type="wfs:TransactionResultType">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  The TransactionResult element contains a Status element
//	                  indicating the completion status of a transaction.  In
//	                  the event that the transaction fails, additional element
//	                  may be included to help locate which part of the transaction
//	                  failed and why.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	      </xsd:sequence>
//	      <xsd:attribute name="version"
//	                     type="xsd:string" use="required" fixed="1.0.0"/>
//	   </xsd:complexType>
        
        private static Element[] elems = new Element[] {
        		new WFSElement("InsertResult",InsertResultType.getInstance(),0,Integer.MAX_VALUE,true,null),
				new WFSElement("TransactionResult",TransactionResultType.getInstance()),
        };
        private static Sequence child = new DefaultSequence(elems);
        private static Attribute[] attrs = new Attribute[]{
        		new WFSAttribute("version",XSISimpleTypes.String.getInstance(),Attribute.REQUIRED){
        			public String getFixed(){return "1.0.0";}
        		},
        };
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attrs;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "WFS_TransactionResponseType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class TransactionResultType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new TransactionResultType();

        public static WFSComplexType getInstance() {
            return instance;
        }
        
//        <xsd:complexType name="TransactionResultType">
//	      <xsd:sequence>
//	         <xsd:element name="Status" type="wfs:StatusType">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  The Status element contains an element indicating the
//	                  completion status of a transaction.  The SUCCESS element
//	                  is used to indicate successful completion.  The FAILED
//	                  element is used to indicate that an exception was 
//	                  encountered.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	         <xsd:element name="Locator" type="xsd:string" minOccurs="0">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  In the event that an exception was encountered while 
//	                  processing a transaction, a Web Feature Service may
//	                  use the Locator element to try and identify the part
//	                  of the transaction that failed.  If the element(s)
//	                  contained in a Transaction element included a handle
//	                  attribute, then a Web Feature Service may report the
//	                  handle to identify the offending element.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	         <xsd:element name="Message" type="xsd:string" minOccurs="0">
//	            <xsd:annotation>
//	               <xsd:documentation>
//	                  The Message element may contain an exception report
//	                  generated by a Web Feature Service when an exception
//	                  is encountered.
//	               </xsd:documentation>
//	            </xsd:annotation>
//	         </xsd:element>
//	      </xsd:sequence>
//	      <xsd:attribute name="handle" type="xsd:string" use="optional"/>
//	   </xsd:complexType>
        
        private static Element[] elems = new Element[] {
        		new WFSElement("Status",StatusType.getInstance()),
				new WFSElement("Locator",XSISimpleTypes.String.getInstance(),0,1,true,null),
				new WFSElement("Message",XSISimpleTypes.String.getInstance(),0,1,true,null)
        };
        private static Sequence child = new DefaultSequence(elems);
        private static Attribute[] attrs = new Attribute[]{
        		new WFSAttribute("handle",XSISimpleTypes.String.getInstance(),Attribute.OPTIONAL),
        };
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attrs;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "TransactionResultType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class InsertResultType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new InsertResultType();

        public static WFSComplexType getInstance() {
            return instance;
        }
        
//        <xsd:complexType name="InsertResultType">
//	      <xsd:sequence>
//	         <xsd:element ref="ogc:FeatureId" maxOccurs="unbounded"/>
//	      </xsd:sequence>
//	      <xsd:attribute name="handle" type="xsd:string" use="optional"/>
//	   </xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		FilterSchema.getInstance().getElements()[1],
        };
        private static Sequence child = new DefaultSequence(null,elems,1,Integer.MAX_VALUE);
        private static Attribute[] attrs = new Attribute[]{
        		new WFSAttribute("handle",XSISimpleTypes.String.getInstance(),Attribute.OPTIONAL),
        };
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return attrs;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "InsertResultType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }

    static class StatusType extends WFSComplexType {
        // singleton instance
        private static final WFSComplexType instance = new StatusType();

        public static WFSComplexType getInstance() {
            return instance;
        }
        
//        <xsd:complexType name="StatusType">
//	      <xsd:choice>
//	         <xsd:element ref="wfs:SUCCESS"/>
//	         <xsd:element ref="wfs:FAILED"/>
//	         <xsd:element ref="wfs:PARTIAL"/>
//	      </xsd:choice>
//	   </xsd:complexType>
        
        private static Element[] elems = new Element[]{
        		new WFSElement("SUCCESS",WFSEmptyType.getInstance()),
				new WFSElement("FAILED",WFSEmptyType.getInstance()),
				new WFSElement("FAILED",WFSEmptyType.getInstance()),
        };
        private static Choice child = new DefaultChoice(elems);
        
        /**
         * @see org.geotools.xml.schema.ComplexType#getAttributes()
         */
        public Attribute[] getAttributes() {
            return null;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChild()
         */
        public ElementGrouping getChild() {
            return child;
        }

        /**
         * @see org.geotools.xml.schema.ComplexType#getChildElements()
         */
        public Element[] getChildElements() {
            return elems;
        }

        /**
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element,
         *      org.geotools.xml.schema.ElementValue[],
         *      org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue(Element element, ElementValue[] value,
            Attributes attrs, Map hints)
            throws SAXException, SAXNotSupportedException {
            // TODO Auto-generated method stub
            throw new SAXNotSupportedException();
        }

        /**
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            return "StatusType";
        }

        /**
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element,
         *      java.lang.Object, java.util.Map)
         */
        public boolean canEncode(Element element, Object value, Map hints) {
            // TODO Auto-generated method stub
            return false;
        }

        /**
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element,
         *      java.lang.Object, org.geotools.xml.PrintHandler,
         *      java.util.Map)
         */
        public void encode(Element element, Object value, PrintHandler output,
            Map hints) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            throw new OperationNotSupportedException();
        }
    }
    
    private static class WFSEmptyType extends WFSComplexType{
    	private static WFSComplexType instance = new WFSEmptyType();
    	public static WFSComplexType getInstance(){return instance;}
    	
// 	   <xsd:complexType name="EmptyType"/>
 	
 	
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getAttributes()
		 */
		public Attribute[] getAttributes() {
			return null;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChild()
		 */
		public ElementGrouping getChild() {
			return null;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return null;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
		 */
		public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
			return null;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.Type#getName()
		 */
		public String getName() {
			return "EmptyType";
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.Type#getInstanceType()
		 */
		public Class getInstanceType() {
			return null;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
		 */
		public boolean canEncode(Element element, Object value, Map hints) {
			return element!=null;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
		 */
		public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
			output.element(element.getNamespace(),element.getName(),null);
		}
    }

    private static class AllSomeType implements SimpleType{
    	private static SimpleType instance = new AllSomeType();
    	public static SimpleType getInstance(){return instance;}

//    	   <xsd:simpleType name="AllSomeType">
//    	      <xsd:restriction base="xsd:string">
//    	         <xsd:enumeration value="ALL"/>
//    	         <xsd:enumeration value="SOME"/>
//    	      </xsd:restriction>
//    	   </xsd:simpleType>
		   
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.SimpleType#getFinal()
		 */
		public int getFinal() {
			return 0;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.SimpleType#getId()
		 */
		public String getId() {
			return null;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.SimpleType#toAttribute(org.geotools.xml.schema.Attribute, java.lang.Object, java.util.Map)
		 */
		public AttributeValue toAttribute(Attribute attribute, Object value, Map hints) {
			// TODO Auto-generated method stub
			return null;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.SimpleType#canCreateAttributes(org.geotools.xml.schema.Attribute, java.lang.Object, java.util.Map)
		 */
		public boolean canCreateAttributes(Attribute attribute, Object value, Map hints) {
			// TODO Auto-generated method stub
			return false;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.SimpleType#getChildType()
		 */
		public int getChildType() {
			return RESTRICTION;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.SimpleType#getParents()
		 */
		public SimpleType[] getParents() {
			return new SimpleType[]{XSISimpleTypes.String.getInstance(),};
		}
		private static Facet[] facets = new Facet[]{
				new DefaultFacet(Facet.ENUMERATION,"ALL"),
				new DefaultFacet(Facet.ENUMERATION,"SOME")
		};
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.SimpleType#getFacets()
		 */
		public Facet[] getFacets() {
			return facets;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
		 */
		public Object getValue(Element element, ElementValue[] value, Attributes attrs, Map hints) throws SAXException, SAXNotSupportedException {
			if(value == null || value.length!=1 || element == null || element.getType() == null)
				throw new SAXNotSupportedException("invalid inputs");
			if(value[0].getValue() instanceof String){
				String t = (String)value[0].getValue();
				if("ALL".equals(t) || "SOME".equals(t))
					return t;
				throw new SAXException("Invalid value: not ALL or NONE");
			}else{
				throw new SAXNotSupportedException("Invalid child value type.");
			}
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.Type#getName()
		 */
		public String getName() {
			return "AllSomeType";
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.Type#getNamespace()
		 */
		public URI getNamespace() {
			return WFSSchema.NAMESPACE;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.Type#getInstanceType()
		 */
		public Class getInstanceType() {
			return String.class;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
		 */
		public boolean canEncode(Element element, Object value, Map hints) {
			return element!=null && element.getType()!=null && getName().equals(element.getType().getName()) && value instanceof String && ("ALL".equals(value) || "SOME".equals(value));
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
		 */
		public void encode(Element element, Object value, PrintHandler output, Map hints) throws IOException, OperationNotSupportedException {
			if(canEncode(element,value,hints))
			output.startElement(element.getNamespace(),element.getName(),null);
			output.characters((String)value);
			output.endElement(element.getNamespace(),element.getName());
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
		 */
		public Element findChildElement(String name) {
			return null;
		}
    }
}
