/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.catalog;

import org.opengis.catalog.MetadataEntity;

/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface StupidFileData extends MetadataEntity {
    String getName();
    String getLastModified();
    String getPath();
}
