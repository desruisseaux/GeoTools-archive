package org.geotools.graph.build.line;

import org.geotools.graph.build.GraphGenerator;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;

import com.vividsolutions.jts.geom.Coordinate;

//TODO: COMMENT ME!

public interface LineGraphGenerator extends GraphGenerator {
 
  public Node getNode(Coordinate c);
  
  public Edge getEdge(Coordinate c1, Coordinate c2); 
  
}