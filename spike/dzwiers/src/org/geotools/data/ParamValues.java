/*
 * Created on 14-Oct-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data;

import java.util.Iterator;

import org.opengis.catalog.MetadataEntity;

/**
 * @author dzwiers
 */
public interface ParamValues {
    // MetadataEntity
    Iterator getParamDescriptors();
    
    Object getParamValue(MetadataEntity mde);
}
