
package org.geotools.data.grid;

import java.util.Iterator;

import org.geotools.data.Query;

import org.geotools.coverage.grid.GridCoverage;

public interface GridCoverageSource {

    Object getSource();

    GridEntry getEntry();

    Iterator getGrids(Query query);
    
    GridCoverage getGrid(String name);
}
