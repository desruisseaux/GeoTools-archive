/*
 * Created on Jun 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.geotools.data.coverage.grid.file.test;

import org.geotools.catalog.AbstractMetadataEntity;

/**
 * @author jeichar
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StupidFileDataImpl extends AbstractMetadataEntity implements
        StupidFileData {

    /* (non-Javadoc)
     * @see org.geotools.metadata.StupidFileData#getName()
     */
    public String getName() {
        // TODO Auto-generated method stub
        return "Stupid";
    }

    /* (non-Javadoc)
     * @see org.geotools.metadata.StupidFileData#getLastModified()
     */
    public String getLastModified() {
        // TODO Auto-generated method stub
        return "Jun 21 2004";
    }

    /* (non-Javadoc)
     * @see org.geotools.metadata.StupidFileData#getPath()
     */
    public String getPath() {
        // TODO Auto-generated method stub
        return "Data/File/Stupid";
    }

}
