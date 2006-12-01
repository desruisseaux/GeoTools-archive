package org.geotools.data.grid;

import java.util.Map;

import org.opengis.coverage.grid.Format;
import org.opengis.feature.type.TypeName;

public interface GridCoverageDescription {
    TypeName getName();
    
    Format format();
    
    /** Map<MetaDataKey,MetaDataValue> describing associated GridCoverage */    
    Map/*<Key,Value*/ metadata();
    
    // add more stuff here, basically preparse your header
}
