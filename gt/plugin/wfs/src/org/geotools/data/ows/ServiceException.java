
package org.geotools.data.ows;

import org.xml.sax.SAXException;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public class ServiceException extends SAXException {
    private int code = 0;
    public int getCode(){
        return code;
    }
    private String locator = null;
    public String getLocator(){
        return locator;
    }
    private ServiceException(){}
    public ServiceException(String msg){
        super(msg);
    }
    public ServiceException(String msg, int code, String locator){
        super(msg);
        this.code = code;this.locator = locator;
    }
}
