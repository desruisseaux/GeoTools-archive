package org.geotools.graph.traverse.standard;

import java.util.Iterator;

import org.geotools.graph.structure.DirectedGraphable;
import org.geotools.graph.structure.Graphable;

public class DirectedDijkstraIterator extends DijkstraIterator {

	public DirectedDijkstraIterator(EdgeWeighter weighter) {
		super(weighter);
	}
	
	protected Iterator getRelated(Graphable current) {
		return(((DirectedGraphable)current).getOutRelated());
	}

	
}