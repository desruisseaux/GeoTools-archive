
package org.geotools.xml.wfs;

/**
 * <p> 
 * DOCUMENT ME!
 * </p>
 * @author dzwiers
 *
 */
public interface Request {
    public static final int GET_CAPABILITIES = 1;
    public static final int DESCRIBE_FEATURE_TYPE = 2;
    public static final int TRANSACTION = 4;
    public static final int GET_FEATURE = 8;
    public static final int GET_FEATURE_WITH_LOCK = 16;
    public static final int LOCK_FEATURE = 32;
    
    public int getRequestType();
    public boolean isGet();
    public boolean isPost();
}
