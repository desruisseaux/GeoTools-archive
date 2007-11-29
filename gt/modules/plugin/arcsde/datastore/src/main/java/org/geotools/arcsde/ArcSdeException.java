package org.geotools.arcsde;

import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeError;
import com.esri.sde.sdk.client.SeException;

/**
 * An IOException that wraps an {@link SeException} in order to report the
 * {@link SeError} messages that otherwise get hidden in a normal stack trace.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5
 * @URL $URL:
 *      http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/ArcSdeException.java $
 */
public class ArcSdeException extends DataSourceException {

    private static final long serialVersionUID = -1392514883217797825L;

    public ArcSdeException(SeException cause) {
        this("", cause);
    }

    public ArcSdeException(String msg, SeException cause) {
        super(msg, cause);
    }

    @Override
    public SeException getCause() {
        return (SeException) super.getCause();
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        SeError error = getSeError();
        StringBuffer sb = new StringBuffer();
        if (message != null) {
            sb.append(message);
        }
        if (error != null) {
            String sdeErrMsg = error.getSdeErrMsg();
            String extErrMsg = error.getExtErrMsg();
            String errDesc = error.getErrDesc();

            sb.append("[SDE error ").append(error.getSdeError());
            if (sdeErrMsg != null && !"".equals(sdeErrMsg)) {
                sb.append(" ").append(sdeErrMsg);
            }
            sb.append("]");
            if (errDesc != null && !"".equals(errDesc)) {
                sb.append("[Error desc=").append(errDesc).append("]");
            }
            if (extErrMsg != null && !"".equals(extErrMsg)) {
                sb.append("[Extended desc=").append(extErrMsg).append("]");
            }
        }
        return sb.toString();
    }

    public SeError getSeError() {
        SeException ex = getCause();
        if (ex == null) {
            return null;
        }
        return ex.getSeError();
    }
}
