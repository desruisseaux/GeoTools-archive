package org.geotools.graph.build.line;

import org.geotools.graph.build.basic.BasicDirectedGraphBuilder;


public class DirectedLineStringGraphGenerator extends LineStringGraphGenerator {

  public DirectedLineStringGraphGenerator() {
    super();
    setGraphBuilder(new BasicDirectedGraphBuilder());
  }
	
  	
}