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

import java.nio.ByteBuffer;

import org.geotools.data.shapefile.shp.ShapeHandler;
import org.geotools.data.shapefile.shp.ShapeType;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.algorithm.RobustCGAlgorithms;
import com.vividsolutions.jts.geom.Envelope;

/**
 * @TODO class description
 * 
 * @author jeichar
 * @since 2.1.x
 */
public class PolygonHandler implements ShapeHandler {

	private ShapeType type;

	private Envelope bbox;

	double spanx, spany;

	private MathTransform mt;

	RobustCGAlgorithms cga = new RobustCGAlgorithms();

	/**
	 * Create new instance
	 * 
	 * @param type
	 *            the type of shape.
	 * @param env
	 *            the area that is visible. If shape is not in area then skip.
	 * @param mt
	 *            the transform to go from data to the envelope (and that should
	 *            be used to transform the shape coords)
	 */
	public PolygonHandler(ShapeType type, Envelope env, MathTransform mt)
			throws TransformException {
		this.type = type;
		this.bbox = env;
		this.mt = mt;
		if (mt != null) {
			MathTransform screenToWorld = mt.inverse();
			double[] original = new double[] { 0, 0, 1, 1 };
			double[] coords = new double[4];
			screenToWorld.transform(original, 0, coords, 0, 2);
			this.spanx = Math.abs(coords[0] - coords[2]);
			this.spany = Math.abs(coords[1] - coords[3]);
		}
	}

	/**
	 * @see org.geotools.data.shapefile.shp.ShapeHandler#getShapeType()
	 */
	public ShapeType getShapeType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.data.shapefile.shp.ShapeHandler#read(java.nio.ByteBuffer,
	 *      org.geotools.data.shapefile.shp.ShapeType)
	 */
	public Object read(ByteBuffer buffer, ShapeType type) {
		if (type == ShapeType.NULL) {
			return null;
		}

		int dimensions = (type == ShapeType.ARCZ) ? 3 : 2;
		// read bounding box
		double[] tmpbbox = new double[4];
		tmpbbox[0] = buffer.getDouble();
		tmpbbox[1] = buffer.getDouble();
		tmpbbox[2] = buffer.getDouble();
		tmpbbox[3] = buffer.getDouble();

		Envelope geomBBox = new Envelope(tmpbbox[0], tmpbbox[2], tmpbbox[1],
				tmpbbox[3]);

		if (!bbox.intersects(geomBBox)) {
			return null;
		}

		boolean bboxdecimate = geomBBox.getWidth() <= spanx
				&& geomBBox.getHeight() <= spany;
		int numParts = buffer.getInt();
		int numPoints = buffer.getInt(); // total number of points

		int[] partOffsets = new int[numParts];

		// points = new Coordinate[numPoints];
		for (int i = 0; i < numParts; i++) {
			partOffsets[i] = buffer.getInt();
		}
		double[][] coords = new double[numParts][];
		double[][] transformed = new double[numParts][];
		// if needed in future otherwise all references to a z are commented
		// out.
		// if( dimensions==3 )
		// z=new double[numParts][];

		int finish, start = 0;
		int length = 0;
		if (bboxdecimate) {
			coords = new double[1][];
			coords[0] = new double[4];
			transformed = new double[1][];
			transformed[0] = new double[4];
			coords[0][0] = buffer.getDouble();
			coords[0][1] = buffer.getDouble();
			buffer.position((buffer.position() + (numPoints - 2) * 16));
			coords[0][2] = buffer.getDouble();
			coords[0][3] = buffer.getDouble();
			if (!bbox.contains(coords[0][0], coords[0][1])
					&& !bbox.contains(coords[0][2], coords[0][3]))
				return null;
			try {
				mt.transform(coords[0], 0, transformed[0], 0, 2);
			} catch (Exception e) {
				ShapeRenderer.LOGGER.severe("could not transform coordinates "
						+ e.getLocalizedMessage());
				transformed[0] = coords[0];
			}
		} else {
			Envelope partEnvelope = new Envelope();
			int partsInBBox = 0;

			for (int part = 0; part < numParts; part++) {
				start = partOffsets[part];
				partEnvelope.init();

				if (part == (numParts - 1)) {
					finish = numPoints;
				} else {
					finish = partOffsets[part + 1];
				}

				length = finish - start;
				int totalDoubles = length * 2;
				coords[part] = new double[totalDoubles];
				int readDoubles = 0;
				int currentDoubles = 0;
				for (; currentDoubles < totalDoubles;) {
					try {
						coords[part][readDoubles] = buffer.getDouble();
						readDoubles++;
						currentDoubles++;
						coords[part][readDoubles] = buffer.getDouble();
						readDoubles++;
						currentDoubles++;
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (currentDoubles > 3 && currentDoubles < totalDoubles - 1) {
						if (Math.abs(coords[part][readDoubles - 4]
								- coords[part][readDoubles - 2]) <= spanx
								&& Math.abs(coords[part][readDoubles - 3]
										- coords[part][readDoubles - 1]) <= spany) {
							readDoubles -= 2;
						} else {
							partEnvelope.expandToInclude(
									coords[part][readDoubles - 2],
									coords[part][readDoubles - 1]);
						}
					} else {
						partEnvelope.expandToInclude(
								coords[part][readDoubles - 2],
								coords[part][readDoubles - 1]);
					}
				}
				if (!partEnvelope.intersects(bbox)) {
					continue;
				}
				if (!mt.isIdentity()) {
					try {
						transformed[partsInBBox] = new double[readDoubles];
						mt.transform(coords[part], 0, transformed[partsInBBox],
								0, readDoubles / 2);
					} catch (Exception e) {
						ShapeRenderer.LOGGER
								.severe("could not transform coordinates "
										+ e.getLocalizedMessage());
						transformed[partsInBBox] = coords[part];
					}
				} else {
					transformed[partsInBBox] = new double[readDoubles];
					System.arraycopy(coords[part], 0, transformed[partsInBBox],
							0, readDoubles / 2);
				}
				partsInBBox++;
			}
			if (partsInBBox == 0)
				return null;
			if (partsInBBox != numParts) {
				double[][] tmp = new double[partsInBBox][];
				System.arraycopy(transformed, 0, tmp, 0, partsInBBox);
				transformed = tmp;
			}
		}
		return new SimpleGeometry(type, transformed, geomBBox);
	}

	/**
	 * @see org.geotools.data.shapefile.shp.ShapeHandler#write(java.nio.ByteBuffer,
	 *      java.lang.Object)
	 */
	public void write(ByteBuffer buffer, Object geometry) {
		// This handler doesnt write
		throw new UnsupportedOperationException(
				"This handler is only for reading");
	}

	/**
	 * @see org.geotools.data.shapefile.shp.ShapeHandler#getLength(java.lang.Object)
	 */
	public int getLength(Object geometry) {
		// TODO Auto-generated method stub
		return 0;
	}

}
