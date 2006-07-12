package org.geotools.catalog.adaptable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.geotools.catalog.Service;
import org.geotools.catalog.ServiceInfo;
import org.geotools.util.ProgressListener;

public class AdaptingService extends AdaptingResolve implements Service {

	AdaptingService( Service service, ResolveAdapterFactoryFinder finder ) {
		super( service, finder );
	}

	public Map getConnectionParams() {
		return service().getConnectionParams();
	}

	public ServiceInfo getInfo(ProgressListener monitor) throws IOException {
		return service().getInfo( monitor );
	}

	protected Service service() {
		return (Service) resolve;
	}
}
