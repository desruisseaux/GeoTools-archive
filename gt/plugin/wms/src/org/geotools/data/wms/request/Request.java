/*
 * Created on 10-Nov-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.request;

import java.net.URL;
import java.util.Properties;

/**
 * @author Richard Gould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface Request {
    public URL getFinalURL();
    
    public void setProperty(String name, String value);
    
    public Properties getProperties();
}
