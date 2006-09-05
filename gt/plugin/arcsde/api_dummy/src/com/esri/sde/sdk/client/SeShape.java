package com.esri.sde.sdk.client;

public class SeShape {
	public SeShape(){}
	public SeShape(SeCoordinateReference s) throws SeException {}
	
	public boolean isNil() { return false; }
	public double[][][] getAllCoords() { return null; }
	public void generatePoint(int i, SDEPoint[] p) throws SeException {}
	public void generateLine(int i, int j, int[] k, SDEPoint[] p) throws SeException {}
	public void generatePolygon(int i, int j, int[] k, SDEPoint[] p) throws SeException {}
	public void generateRectangle(SeExtent x) throws SeException {}
	public int getType() { return 0; }
	public Long getFeatureId() { return null; }
	public int getNumOfPoints(){return 0;}
	public void generateSimpleLine(int numPts, int numParts, int[] partOffsets, SDEPoint[] ptArray){}

}
