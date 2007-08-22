/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gpx.binding;

import org.picocontainer.MutablePicoContainer;
import org.geotools.xml.BindingConfiguration;


/**
 * Binding configuration for the http://www.topografix.com/GPX/1/1 schema.
 *
 * @generated
 */
public final class GPXBindingConfiguration implements BindingConfiguration {
    /**
     * @generated modifiable
     */
    public void configure(MutablePicoContainer container) {
        //Types
        container.registerComponentImplementation(GPX.boundsType, BoundsTypeBinding.class);
        container.registerComponentImplementation(GPX.copyrightType, CopyrightTypeBinding.class);
        container.registerComponentImplementation(GPX.degreesType, DegreesTypeBinding.class);
        container.registerComponentImplementation(GPX.dgpsStationType, DgpsStationTypeBinding.class);
        container.registerComponentImplementation(GPX.emailType, EmailTypeBinding.class);
        container.registerComponentImplementation(GPX.extensionsType, ExtensionsTypeBinding.class);
        container.registerComponentImplementation(GPX.fixType, FixTypeBinding.class);
        container.registerComponentImplementation(GPX.gpxType, GpxTypeBinding.class);
        container.registerComponentImplementation(GPX.latitudeType, LatitudeTypeBinding.class);
        container.registerComponentImplementation(GPX.linkType, LinkTypeBinding.class);
        container.registerComponentImplementation(GPX.longitudeType, LongitudeTypeBinding.class);
        container.registerComponentImplementation(GPX.metadataType, MetadataTypeBinding.class);
        container.registerComponentImplementation(GPX.personType, PersonTypeBinding.class);
        container.registerComponentImplementation(GPX.ptsegType, PtsegTypeBinding.class);
        container.registerComponentImplementation(GPX.ptType, PtTypeBinding.class);
        container.registerComponentImplementation(GPX.rteType, RteTypeBinding.class);
        container.registerComponentImplementation(GPX.trksegType, TrksegTypeBinding.class);
        container.registerComponentImplementation(GPX.trkType, TrkTypeBinding.class);
        container.registerComponentImplementation(GPX.wptType, WptTypeBinding.class);
    }
}
