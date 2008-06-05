/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.resources.jaxb.code;

import javax.xml.bind.annotation.XmlElement;
import org.opengis.metadata.constraint.Restriction;


/**
 * JAXB adapter for {@link Restriction}, in order to integrate the value in a tags
 * respecting the ISO-19139 standard. See package documentation to have more information
 * about the handling of CodeList in ISO-19139.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class RestrictionAdapter
        extends CodeListAdapter<RestrictionAdapter, Restriction>
{
    /**
     * Ensures that the adapted code list class is loaded.
     */
    static {
        ensureClassLoaded(Restriction.class);
    }

    /**
     * Empty constructor for JAXB only.
     */
    private RestrictionAdapter() {
    }

    public RestrictionAdapter(final CodeListProxy proxy) {
        super(proxy);
    }

    protected RestrictionAdapter wrap(CodeListProxy proxy) {
        return new RestrictionAdapter(proxy);
    }

    protected Class<Restriction> getCodeListClass() {
        return Restriction.class;
    }

    @XmlElement(name = "MD_RestrictionCode")
    public CodeListProxy getCodeListProxy() {
        return proxy;
    }

    public void setCodeListProxy(final CodeListProxy proxy) {
        this.proxy = proxy;
    }
}
