/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.renderer.shape;

import java.util.Iterator;
import java.util.Stack;

import org.geotools.filter.Filter;
import org.geotools.filter.FilterType;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Visits a filter and extracts the minimum bounds that the filter requires.
 * 
 * @author jones
 */
public class BoundsExtractor extends AbstractProcessFilterGeometiresVisitor {
    private Stack/*<Envelope>*/ envelopeStack=new Stack/*<Envelope>*/();

    private final Envelope original;

    private Envelope notEnvelope;

    private final static GeometryFactory factory=new GeometryFactory();

    public BoundsExtractor(Envelope bbox) {
        original=bbox;
    }

    public BoundsExtractor(int minx, int maxx, int miny, int maxy) {
        this(new Envelope(minx, maxx, miny, maxy));
    }

    /**
     *  @return the intersecton of the new bbox and the original
     */ 
    public Envelope getIntersection() {
        Envelope bbox=null;
        if( !envelopeStack.isEmpty())
            bbox=(Envelope) envelopeStack.peek();
        if( original==null ){
            return bbox==null?new Envelope():bbox;
        }
        if( bbox!=null )
            return bbox.intersection(original);
        if( notEnvelope!=null ){
            return intersectionWithNotEnvelope(original);
        }
        return original;
    }

    /**
     *  @return the intersecton of the new bbox and the original
     */ 
    public Envelope getFilterEnvelope() {
        if( envelopeStack.isEmpty() )
            return new Envelope();
        return (Envelope) envelopeStack.peek();
    }
    
    public Envelope getNotEnvelope() {
        return notEnvelope==null?new Envelope():notEnvelope;
    }
    /*
     * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LogicFilter)
     */
    public void visit(LogicFilter filter) {

        if (filter != null) {
            switch (filter.getFilterType()) {
            case FilterType.LOGIC_OR: {
                Iterator i = filter.getFilterIterator();
                while (i.hasNext()) {
                    Filter tmp = (Filter) i.next();
                    tmp.accept(this);
                }
                Envelope bbox=new Envelope();
                while( !envelopeStack.isEmpty() ){
                    Envelope env = (Envelope) envelopeStack.pop();
                    bbox.expandToInclude(env);
                }
                if( notEnvelope!=null ){
                    if( bbox.contains(notEnvelope) ){
                        // or contains all of notEnvelope so notEnvelope is meaningless
                        notEnvelope=null;
                    }else{
                        // lets err on the side of caution and we can safely ignore the or... This will
                        // be a little big but that's ok.
                        bbox=new Envelope();
                    }
                }

                if( !bbox.isNull() )
                    envelopeStack.push(bbox);
                break;
            }
            case FilterType.LOGIC_AND: {
                Iterator i = filter.getFilterIterator();
                while (i.hasNext()) {
                    Filter tmp = (Filter) i.next();
                    tmp.accept(this);
                }
                if( !envelopeStack.isEmpty() ){
                    Envelope bbox = null;
                    while( !envelopeStack.isEmpty() ){
                        Envelope env = (Envelope) envelopeStack.pop();
                        if( bbox==null ){
                            bbox=env;
                        }else{
                            bbox=bbox.intersection(env);
                        }
                    }
                    if( notEnvelope!=null && bbox!=null){
                        if( notEnvelope.contains(bbox) ){
                            // this mean that nothing valid since we are ANDING
                            // and area with an area that is guaranteed to be empty
                            // Erring on the side of caution for now
                            notEnvelope=bbox;
                            bbox=null;
                        }else{
                            bbox = intersectionWithNotEnvelope(bbox);
                            notEnvelope=null;
                        }
                    }
                    if( bbox!=null && !bbox.isNull() )
                        envelopeStack.push(bbox);
                }
                
                break;
            }
            case FilterType.LOGIC_NOT:
                Iterator i = filter.getFilterIterator();
                Filter tmp = (Filter) i.next();
                tmp.accept(this);
                if( !envelopeStack.isEmpty() ){
                    notEnvelope=(Envelope) envelopeStack.pop();
                    assert envelopeStack.isEmpty();
                }else if( notEnvelope!=null || !notEnvelope.isNull()){
                    envelopeStack.push(notEnvelope);
                    notEnvelope=null;
                }
            default:
                break;
            }

        }
    }

    private Envelope intersectionWithNotEnvelope(Envelope bbox) {
        Geometry notGeom = factory.toGeometry(notEnvelope);
        Geometry andGeom = factory.toGeometry(bbox);
        
        Envelope envelopeInternal = andGeom.difference(notGeom).getEnvelopeInternal();
        bbox = envelopeInternal;
        return bbox;
    }
    
    public void visit(LiteralExpression expression) {
        Object literal = expression.getLiteral();
        if (literal instanceof Geometry) {
            Geometry geom = (Geometry) literal;
            envelopeStack.push(geom.getEnvelopeInternal());
        }
    }

//  /*
//   * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.BetweenFilter)
//   */
//  public void visit(BetweenFilter filter) {
//      if (filter != null) {
//          if (filter.getLeftValue() != null)
//              filter.getLeftValue().accept(this);
//          if (filter.getRightValue() != null)
//              filter.getRightValue().accept(this);
//          if (filter.getMiddleValue() != null)
//              filter.getMiddleValue().accept(this);
//      }
//  }
//
//  /*
//   * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.CompareFilter)
//   */
//  public void visit(CompareFilter filter) {
//      if (filter != null) {
//          if (filter.getLeftValue() != null)
//              filter.getLeftValue().accept(this);
//          if (filter.getRightValue() != null)
//              filter.getRightValue().accept(this);
//      }
//  }
//
//  /*
//   * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.GeometryFilter)
//   */
//  public void visit(GeometryFilter filter) {
//      if (filter != null) {
//
//          LiteralExpression le = null;
//          Envelope bbox = null;
//          if (filter.getLeftGeometry().getType() == ExpressionType.LITERAL_GEOMETRY) {
//              le = (LiteralExpression) filter.getLeftGeometry();
//              if (le != null && le.getLiteral() != null
//                      && le.getLiteral() instanceof Geometry) {
//                  bbox = ((Geometry) le.getLiteral()).getEnvelopeInternal();
//              }
//          } else {
//              if (filter.getRightGeometry().getType() == ExpressionType.LITERAL_GEOMETRY) {
//                  le = (LiteralExpression) filter.getRightGeometry();
//                  if (le != null && le.getLiteral() != null
//                          && le.getLiteral() instanceof Geometry) {
//                      Geometry g = (Geometry) le.getLiteral();
//                      bbox = g.getEnvelopeInternal();
//                  }
//              }
//          }
//
//          if (bbox != null) {
//              switch (logicType) {
//              case Filter.LOGIC_AND:
//                  and(bbox, filter.getFilterType());
//                  break;
//
//              case Filter.LOGIC_OR:
//                  or(bbox, filter.getFilterType());
//                  break;
//
//              default:
//                  break;
//              }
//          }
//
//      }
//  }
//
//  private void or(Envelope bbox, short s) {
//      switch (s) {
//
//      case FilterType.GEOMETRY_BBOX:
//      case FilterType.GEOMETRY_CONTAINS:
//      case FilterType.GEOMETRY_CROSSES:
//      case FilterType.GEOMETRY_DWITHIN:
//      case FilterType.GEOMETRY_EQUALS:
//      case FilterType.GEOMETRY_INTERSECTS:
//      case FilterType.GEOMETRY_OVERLAPS:
//      case FilterType.GEOMETRY_TOUCHES:
//      case FilterType.GEOMETRY_WITHIN:
//          if (!bbox.intersects(clippedbbox)) {
//              if (clippedbbox == null || clippedbbox.isNull())
//                  clippedbbox = bbox;
//              else
//                  clippedbbox.expandToInclude(bbox);
//          } else {
//              boolean changed = false;
//              double minx, miny, maxx, maxy;
//              minx = clippedbbox.getMinX();
//              miny = clippedbbox.getMinY();
//              maxx = clippedbbox.getMaxX();
//              maxy = clippedbbox.getMaxY();
//              if (minx > bbox.getMinX()) {
//                  minx = bbox.getMinX();
//                  changed = true;
//              }
//              if (maxx < bbox.getMaxX()) {
//                  maxx = bbox.getMaxX();
//                  changed = true;
//              }
//              if (miny > bbox.getMinY()) {
//                  miny = bbox.getMinY();
//                  changed = true;
//              }
//              if (maxy < bbox.getMaxY()) {
//                  maxy = bbox.getMaxY();
//                  changed = true;
//              }
//              if (changed) {
//                  clippedbbox = new Envelope(minx, maxx, miny, maxy);
//              }
//          }
//          return;
//      case FilterType.GEOMETRY_BEYOND:
//      case FilterType.GEOMETRY_DISJOINT:
//          return;
//      }
//  }
//
//  private void and(Envelope bbox, short s) {
//      switch (s) {
//
//      case FilterType.GEOMETRY_BBOX:
//      case FilterType.GEOMETRY_CONTAINS:
//      case FilterType.GEOMETRY_CROSSES:
//      case FilterType.GEOMETRY_DWITHIN:
//      case FilterType.GEOMETRY_EQUALS:
//      case FilterType.GEOMETRY_INTERSECTS:
//      case FilterType.GEOMETRY_OVERLAPS:
//      case FilterType.GEOMETRY_TOUCHES:
//      case FilterType.GEOMETRY_WITHIN:
//          if (clippedbbox==null ) {
//              clippedbbox = new Envelope(bbox.getMinX(), bbox
//                      .getMaxX(), bbox.getMinY(), bbox
//                      .getMaxY());
//          } else if( !bbox.intersects(clippedbbox) ){
//              clippedbbox=new Envelope();
//          }else{
//              clippedbbox=clippedbbox.intersection(bbox);
//          }
//          return;
//      case FilterType.GEOMETRY_BEYOND:
//      case FilterType.GEOMETRY_DISJOINT:
//          return;
//      }
//  }
//
//  /*
//   * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LikeFilter)
//   */
//  public void visit(LikeFilter filter) {
//      if (filter != null) {
//          if (filter.getValue() != null)
//              filter.getValue().accept(this);
//      }
//  }
//
//  /*
//   * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LogicFilter)
//   */
//  public void visit(LogicFilter filter) {
//
//      short oldType = logicType;
//      if (filter != null) {
//          switch (filter.getFilterType()) {
//          case Filter.LOGIC_OR: {
//              Envelope original = clippedbbox;
//              clippedbbox = new Envelope();
//              logicType = logicType == Filter.LOGIC_NOT ? logicType
//                      : Filter.LOGIC_OR;
//              Iterator i = filter.getFilterIterator();
//              while (i.hasNext()) {
//                  Filter tmp = (Filter) i.next();
//                  tmp.accept(this);
//              }
//              if (logicType != Filter.LOGIC_NOT) {
//                  logicType = Filter.LOGIC_AND;
//
//                  Envelope newBbox = clippedbbox;
//                  clippedbbox = original;
//
//                  and(newBbox, Filter.GEOMETRY_INTERSECTS);
//              }
//              break;
//          }
//          case Filter.LOGIC_AND: {
//              Iterator i = filter.getFilterIterator();
//              logicType = logicType == Filter.LOGIC_NOT ? logicType
//                      : Filter.LOGIC_AND;
//              while (i.hasNext()) {
//                  Filter tmp = (Filter) i.next();
//                  tmp.accept(this);
//              }
//
//              break;
//          }
//          case Filter.LOGIC_NOT:
//              Iterator i = filter.getFilterIterator();
//              logicType = logicType == Filter.LOGIC_NOT ? Filter.LOGIC_AND
//                      : Filter.LOGIC_NOT;
//              while (i.hasNext()) {
//                  Filter tmp = (Filter) i.next();
//                  tmp.accept(this);
//              }
//          default:
//              break;
//          }
//          logicType = oldType;
//
//      }
//  }
//
//  /*
//   * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.NullFilter)
//   */
//  public void visit(NullFilter filter) {
//      if (filter != null) {
//          if (filter.getNullCheckValue() != null)
//              filter.getNullCheckValue().accept(this);
//      }
//  }
//
//  /*
//   * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FidFilter)
//   */
//  public void visit(FidFilter filter) {
//      // do nothing
//  }
//
//  /*
//   * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.AttributeExpression)
//   */
//  public void visit(AttributeExpression expression) {
//      // do nothing
//  }
//
//  /*
//   * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.Expression)
//   */
//  public void visit(Expression expression) {
//      // do nothing
//  }
//
//  /*
//   * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.LiteralExpression)
//   */
//  public void visit(LiteralExpression expression) {
//      // do nothing
//  }
//
//  /*
//   * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.MathExpression)
//   */
//  public void visit(MathExpression expression) {
//      // do nothing
//  }
//
//  /*
//   * @see org.geotools.filter.FilterVisitor#visit(org.geotools.filter.FunctionExpression)
//   */
//  public void visit(FunctionExpression expression) {
//      // do nothing
//  }
//  
}
