package org.geotools.data.wms.xml;

import java.net.URI;

import org.geotools.xml.SchemaFactory;
import org.geotools.xml.XSIElementHandler;
import org.geotools.xml.XSISAXHandler;
import org.geotools.xml.handlers.xsi.RootHandler;
import org.geotools.xml.schema.Schema;
import org.xml.sax.SAXException;

public class WMSSchemaFactory extends SchemaFactory {

	protected WMSSchemaFactory () {
		is = this;
	}
	
	protected static class WmsXsiSaxHandler extends XSISAXHandler {

		public WmsXsiSaxHandler(URI uri) {
			super(uri);
			rootHandler = new WMSRootHandler(uri);
		}
	}
	
	protected static class WMSRootHandler extends RootHandler {

//		private ServiceExceptionReportHandler se;

		public WMSRootHandler(URI uri) {
			super(uri);
		}
		
		public XSIElementHandler getHandler(String namespaceURI,
				String localName) throws SAXException {
			XSIElementHandler r = null;
			r = super.getHandler(namespaceURI, localName);

            if (r != null) {
                return r;
            }

//            if ("ServiceExceptionReport".equalsIgnoreCase(localName)
//                    && FilterSchema.NAMESPACE.toString().equalsIgnoreCase(namespaceURI)) {
//                //                FilterSchema.getInstance().getElements()[37]
//                //                ServiceException
//                if (se == null) {
//                    se = new ServiceExceptionReportHandler();
//                }
//
//                return se;
//            }

            return null;
		}

		public Schema getSchema() throws SAXException {
//            if (se != null) {
//                if (se.getException() != null) {
//                    throw se.getException();
//                }
//            }
            
			return super.getSchema();
		}
	}
	
//	private static class ServiceExceptionReportHandler extends XSIElementHandler {
//        private ServiceException exception;
//        private boolean inside = false;
//
//        /**
//         * @see org.geotools.xml.XSIElementHandler#getHandlerType()
//         */
//        public int getHandlerType() {
//            return DEFAULT;
//        }
//
//        protected ServiceException getException() {
//            return exception;
//        }
//
//        /**
//         * @see org.geotools.xml.XSIElementHandler#endElement(java.lang.String,
//         *      java.lang.String)
//         */
//        public void endElement(String namespaceURI, String localName){
//            inside = false;
//        }
//
//        /**
//         * @see org.geotools.xml.XSIElementHandler#startElement(java.lang.String,
//         *      java.lang.String, org.xml.sax.Attributes)
//         */
//        public void startElement(String namespaceURI, String localName,
//            Attributes attr){
//            if ("ServiceException".equalsIgnoreCase(localName)) {
//                inside = true;
//
//                if (attr != null) {
//                    String locator = attr.getValue("", "locator");
//
//                    if (locator == null) {
//                        locator = attr.getValue(namespaceURI, "locator");
//                    }
//
//                    String code = attr.getValue("", "code");
//
//                    if (code == null) {
//                        code = attr.getValue(namespaceURI, "code");
//                    }
//
//                    exception = new ServiceException("", code, locator);
//                }
//            }
//        }
//
//        /**
//         * @see org.geotools.xml.XSIElementHandler#characters(java.lang.String)
//         */
//        public void characters(String text){
//            if (inside) {
//                exception = new ServiceException(text, exception.getCode(),
//                        exception.getLocator());
//            }
//        }
//
//        /**
//         * @see org.geotools.xml.XSIElementHandler#getHandler(java.lang.String,
//         *      java.lang.String)
//         */
//        public XSIElementHandler getHandler(String namespaceURI,
//            String localName){
//            if ("ServiceException".equalsIgnoreCase(localName)
//                    && FilterSchema.NAMESPACE.toString().equalsIgnoreCase(namespaceURI)) {
//                return this;
//            }
//
//            return null;
//        }
//
//        /**
//         * @see org.geotools.xml.XSIElementHandler#getLocalName()
//         */
//        public String getLocalName() {
//            // TODO Auto-generated method stub
//            return null;
//        }
//
//        /**
//         * @see org.geotools.xml.XSIElementHandler#hashCode()
//         */
//        public int hashCode() {
//            // TODO Auto-generated method stub
//            return 0;
//        }
//    }
}
