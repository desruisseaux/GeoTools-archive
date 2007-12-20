package org.geotools.feature.visitor;

import org.geotools.filter.FidFilter;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.opengis.filter.Id;

/**
 * Quick check to see if an ID filter is found.
 */
public class IdFinderFilterVisitor extends DefaultFilterVisitor {

    public boolean hasFIDFilter = false;
    
    @Override
    public Object visit( Id filter, Object data ) {
        hasFIDFilter = true;
        return true;
    }
}
