package org.geotools.graph.util.geom;

public class Coordinate2D {
  public double x;
  public double y;
  
  public Coordinate2D(double x, double y) {
    this.x = x;
    this.y = y;
  }
  
  public boolean equals(Object obj) {
    if (obj instanceof Coordinate2D) {
      Coordinate2D other = (Coordinate2D)obj;
      return(x == other.x && y == other.y);
    }
    return(false);
  }
  
  public int hashCode() {
    long v = Double.doubleToLongBits(x + y);
    return((int)(v^(v>>>32)));  
  }  
}