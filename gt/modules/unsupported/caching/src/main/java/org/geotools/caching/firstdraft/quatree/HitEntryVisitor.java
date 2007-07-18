package org.geotools.caching.firstdraft.quadtree;

import org.geotools.caching.firstdraft.spatialindex.spatialindex.IData;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.INode;
import org.geotools.caching.firstdraft.spatialindex.spatialindex.IVisitor;

public class HitEntryVisitor implements IVisitor {

	public void visitData(IData d) {
		// do nothing

	}

	public void visitNode(INode n) {
		if (n instanceof Node) {
			Node node = (Node) n ;
			node.hit() ;
		}
	}

}
