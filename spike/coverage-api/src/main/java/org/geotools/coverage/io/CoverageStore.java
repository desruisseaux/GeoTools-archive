/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.coverage.io;

import org.opengis.util.ProgressListener;

/**
 * Provided write access to a coverage data product.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @author Jody Garnett
 * @todo revisit and improve when feedback starts to flow in
 */
public interface CoverageStore extends CoverageSource {

	public CoverageResponse updateCoverage(CoverageUpdateRequest writeRequest, ProgressListener progress);


}
