/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data.complex.config;

import java.util.Collection;
import java.util.Map;

import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.TypeName;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class ComplexTypeProxy extends AttributeTypeProxy implements ComplexType {

	public ComplexTypeProxy(TypeName typeName, Map registry) {
		super(typeName, registry);
	}

	public Collection associations() {
		return ((ComplexType)getSubject()).associations();
	}

	public Collection attributes() {
		return ((ComplexType)getSubject()).attributes();
	}

	public Collection getProperties() {
		return ((ComplexType) getSubject()).getProperties();
	}

	public boolean isInline() {
		return ((ComplexType)getSubject()).isInline();
	}
    
}
