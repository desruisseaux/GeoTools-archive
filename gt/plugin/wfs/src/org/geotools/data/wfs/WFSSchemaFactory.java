
package org.geotools.data.wfs;

import java.net.URI;

import org.geotools.data.ows.ServiceException;
import org.geotools.xml.SchemaFactory;
import org.geotools.xml.XSIElementHandler;
import org.geotools.xml.XSISAXHandler;
import org.geotools.xml.handlers.xsi.RootHandler;
import org.geotools.xml.ogc.FilterSchema;
import org.geotools.xml.schema.Schema;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class WFSSchemaFactory extends SchemaFactory {
        
    // HACK for setting up the static field to use.
    protected WFSSchemaFactory(){
        is = this;
    }
    
    protected XSISAXHandler getSAXHandler(URI uri){
        return new WFSXSISAXHandler(uri);
    }
    
    protected static class WFSXSISAXHandler extends XSISAXHandler{
        public WFSXSISAXHandler(URI uri) {
            super(uri);
            rootHandler = new WFSRootHandler(uri);
        }
    }
    
    protected static class WFSRootHandler extends RootHandler{

        public WFSRootHandler(URI uri) {
            super(uri);
        }
        public XSIElementHandler getHandler(String namespaceURI, String localName)
            throws SAXException {
            XSIElementHandler r = null;
            r = super.getHandler(namespaceURI,localName);
            if(r!=null)
                return r;
            if ( "ServiceExceptionReport".equalsIgnoreCase(localName)
                    && FilterSchema.NAMESPACE.toString().equalsIgnoreCase(namespaceURI)) {
//                FilterSchema.getInstance().getElements()[37]
//                ServiceException
                if (se == null) {
                    se = new ServiceExceptionReportHandler();
                }

                return se;
            }
            return null;
        }

        private ServiceExceptionReportHandler se = null;
        public Schema getSchema() throws SAXException {
            if(se!=null){
                if(se.getException()!=null)
                    throw se.getException();
            }
            return super.getSchema();
        }
    }
    
    private static class ServiceExceptionReportHandler extends XSIElementHandler{

        /**
         * @see org.geotools.xml.XSIElementHandler#getHandlerType()
         */
        public int getHandlerType() {
            return DEFAULT;
        }
        
        protected ServiceException getException(){
            return exception;
        }

        /**
         * @see org.geotools.xml.XSIElementHandler#endElement(java.lang.String, java.lang.String)
         */
        public void endElement(String namespaceURI, String localName) throws SAXException {
            inside = false;   
        }

        /**
         * @see org.geotools.xml.XSIElementHandler#startElement(java.lang.String, java.lang.String, org.xml.sax.Attributes)
         */
        public void startElement(String namespaceURI, String localName, Attributes attr) throws SAXException {
            if("ServiceException".equalsIgnoreCase(localName)){
                inside = true;
                if(attr!=null){
                String locator = attr.getValue("","locator");
                if(locator == null)
                    locator = attr.getValue(namespaceURI,"locator");
                
                String code = attr.getValue("","code");
                if(code == null)
                    code = attr.getValue(namespaceURI,"code");
                exception = new ServiceException("",code,locator);
                }
            }
        }

        /**
         * @see org.geotools.xml.XSIElementHandler#characters(java.lang.String)
         */
        public void characters(String text) throws SAXException {
            if(inside){
                exception = new ServiceException(text,exception.getCode(),exception.getLocator());
            }
        }
        private ServiceException exception;
        private boolean inside = false;
        /**
         * @see org.geotools.xml.XSIElementHandler#getHandler(java.lang.String, java.lang.String)
         */
        public XSIElementHandler getHandler(String namespaceURI, String localName) throws SAXException {

            if ( "ServiceException".equalsIgnoreCase(localName)
                    && FilterSchema.NAMESPACE.toString().equalsIgnoreCase(namespaceURI)) {
                return this;
            }
            return null;
        }

        /**
         * @see org.geotools.xml.XSIElementHandler#getLocalName()
         */
        public String getLocalName() {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * @see org.geotools.xml.XSIElementHandler#hashCode()
         */
        public int hashCode() {
            // TODO Auto-generated method stub
            return 0;
        }
        
    }
}
