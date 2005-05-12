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

import com.vividsolutions.jts.geom.Envelope;

/**
 * Creates Geometry line objects for use by the ShapeRenderer.
 * 
 * @author jeichar
 * @since 2.1.x
 */
public class MultiLineHandler implements ShapeHandler {

	private ShapeType type;

	private Envelope bbox;

	double spanx, spany;

	private MathTransform mt;

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
	 * @throws TransformException
	 */
	public MultiLineHandler(ShapeType type, Envelope env, MathTransform mt)
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

	/**
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
			//            skipMultiLineGeom(buffer, dimensions);
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
		double[][] coords= new double[numParts][];
		double[][] transformed = new double[numParts][];
		int[] total = new int[numParts];
		// if needed in future otherwise all references to a z are commented
		// out.
		// if( dimensions==3 )
		// z=new double[numParts][];

		int finish, start = 0;
		int length = 0;
		// boolean clonePoint = false;
		// if bbox is less than a pixel then decimate the geometry.  But orientation must
		// remain the same so geometry data must be parsed.
		if (bboxdecimate){
			coords=new double[1][];
			coords[0]=new double[4];
			transformed=new double[1][];
			transformed[0] = new double[4];
			coords[0][0]=buffer.getDouble();
			coords[0][1]=buffer.getDouble();
			buffer.position((buffer.position() + (numPoints-2) * 16));
			coords[0][2]=buffer.getDouble();
			coords[0][3]=buffer.getDouble();
			try {
				mt.transform(coords[0], 0, transformed[0], 0, 2);
			} catch (Exception e) {
				ShapeRenderer.LOGGER
						.severe("could not transform coordinates "
								+ e.getLocalizedMessage());
				transformed[0]=coords[0];
			}		
			}else{
			for (int part = 0; part < numParts; part++) {
				start = partOffsets[part];

				if (part == (numParts - 1)) {
					finish = numPoints;
				} else {
					finish = partOffsets[part + 1];
				}

				length = finish - start;
				// if (length == 1) {
				// length = 2;
				// clonePoint = true;
				// } else {
				// clonePoint = false;
				// }
				coords[part] = new double[length * 2];
				int readDoubles = 0;
				int currentDoubles = 0;
				int totalDoubles = length * 2;
				for (; currentDoubles < totalDoubles;) {
					coords[part][readDoubles] = buffer.getDouble();
					readDoubles++;
					currentDoubles++;
					coords[part][readDoubles] = buffer.getDouble();
					readDoubles++;
					currentDoubles++;
					if (currentDoubles > 3 && currentDoubles < totalDoubles - 1) {
						if (Math.abs(coords[part][readDoubles - 4]
								- coords[part][readDoubles - 2]) <= spanx
								&& Math.abs(coords[part][readDoubles - 3]
										- coords[part][readDoubles - 1]) <= spany) {
							readDoubles -= 2;
						}
					}
				}
				total[part] = readDoubles / 2;

				if (!mt.isIdentity()) {
					try {
						transformed[part] = new double[readDoubles];
						mt.transform(coords[part], 0, transformed[part], 0,
								readDoubles / 2);
					} catch (Exception e) {
						ShapeRenderer.LOGGER
								.severe("could not transform coordinates "
										+ e.getLocalizedMessage());
						transformed[part]=coords[part];
					}
				} else
					transformed[part] = coords[part];
				// if(clonePoint) {
				// builder.setOrdinate(builder.getOrdinate(0, 0), 0, 1);
				// builder.setOrdinate(builder.getOrdinate(1, 0), 1, 1);
				// }

			}

			// if we have another coordinate, read and add to the coordinate
			// sequences
			if (dimensions == 3) {
				// z min, max
				buffer.position(buffer.position() + 2 * 8 + 8 * numPoints);
				// for (int part = 0; part < numParts; part++) {
				// start = partOffsets[part];
				//
				// if (part == (numParts - 1)) {
				// finish = numPoints;
				// } else {
				// finish = partOffsets[part + 1];
				// }
				//
				// length = finish - start;
				// // if (length == 1) {
				// // length = 2;
				// // clonePoint = true;
				// // } else {
				// // clonePoint = false;
				// // }
				//
				// for (int i = 0; i < length; i++) {
				// builder.setOrdinate(lines[part], buffer.getDouble(), 2, i);
				// }
				//
				// }
			}
		}
		return new Geometry(type, transformed, geomBBox);
	}

	/**
	 * @return
	 */
	private Geometry decimateBasedOnEnvelope(Envelope geomBBox) {
		if (geomBBox.getWidth() <= spanx && geomBBox.getHeight() <= spany) {
			double[][] coords = new double[1][];
			coords[0] = new double[] { geomBBox.getMinX(), geomBBox.getMinY() };
			double[][] transformed = new double[1][];
			transformed[0] = new double[4];
			try {
				mt.transform(coords[0], 0, transformed[0], 0, 1);
			} catch (Exception e) {
				ShapeRenderer.LOGGER.severe("could not transform coordinates "
						+ e.getLocalizedMessage());
				transformed = coords;
			}
			transformed[0][2]=transformed[0][0];
			transformed[0][3]=transformed[0][1];
			return new Geometry(type, transformed, geomBBox);
		}
		return null;
	}

	private void skipMultiLineGeom(ByteBuffer buffer, int dimensions) {
		int numParts = buffer.getInt();
		int numPoints = buffer.getInt(); // total number of points

		// skip partOffsets
		buffer.position(buffer.position() + numParts * 4);

		// skip x y points;
		buffer.position(buffer.position() + numPoints * 4);

		// if we have another coordinate, read and add to the coordinate
		// sequences
		if (dimensions == 3) {
			// skip z min, max and z points
			buffer.position(buffer.position() + 2 * 8 + 8 * numPoints);
		}
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
