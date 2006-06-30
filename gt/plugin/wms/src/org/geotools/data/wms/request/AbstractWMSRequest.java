package org.geotools.data.wms.request;

import java.net.URL;
import java.util.Properties;

import org.geotools.data.ows.AbstractRequest;

public abstract class AbstractWMSRequest extends AbstractRequest {
    public AbstractWMSRequest(URL onlineResource, Properties properties) {
		super(onlineResource, properties);
	}

	protected void initService() {
		setProperty(SERVICE, "WMS");
	}
}
