package org.geotools.geometry.iso;

import java.util.List;

import org.geotools.geometry.iso.coordinate.DirectPositionImpl;
import org.geotools.geometry.iso.coordinate.DoublePointArray;
import org.geotools.geometry.iso.coordinate.PointArrayImpl;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.PositionFactory;
import org.opengis.geometry.Precision;
import org.opengis.geometry.PrecisionType;
import org.opengis.geometry.coordinate.PointArray;
import org.opengis.geometry.coordinate.Position;

/**
 * Default implementation of PositionFactory that stores contents using double.
 * <p>
 * You should be aware of the following:
 * <ul>
 * <li>createPositionList() is backed by an ArrayList
 * <li>createPositionList( double, int, int) is a custom efficient
 * implementation that does not support add
 * <li>createPositionList( float, int, int ) will copy the array contents into
 * individual DirectPositions.
 * </ul>
 * 
 * @author Jody Garnett
 */
public class PositionFactoryImpl implements PositionFactory {
	private Precision precision;

	public PositionFactoryImpl(CoordinateReferenceSystem crs) {
		this(crs, new PrecisionModel(PrecisionType.DOUBLE));
	}	
	
	public PositionFactoryImpl(CoordinateReferenceSystem crs,
			Precision precision) {
		assert( precision.getType() == PrecisionType.DOUBLE );
		this.crs = crs;
		this.precision = precision;
	}

	final CoordinateReferenceSystem crs;

	public DirectPosition createDirectPosition(double[] coords)
			throws MismatchedDimensionException {
		if (coords != null) return new DirectPositionImpl(crs, coords);
		return new DirectPositionImpl(crs);
	}

	public Position createPosition(Position position) {
		DirectPosition directPosition = position.getPosition();
		return new DirectPositionImpl(directPosition);
	}

	public List<Position> createPositionList() {
		return new PointArrayImpl(crs);
	}

	public List<Position> createPositionList(final double[] array,
			final int start, final int end) {
		return new DoublePointArray(crs, array, start, end);
	}

	public List<Position> createPositionList(float[] array, int start, int end) {
		PointArray pointArray = (PointArray) createPositionList();
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

	public CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return crs;
	}

	public Precision getPrecision() {
		return precision;
	}

}
