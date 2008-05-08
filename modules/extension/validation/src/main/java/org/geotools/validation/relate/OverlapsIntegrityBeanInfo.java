/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2004 TOPP - www.openplans.org
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.validation.relate;

import java.beans.PropertyDescriptor;


/**
 * @author bowens
 *
 * @source $URL$
 */
public class OverlapsIntegrityBeanInfo extends RelationIntegrityBeanInfo 
{
	
	
	/**
	 * 
	 */
	public OverlapsIntegrityBeanInfo(){
        super();
    }

	
    /**
     * Implementation of getPropertyDescriptors.
     *
     *
     * @see java.beans.BeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return super.getPropertyDescriptors();
    }
}
