/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2005, Refractions Research Inc.
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
package org.geotools.catalog.wms;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geotools.catalog.AbstractGeoResource;
import org.geotools.catalog.GeoResource;
import org.geotools.catalog.GeoResourceInfo;
import org.geotools.catalog.Resolve;
import org.geotools.catalog.ResolveChangeEvent;
import org.geotools.catalog.ResolveDelta;
import org.geotools.catalog.Service;
import org.geotools.catalog.defaults.DefaultGeoResourceInfo;
import org.geotools.catalog.defaults.DefaultResolveChangeEvent;
import org.geotools.catalog.defaults.DefaultResolveDelta;
import org.geotools.data.ows.WMSCapabilities;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.xml.WMSSchema;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.ProgressListener;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

/**
 * FeatureType provided by WFS.
 * </p>
 * 
 * @author David Zwiers, Refractions Research
 * @author Richard Gould
 * @author Justin Deoliveira, The Open Planning Project
 * @since 0.6
 * @source $URL$
 */
public class WMSGeoResource extends AbstractGeoResource {

    WMSService parent;
    org.geotools.data.ows.Layer layer;
    private GeoResourceInfo info;
    
    private WMSGeoResource() {
        // should not be used
    }

    /**
     * Construct <code>WMSGeoResourceImpl</code>.
     *
     * @param parent
     * @param layer
     */
    public WMSGeoResource( WMSService parent, org.geotools.data.ows.Layer layer ) {
        this.parent = parent;
        this.layer = layer;
    }

    public Resolve parent( ProgressListener monitor ) throws IOException {
        return parent;
    }
    /*
     * @see net.refractions.udig.catalog.IGeoResource#getStatus()
     */
    public Status getStatus() {
        return parent.getStatus();
    }

    public GeoResourceInfo getInfo( ProgressListener monitor ) throws IOException {
        if (info == null) {
            info = new WMSResourceInfo( monitor );
            ResolveDelta delta = new DefaultResolveDelta( this, ResolveDelta.Kind.CHANGED );
            parent(monitor).parent(monitor).fire(new DefaultResolveChangeEvent( this, ResolveChangeEvent.Type.POST_CHANGE, delta ) );
            
        }
        return info;
    }

    public URI getIdentifier() {
        try {
            return new URI(parent.getIdentifier().toString() + "#" + layer.getName()); //$NON-NLS-1$
        } catch (Throwable e) {
            return parent.getIdentifier();
        }
    }

    /*
     * @see net.refractions.udig.catalog.IGeoResource#resolve(java.lang.Class,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public Object resolve( Class adaptee, ProgressListener monitor ) throws IOException {
        if (adaptee == null) {
            return null;
        }
        
        if (adaptee.isAssignableFrom(Service.class)) {
            return parent;
        }
        
        if (adaptee.isAssignableFrom(GeoResource.class)) {
            return this;
        }
        
        if (adaptee.isAssignableFrom(GeoResourceInfo.class)) {
            return getInfo(monitor);
        }
        
        if (adaptee.isAssignableFrom(WebMapServer.class)) { 
            return parent.getWMS( monitor );
        }
        
        if (adaptee.isAssignableFrom(org.geotools.data.ows.Layer.class)) {
            return layer;
        }
      
        return null;
    }

   
    /*
     * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
     */
    public boolean canResolve( Class adaptee ) {
        if (adaptee == null) {
            return false;
        }

        if (adaptee.isAssignableFrom(GeoResource.class)
                || adaptee.isAssignableFrom(WebMapServer.class)
                || adaptee.isAssignableFrom(org.geotools.data.ows.Layer.class)
                || adaptee.isAssignableFrom(Service.class)) {
            return true;
        }

        return false;
    }

    /*
     * @see net.refractions.udig.catalog.IResolve#getMessage()
     */
    public Throwable getMessage() {
        return parent.getMessage();
    }

    private class WMSResourceInfo extends DefaultGeoResourceInfo {
    	
        WMSResourceInfo( ProgressListener monitor ) throws IOException {
            WebMapServer wms = parent.getWMS( monitor );
            WMSCapabilities caps = wms.getCapabilities();
            
            org.opengis.geometry.Envelope env = null;
            CoordinateReferenceSystem crs = null;
            try {
                crs = CRS.decode("EPSG:4326"); //$NON-NLS-1$
                env = parent.getWMS(null).getEnvelope(layer, crs);                
            } 
            catch (FactoryException e) {
                throw (IOException) new IOException("Bounds not available").initCause( e ); //$NON-NLS-1$
            }            
            bounds = new ReferencedEnvelope(new Envelope(env.getMinimum(0), env.getMaximum(0), 
                    env.getMinimum(1), env.getMaximum(1)), DefaultGeographicCRS.WGS84 );
            
            String parentid = parent != null && parent.getIdentifier() != null ? getIdentifier().toString() : ""; //$NON-NLS-1$
            name = layer.getName();            
            List keywordsFromWMS = new ArrayList();            
            if (caps.getService().getKeywordList() != null) {
                keywordsFromWMS.addAll( Arrays.asList( caps.getService().getKeywordList() ));
            }
                        
            if (layer.getKeywords() != null) {
                keywordsFromWMS.addAll( Arrays.asList( layer.getKeywords() ) );           
            }            
            keywordsFromWMS.add("WMS"); //$NON-NLS-1$
            keywordsFromWMS.add( layer.getName() );
            keywordsFromWMS.add( caps.getService().getName() );
            keywordsFromWMS.add( parentid );
            keywords = (String[]) keywordsFromWMS.toArray(new String[keywordsFromWMS.size()]);
                        
            if (layer.get_abstract() != null && layer.get_abstract().length() != 0) {
                description = layer.get_abstract();
            } else {
                description = caps.getService().get_abstract();
            }            
            description = caps.getService().get_abstract();
            
            if (layer.getTitle() != null && layer.getTitle().length() != 0) {
                title = layer.getTitle();
            } else {
                title = caps.getService().getTitle();
            }            
        }                
        public String getName() {
            return layer.getName();
        }
        public URI getSchema() {
            return WMSSchema.NAMESPACE;
        }
        public String getTitle() {
            return getName();
        }
    }
}
