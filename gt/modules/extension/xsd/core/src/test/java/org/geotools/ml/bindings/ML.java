package org.geotools.ml.bindings;

import javax.xml.namespace.QName;

import org.geotools.xml.XSD;

/**
 * This interface contains the qualified names of all the types in the 
 * http://mails/refractions/net schema.
 *
 * @generated
 */public class ML extends XSD {

     /**
      * singleton instance
      */
    private static ML instance = new ML();
    /**
     * the singleton instance.
     */
    public static ML getInstance() {
        return instance;
    }
    
    /**
     * private constructor.
     */
    private ML() {}
    
    /**
     * Returns 'http://mails/refractions/net'.
     */
    public String getNamespaceURI() {
        return NAMESPACE;
    }
     
    /**
     * Returns the location of 'mails.xsd'.
     */
    public String getSchemaLocation() {
        return getClass().getResource("mails.xsd").toString();
    }
     
     
	public static final String NAMESPACE = "http://mails/refractions/net";
	
	public static final QName ATTACHMENTTYPE = 
		new QName("http://mails/refractions/net","attachmentType");
	public static final QName BODYTYPE = 
		new QName("http://mails/refractions/net","bodyType");
	public static final QName ENVELOPETYPE = 
		new QName("http://mails/refractions/net","envelopeType");
	public static final QName MAILSTYPE = 
		new QName("http://mails/refractions/net","mailsType");
	public static final QName MAILTYPE = 
		new QName("http://mails/refractions/net","mailType");
	public static final QName MIMETOPLEVELTYPE = 
		new QName("http://mails/refractions/net","mimeTopLevelType");
}
	