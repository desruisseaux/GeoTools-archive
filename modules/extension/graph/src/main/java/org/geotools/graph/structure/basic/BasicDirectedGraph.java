/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2002, Refractions Reserach Inc.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.graph.structure.basic;

import java.util.Collection;

import org.geotools.graph.structure.DirectedGraph;

/**
 * Basic implementation of DirectedGraph. 
 * 
 * @author Justin Deoliveira, Refractions Research Inc, jdeolive@refractions.net
 *
 * @source $URL$
 */
public class BasicDirectedGraph extends BasicGraph implements DirectedGraph {

  /**
   * Creates a directed graph from a collection of directed nodes and a 
   * collection of directed edges.
   * The relationships between the nodes (edges) are already assumed to be 
   * formed. Only the references to the node and edge collections are copied,
   * not the underlying collections themselves.
   * 
   * @param nodes Collection of DirectedNode objects contained by the graph.
   * @param edges Collection of DirectedEdge objects contained by the graph.
   */
  public BasicDirectedGraph(Collection nodes, Collection edges) {
    super(nodes, edges);
  }
  
}
