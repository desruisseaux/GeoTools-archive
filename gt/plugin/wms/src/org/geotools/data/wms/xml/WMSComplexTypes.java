/*
 * Created on 27-Oct-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.xml;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.geotools.data.wms.xml.WMSSchema.WMSAttribute;
import org.geotools.data.wms.xml.WMSSchema.WMSComplexType;
import org.geotools.data.wms.xml.WMSSchema.WMSElement;
import org.geotools.xml.PrintHandler;
import org.geotools.xml.schema.Attribute;
import org.geotools.xml.schema.AttributeValue;
import org.geotools.xml.schema.Element;
import org.geotools.xml.schema.ElementGrouping;
import org.geotools.xml.schema.ElementValue;
import org.geotools.xml.schema.Facet;
import org.geotools.xml.schema.Sequence;
import org.geotools.xml.schema.SimpleType;
import org.geotools.xml.schema.impl.FacetGT;
import org.geotools.xml.schema.impl.SequenceGT;
import org.geotools.xml.xsi.XSISimpleTypes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WMSComplexTypes {
    static class OperationType extends WMSComplexType {
        private static final WMSComplexType instance = new OperationType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Format", XSISimpleTypes.String.getInstance(), 1,
                Integer.MAX_VALUE),
            new WMSElement("DCPType", _DCPTypeType.getInstance(), 1, Integer.MAX_VALUE)    
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}
    }
    
    protected static class _WMS_CapabilitiesType extends WMSComplexType {
        private static final WMSComplexType instance = new _WMS_CapabilitiesType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Service", _ServiceType.getInstance()),
            new WMSElement("Capability", _CapabilityType.getInstance())
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        private static Attribute[] attrs = new Attribute[] {
            new WMSAttribute(null, "version", WMSSchema.NAMESPACE, XSISimpleTypes.String.getInstance(), Attribute.REQUIRED, null, "1.3.0", false),
            new WMSAttribute("updateSequence", XSISimpleTypes.String.getInstance())
        };
        
        public static WMSComplexType getInstance() {
            return instance;
        }

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getAttributes()
		 */
		public Attribute[] getAttributes() {
			return attrs;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChild()
		 */
		public ElementGrouping getChild() {
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}
    }
    
    protected static class _ServiceType extends WMSComplexType {
        private static final WMSComplexType instance = new _ServiceType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Title", XSISimpleTypes.String.getInstance()),
            new WMSElement("Abstract", XSISimpleTypes.String.getInstance(), 0, 1),
            new WMSElement("KeywordList", _KeywordListType.getInstance(), 0, 1),
            new WMSElement("OnlineResource", _OnlineResourceType.getInstance()),
            new WMSElement("ContactInformation", _ContactInformationType.getInstance(), 0, 1),
            new WMSElement("Fees", XSISimpleTypes.String.getInstance(), 0, 1),
            new WMSElement("AccessConstraints", XSISimpleTypes.String.getInstance(), 0, 1),
            new WMSElement("LayerLimit", XSISimpleTypes.PositiveInteger.getInstance(), 0, 1),
            new WMSElement("MaxWidth", XSISimpleTypes.PositiveInteger.getInstance(), 0, 1),
            new WMSElement("MaxHeight", XSISimpleTypes.PositiveInteger.getInstance(), 0, 1)
        };
        
        private static Sequence seq = new SequenceGT(elems);

        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}
    }
    
    protected static class _KeywordListType extends WMSComplexType {
        private static final WMSComplexType instance = new _KeywordListType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Keyword", _KeywordType.getInstance(), 0, Integer.MAX_VALUE)
        };
        
        private static Sequence seq = new SequenceGT(elems);
        public static WMSComplexType getInstance() {
            return instance;
        }
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
			return seq;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}        
    }
    
    protected static class _KeywordType extends WMSComplexType {
        private static final WMSComplexType instance = new _KeywordType();

        private static Attribute[] attributes = {
            new WMSAttribute("vocabulary", XSISimpleTypes.String.getInstance())
        };
        public static WMSComplexType getInstance() {
            return instance;
        }
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getAttributes()
		 */
		public Attribute[] getAttributes() {
			return attributes;
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
    }
    
    protected static class _ContactInformationType extends WMSComplexType {
        private static final WMSComplexType instance = new _ContactInformationType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("ContactPersonPrimary", _ContactPersonPrimaryType.getInstance(), 0, 1),
            new WMSElement("ContactPosition", XSISimpleTypes.String.getInstance(), 0, 1),
            new WMSElement("ContactAddress", _ContactAddressType.getInstance(), 0, 1),
            new WMSElement("ContactVoiceTelephone", XSISimpleTypes.String.getInstance(), 0, 1),
            new WMSElement("ContactFacsimileTelephone", XSISimpleTypes.String.getInstance(), 0, 1),
            new WMSElement("ContactElectronicMailAddress", XSISimpleTypes.String.getInstance(), 0, 1)
        };
        
        private static Sequence seq = new SequenceGT(elems);
        public static WMSComplexType getInstance() {
            return instance;
        }
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
			return seq;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}        
    }
    
    protected static class _ContactPersonPrimaryType extends WMSComplexType {
        private static final WMSComplexType instance = new _ContactPersonPrimaryType();
        
        private static Element[] elems = new Element [] {
            new WMSElement("ContactPerson", XSISimpleTypes.String.getInstance()),
            new WMSElement("ContactOrganization", XSISimpleTypes.String.getInstance())
        };
        private static Sequence seq = new SequenceGT(elems);
        public static WMSComplexType getInstance() {
            return instance;
        }
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
			return seq;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}        
    }
    
    protected static class _ContactAddressType extends WMSComplexType {
        private static final WMSComplexType instance = new _ContactAddressType();
        
        private static Element[] elems = new Element [] { 
            new WMSElement("AddressType", XSISimpleTypes.String.getInstance()),
            new WMSElement("Address", XSISimpleTypes.String.getInstance()),
            new WMSElement("City", XSISimpleTypes.String.getInstance()),
            new WMSElement("StateOrProvince", XSISimpleTypes.String.getInstance()),
            new WMSElement("PostCode", XSISimpleTypes.String.getInstance()),
            new WMSElement("Country", XSISimpleTypes.String.getInstance())
        };
        private static Sequence seq = new SequenceGT(elems);
        public static WMSComplexType getInstance() {
            return instance;
        }
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
			return seq;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}        
    }
    
    protected static class _CapabilityType extends WMSComplexType {
        private static final WMSComplexType instance = new _CapabilityType();
        
        private static Element[] elems = new Element [] {
            new WMSElement("Request", _RequestType.getInstance()),
            new WMSElement("Exception", _ExceptionType.getInstance()),
            new WMSElement("_ExtendedCapabilities", __ExtendedCapabilitiesType.getInstance(), 0, Integer.MAX_VALUE),
            new WMSElement("Layer", _LayerType.getInstance(), 0, 1)
        };

        private static Sequence seq = new SequenceGT(elems);
        public static WMSComplexType getInstance() {
            return instance;
        }
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
			return seq;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}        
    }
    
    protected static class __ExtendedCapabilitiesType extends WMSComplexType {
    	private static final WMSComplexType instance = new __ExtendedCapabilitiesType();
        public static WMSComplexType getInstance() {
            return instance;
        }
        
		public boolean isAbstract() {
			return true;
		}

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
    }
    
    protected static class _RequestType extends WMSComplexType {
        private static final WMSComplexType instance = new _RequestType();
        
        private static Element[] elems = new Element [] {
            new WMSElement("GetCapabilities", OperationType.getInstance()),
            new WMSElement("GetMap", OperationType.getInstance()),
            new WMSElement("GetFeatureInfo", OperationType.getInstance()),
            new WMSElement("_ExtendedOperation", OperationType.getInstance(), 0, Integer.MAX_VALUE)
        };
        private static Sequence seq = new SequenceGT(elems);
        public static WMSComplexType getInstance() {
            return instance;
        }
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
			return seq;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}        
    }
    
    protected static class _DCPTypeType extends WMSComplexType {
        private static final WMSComplexType instance = new _DCPTypeType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("HTTP", _HTTPType.getInstance())
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}
    }
    
    protected static class _HTTPType extends WMSComplexType {
        private static final WMSComplexType instance = new _HTTPType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Get", _GetType.getInstance()),
            new WMSElement("Post", _PostType.getInstance(), 0, 1)
        };
        private static Sequence seq = new SequenceGT(elems);
        
        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}
    }
    
    protected static class _GetType extends WMSComplexType {
        private static final WMSComplexType instance = new _GetType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("OnlineResource", _OnlineResourceType.getInstance())
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}
    }
    
    protected static class _PostType extends WMSComplexType {
        private static final WMSComplexType instance = new _PostType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("OnlineResource", _OnlineResourceType.getInstance())
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}
    }
    
    protected static class _ExceptionType extends WMSComplexType {
        private static final WMSComplexType instance = new _ExceptionType();
        
        private static Element[] elems = new Element [] {
            new WMSElement("Format", XSISimpleTypes.String.getInstance(), 1, Integer.MAX_VALUE)
        };
        
        private static Sequence seq = new SequenceGT(elems);
        public static WMSComplexType getInstance() {
            return instance;
        }
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
			return seq;
		}
		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}        
    }

    protected static class _LayerType extends WMSComplexType {
        private static final WMSComplexType instance = new _LayerType();
        
        private static Element[] elems = new Element [] {
            new WMSElement("Name", XSISimpleTypes.String.getInstance(), 0, 1),
            new WMSElement("Title", XSISimpleTypes.String.getInstance()),
            new WMSElement("Abstract", XSISimpleTypes.String.getInstance(), 0, 1),
            new WMSElement("KeywordList", _KeywordListType.getInstance(), 0, 1),
            new WMSElement("CRS", XSISimpleTypes.String.getInstance(), 0, Integer.MAX_VALUE),
            new WMSElement("EX_GeographicBoundingBox", _EX_GeographicBoundingBoxType.getInstance(), 0, 1),
            new WMSElement("BoundingBox", _BoundingBoxType.getInstance(), 0, Integer.MAX_VALUE),
            new WMSElement("Dimension", _DimensionType.getInstance(), 0, Integer.MAX_VALUE),
            new WMSElement("Attribution", _AttributionType.getInstance(), 0, 1),
            new WMSElement("AuthorityURL", _AuthorityURLType.getInstance(), 0, Integer.MAX_VALUE),
            new WMSElement("Identifier", _IdentifierType.getInstance(), 0, Integer.MAX_VALUE),
            new WMSElement("MetadataURL", _MetadataURLType.getInstance(), 0, Integer.MAX_VALUE),
            new WMSElement("DataURL", _DataURLType.getInstance(), 0, Integer.MAX_VALUE),
            new WMSElement("FeatureListURL", _FeatureListURLType.getInstance(), 0, Integer.MAX_VALUE),
            new WMSElement("Style", _StyleType.getInstance(), 0, Integer.MAX_VALUE),
            new WMSElement("MinScaleDenominator", XSISimpleTypes.Double.getInstance(), 0, 1),
            new WMSElement("MaxScaleDenominator", XSISimpleTypes.Double.getInstance(), 0, 1),
            new WMSElement("Layer", _LayerType.getInstance(), 0, Integer.MAX_VALUE)
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        private static Attribute[] attrs = new Attribute[] {
            new WMSAttribute(null, "queryable", WMSSchema.NAMESPACE, XSISimpleTypes.Boolean.getInstance(), Attribute.REQUIRED, "0", null, false),
            new WMSAttribute("cascaded", XSISimpleTypes.NonNegativeInteger.getInstance()),
            new WMSAttribute(null, "opaque", WMSSchema.NAMESPACE, XSISimpleTypes.Boolean.getInstance(), Attribute.REQUIRED, "0", null, false),
            new WMSAttribute(null, "noSubSets", WMSSchema.NAMESPACE, XSISimpleTypes.Boolean.getInstance(), Attribute.REQUIRED, "0", null, false),
            new WMSAttribute("fixedWidth", XSISimpleTypes.NonNegativeInteger.getInstance()),
            new WMSAttribute("fixedHeight", XSISimpleTypes.NonNegativeInteger.getInstance())
        };
        
        public static WMSComplexType getInstance() {
            return instance;
        }

		public Attribute[] getAttributes() {
			return attrs;
		}

		public ElementGrouping getChild() {
			return seq;
		}

		public Element[] getChildElements() {
			return elems;
		}        
    }
    
    protected static class _EX_GeographicBoundingBoxType extends WMSComplexType {
        private static final WMSComplexType instance = new _EX_GeographicBoundingBoxType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("westBoundLongitude", LongitudeType.getInstance()),
            new WMSElement("eastBoundLongitude", LongitudeType.getInstance()),
            new WMSElement("southBoundLatitude", LatitudeType.getInstance()),
            new WMSElement("northBoundLatitude", LatitudeType.getInstance())
        };

        private static Sequence seq = new SequenceGT(elems);
        
        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}  
    }
    
    protected static class _BoundingBoxType extends WMSComplexType {
        private static final WMSComplexType instance = new _BoundingBoxType();

        private static Attribute[] attrs = new Attribute[] {
            new WMSAttribute(null, "CRS", WMSSchema.NAMESPACE, XSISimpleTypes.String.getInstance(), Attribute.REQUIRED, null, null, false),
            new WMSAttribute(null, "minx", WMSSchema.NAMESPACE, XSISimpleTypes.Double.getInstance(), Attribute.REQUIRED, null, null, false),
            new WMSAttribute(null, "miny", WMSSchema.NAMESPACE, XSISimpleTypes.Double.getInstance(), Attribute.REQUIRED, null, null, false),
            new WMSAttribute(null, "maxx", WMSSchema.NAMESPACE, XSISimpleTypes.Double.getInstance(), Attribute.REQUIRED, null, null, false),
            new WMSAttribute(null, "maxy", WMSSchema.NAMESPACE, XSISimpleTypes.Double.getInstance(), Attribute.REQUIRED, null, null, false),
            new WMSAttribute("resx", XSISimpleTypes.Double.getInstance()),
            new WMSAttribute("resy", XSISimpleTypes.Double.getInstance())
        };
        
        public static WMSComplexType getInstance() {
            return instance;
        }

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getAttributes()
		 */
		public Attribute[] getAttributes() {
			return attrs;
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
    }
    
    protected static class _DimensionType extends WMSComplexType {
        private static final WMSComplexType instance = new _DimensionType();
        
        private static Attribute[] attrs = new Attribute[] {
            new WMSAttribute(null, "name", WMSSchema.NAMESPACE, XSISimpleTypes.String.getInstance(), Attribute.REQUIRED, null, null, false),
            new WMSAttribute(null, "units", WMSSchema.NAMESPACE, XSISimpleTypes.String.getInstance(), Attribute.REQUIRED, null, null, false),
            new WMSAttribute("unitSymbol", XSISimpleTypes.String.getInstance()),
            new WMSAttribute("default", XSISimpleTypes.String.getInstance()),
            new WMSAttribute("multipleValues", XSISimpleTypes.Boolean.getInstance()),
            new WMSAttribute("nearestValue", XSISimpleTypes.Boolean.getInstance()),
            new WMSAttribute("current", XSISimpleTypes.Boolean.getInstance())
        };
        
        public static WMSComplexType getInstance() {
            return instance;
        }

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getAttributes()
		 */
		public Attribute[] getAttributes() {
			return attrs;
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
    }
    
    protected static class _AttributionType extends WMSComplexType {
        private static final WMSComplexType instance = new _AttributionType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Title", XSISimpleTypes.String.getInstance(), 0, 1),
            new WMSElement("OnlineResource", _OnlineResourceType.getInstance(), 0, 1),
            new WMSElement("LogoURL", _LogoURLType.getInstance(), 0, 1)
        };

        private static Sequence seq = new SequenceGT(elems);
        
        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}
    }
    
    protected static class _LogoURLType extends WMSComplexType {
        private static final WMSComplexType instance = new _LogoURLType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Format", XSISimpleTypes.String.getInstance()),
            new WMSElement("OnlineResource", _OnlineResourceType.getInstance())
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        private static Attribute[] attrs = new Attribute[] {
            new WMSAttribute("width", XSISimpleTypes.PositiveInteger.getInstance()),
            new WMSAttribute("height", XSISimpleTypes.PositiveInteger.getInstance())
        };
        
        public static WMSComplexType getInstance() {
            return instance;
        }

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getAttributes()
		 */
		public Attribute[] getAttributes() {
			return attrs;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChild()
		 */
		public ElementGrouping getChild() {
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}  
    }
    
    protected static class _MetadataURLType extends WMSComplexType {
        private static final WMSComplexType instance = new _MetadataURLType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Format", XSISimpleTypes.String.getInstance()),
            new WMSElement("OnlineResource", _OnlineResourceType.getInstance())
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        private static Attribute[] attrs = new Attribute[] {
            new WMSAttribute(null, "type", WMSSchema.NAMESPACE, XSISimpleTypes.NMTOKEN.getInstance(), Attribute.REQUIRED, null, null, false)
        };
        
        public static WMSComplexType getInstance() {
            return instance;
        }

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getAttributes()
		 */
		public Attribute[] getAttributes() {
			return attrs;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChild()
		 */
		public ElementGrouping getChild() {
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}  
    }
    
    protected static class _AuthorityURLType extends WMSComplexType {
        private static final WMSComplexType instance = new _AuthorityURLType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("OnlineResource", _OnlineResourceType.getInstance())
        };

        private static Sequence seq = new SequenceGT(elems);
        
        private static Attribute[] attrs = new Attribute[] {
            new WMSAttribute(null, "name", WMSSchema.NAMESPACE, XSISimpleTypes.NMTOKEN.getInstance(), Attribute.REQUIRED, null, null, false)
        };
        
        public static WMSComplexType getInstance() {
            return instance;
        }

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getAttributes()
		 */
		public Attribute[] getAttributes() {
			return attrs;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChild()
		 */
		public ElementGrouping getChild() {
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}  
    }
    
    protected static class _IdentifierType extends WMSComplexType {
        private static final WMSComplexType instance = new _IdentifierType();

        private static Attribute[] attrs = new Attribute[] {
            new WMSAttribute(null, "authority", WMSSchema.NAMESPACE, XSISimpleTypes.String.getInstance(), Attribute.REQUIRED, null, null, false)
        };

        public static WMSComplexType getInstance() {
            return instance;
        }

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getAttributes()
		 */
		public Attribute[] getAttributes() {
			return attrs;
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
    }
    
    protected static class _DataURLType extends WMSComplexType {
        private static final WMSComplexType instance = new _DataURLType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Format", XSISimpleTypes.String.getInstance()),
            new WMSElement("OnlineResource", _OnlineResourceType.getInstance())
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}  
    }
    
    protected static class _FeatureListURLType extends WMSComplexType {
        private static final WMSComplexType instance = new _FeatureListURLType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Format", XSISimpleTypes.String.getInstance()),
            new WMSElement("OnlineResource", _OnlineResourceType.getInstance())
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}  
    }
    
    protected static class _StyleType extends WMSComplexType {
        private static final WMSComplexType instance = new _StyleType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Name", XSISimpleTypes.String.getInstance()),
            new WMSElement("Title", XSISimpleTypes.String.getInstance()),
            new WMSElement("Abstract", XSISimpleTypes.String.getInstance(), 0, 1),
            new WMSElement("LegendURL", _LegendURLType.getInstance(), 0, Integer.MAX_VALUE),
            new WMSElement("StyleSheetURL", _StyleSheetURLType.getInstance(), 0, 1),
            new WMSElement("StyleURL", _StyleURLType.getInstance(), 0, 1)
        };
     
        private static Sequence seq = new SequenceGT(elems);
        
        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}  
    }
    
    protected static class _LegendURLType extends WMSComplexType {
        private static final WMSComplexType instance = new _LegendURLType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Format", XSISimpleTypes.String.getInstance()),
            new WMSElement("OnlineResource", _OnlineResourceType.getInstance())
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        private static Attribute[] attrs = new Attribute[] {
            new WMSAttribute("width", XSISimpleTypes.PositiveInteger.getInstance()),
            new WMSAttribute("height", XSISimpleTypes.PositiveInteger.getInstance())
        };
        
        public static WMSComplexType getInstance() {
            return instance;
        }

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getAttributes()
		 */
		public Attribute[] getAttributes() {
			return attrs;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChild()
		 */
		public ElementGrouping getChild() {
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}        
    }
    
    protected static class _StyleSheetURLType extends WMSComplexType {
        private static final WMSComplexType instance = new _StyleSheetURLType();
        
        private static Element[] elems = new Element[] {
            new WMSElement("Format", XSISimpleTypes.String.getInstance()),
            new WMSElement("OnlineResource", _OnlineResourceType.getInstance())
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}
    }
    
    protected static class _StyleURLType extends WMSComplexType {
        private static final WMSComplexType instance = new _StyleURLType();

        private static Element[] elems = new Element[] {
            new WMSElement("Format", XSISimpleTypes.String.getInstance()),
            new WMSElement("OnlineResource", _OnlineResourceType.getInstance())
        };
        
        private static Sequence seq = new SequenceGT(elems);
        
        public static WMSComplexType getInstance() {
            return instance;
        }

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
			return seq;
		}

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getChildElements()
		 */
		public Element[] getChildElements() {
			return elems;
		}
    }
    
    protected static class _OnlineResourceType extends WMSComplexType {
    	private static final WMSComplexType instance = new _OnlineResourceType();
    	
    	private static Attribute[] attrs = new Attribute[] {
    			//TODO FINISH THIS
    	};
    	
        public static WMSComplexType getInstance() {
        	return instance;
        }

		/* (non-Javadoc)
		 * @see org.geotools.xml.schema.ComplexType#getAttributes()
		 */
		public Attribute[] getAttributes() {
			return attrs;
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
    }
    
    static class LongitudeType implements SimpleType {
        private static SimpleType instance = new LongitudeType();
        
        private static Facet[] facets = new Facet[] {
            new FacetGT(Facet.MININCLUSIVE, "-180"),
            new FacetGT(Facet.MAXINCLUSIVE, "180")
        };
        
        public static SimpleType getInstance() {
            return instance;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#toAttribute(org.geotools.xml.schema.Attribute, java.lang.Object, java.util.Map)
         */
        public AttributeValue toAttribute( Attribute attribute, Object value, Map hints ) throws OperationNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#canCreateAttributes(org.geotools.xml.schema.Attribute, java.lang.Object, java.util.Map)
         */
        public boolean canCreateAttributes( Attribute attribute, Object value, Map hints ) {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#getChildType()
         */
        public int getChildType() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#getParents()
         */
        public SimpleType[] getParents() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#getFacets()
         */
        public Facet[] getFacets() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs, Map hints ) throws SAXException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public URI getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints ) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement( String name ) {
            // TODO Auto-generated method stub
            return null;
        }
    }
    
    static class LatitudeType implements SimpleType {
        private static SimpleType instance = new LatitudeType();
        
        private static Facet[] facets = new Facet[] {
            new FacetGT(Facet.MININCLUSIVE, "-90"),
            new FacetGT(Facet.MAXINCLUSIVE, "90")
        };
        
        public static SimpleType getInstance() {
            return instance;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#getFinal()
         */
        public int getFinal() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#getId()
         */
        public String getId() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#toAttribute(org.geotools.xml.schema.Attribute, java.lang.Object, java.util.Map)
         */
        public AttributeValue toAttribute( Attribute attribute, Object value, Map hints ) throws OperationNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#canCreateAttributes(org.geotools.xml.schema.Attribute, java.lang.Object, java.util.Map)
         */
        public boolean canCreateAttributes( Attribute attribute, Object value, Map hints ) {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#getChildType()
         */
        public int getChildType() {
            // TODO Auto-generated method stub
            return 0;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#getParents()
         */
        public SimpleType[] getParents() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.SimpleType#getFacets()
         */
        public Facet[] getFacets() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#getValue(org.geotools.xml.schema.Element, org.geotools.xml.schema.ElementValue[], org.xml.sax.Attributes, java.util.Map)
         */
        public Object getValue( Element element, ElementValue[] value, Attributes attrs, Map hints ) throws SAXException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#getName()
         */
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#getNamespace()
         */
        public URI getNamespace() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#getInstanceType()
         */
        public Class getInstanceType() {
            // TODO Auto-generated method stub
            return null;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#canEncode(org.geotools.xml.schema.Element, java.lang.Object, java.util.Map)
         */
        public boolean canEncode( Element element, Object value, Map hints ) {
            // TODO Auto-generated method stub
            return false;
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#encode(org.geotools.xml.schema.Element, java.lang.Object, org.geotools.xml.PrintHandler, java.util.Map)
         */
        public void encode( Element element, Object value, PrintHandler output, Map hints ) throws IOException, OperationNotSupportedException {
            // TODO Auto-generated method stub
            
        }

        /* (non-Javadoc)
         * @see org.geotools.xml.schema.Type#findChildElement(java.lang.String)
         */
        public Element findChildElement( String name ) {
            // TODO Auto-generated method stub
            return null;
        }
    }    
}
