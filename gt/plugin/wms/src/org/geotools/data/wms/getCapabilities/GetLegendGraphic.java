/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.wms.getCapabilities;

import java.util.List;

/**
 * @author rgould
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GetLegendGraphic extends RequestType {

    /**
     * @param formats
     * @param dcpTypes
     */
    public GetLegendGraphic(List formats, List dcpTypes) {
        super(formats, dcpTypes);
        // TODO Auto-generated constructor stub
    }
    /**
     * @param initialFormat
     * @param initialDCPType
     */
    public GetLegendGraphic(String initialFormat, DCPType initialDCPType) {
        super(initialFormat, initialDCPType);
        // TODO Auto-generated constructor stub
    }
}
