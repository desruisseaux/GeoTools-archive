package org.geotools.data.grid;

import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage;

public interface GridCoverageStore extends GridCoverageSource{

    Object getDestination();

    void add(GridCoverage coverage) throws IOException;

    void set(String name, GridCoverage coverage) throws IOException;
}
