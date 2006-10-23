/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
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
