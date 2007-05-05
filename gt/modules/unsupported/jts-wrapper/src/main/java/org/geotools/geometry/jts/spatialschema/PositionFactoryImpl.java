package org.geotools.geometry.jts.spatialschema;

import java.util.List;

import org.geotools.geometry.jts.spatialschema.geometry.DirectPositionImpl;
import org.geotools.geometry.jts.spatialschema.geometry.geometry.PointArrayImpl;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.Precision;
import org.opengis.geometry.coordinate.PointArray;
import org.opengis.geometry.coordinate.Position;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class PositionFactoryImpl implements PositionFactory {
	private CoordinateReferenceSystem crs;

	public PositionFactoryImpl( CoordinateReferenceSystem crs ){
		this.crs = crs;
	}
	public DirectPosition createDirectPosition(double[] ordiantes)
			throws MismatchedDimensionException {
		return new DirectPositionImpl( crs, ordiantes );
	}

	public Position createPosition(Position position) {
		return new DirectPositionImpl( position.getPosition() );
	}

	public List createPositionList() {
		return new PointArrayImpl( crs );
	}

	public List createPositionList(double[] coordinates, int start, int end) {
		PointArray array = new PointArrayImpl( crs );
		int N = crs.getCoordinateSystem().getDimension();
		for( int i=start; i < end ; i += N ){
			double[] ords = new double[N];
			System.arraycopy( coordinates, i, ords, 0, N );
			array.add( createDirectPosition( ords ));			
		}
		return array;
	}

	public List createPositionList(float[] coordinates, int start, int end) {
		PointArray array = new PointArrayImpl( crs );
		int N = crs.getCoordinateSystem().getDimension();
		for( int i=start; i < end ; i += N ){
			double[] ords = new double[N];
			System.arraycopy( coordinates, i, ords, 0, N );
			array.add( createDirectPosition( ords ));			
		}
		return array;
	}

	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return crs;
	}

	public Precision getPrecision() {
		// TODO Auto-generated method stub
		return null;
	}

}
