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
	
	public PointArray createPointArray(float[] array, int start, int end) {
		PointArray pointArray = (PointArray) createPointArray();
		int D = crs.getCoordinateSystem().getDimension();
		if (D == 2) {
			for (int i = start; i < end; i += D) {
				double[] ordinates = new double[] { array[i], array[i + 1] };
				pointArray.add(new DirectPositionImpl(crs, ordinates));
			}
		} else if (D == 3) {
			for (int i = start; i < end; i += D) {
				double[] ordinates = new double[] { array[i], array[i + 1],
						array[i + 2] };
				pointArray.add(new DirectPositionImpl(crs, ordinates));
			}
		} else {
			for (int i = start; i < end; i += D) {
				double[] ordinates = new double[D];
				for (int o = 0; i < D; i++) {
					ordinates[o] = array[i + o];
				}
				pointArray.add(new DirectPositionImpl(crs, ordinates));
			}
		}
		return pointArray;
	}
	
	public PointArray createPointArray(final double[] array,
			final int start, final int end) {
		PointArray pointArray = (PointArray) createPointArray();
		for (int i=start; i<array.length && i <= end; i++) {
			pointArray.add(array[i]);
		}
		return pointArray;
	}
	public PointArray createPointArray() {
		return new PointArrayImpl(crs);
	}

}
