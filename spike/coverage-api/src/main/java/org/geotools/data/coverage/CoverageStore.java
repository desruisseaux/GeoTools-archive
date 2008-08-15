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
package org.geotools.data.coverage;

import org.opengis.util.ProgressListener;

/**
 * Provided write access to a coverage data product.
 * 
 * @author Simone Giannecchini, GeoSolusions
 * @author Jody Garnett
 */
public interface CoverageStore extends CoverageSource {
	public CoverageResponse addCoverage(CoverageWriteRequest writeRequest, ProgressListener progress);

	public CoverageResponse modifyCoverage(CoverageWriteRequest writeRequest, ProgressListener progress);

	public CoverageResponse removeCoverage(CoverageWriteRequest writeRequest, ProgressListener progress);
}
