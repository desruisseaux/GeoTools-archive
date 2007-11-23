--       (C) 2005 by David Blasby and The Open Planning Project 
--       http://openplans.org 
-- 
--       Released under the LGPL 
-- 
--       contact: dblasby@openplans.org 

CREATE ALIAS GeomFromText for "org.openplans.spatialdbbox.JTS.GeomFromText";
CREATE ALIAS envelope for "org.openplans.spatialdbbox.JTS.envelope";
CREATE ALIAS getSRID FOR "org.openplans.spatialdbbox.JTS.getSRID";
--CREATE ALIAS GeometryFromBytes for "org.openplans.spatialdbbox.JTS.geometryFromBytes";
--CREATE ALIAS extent FOR "org.openplans.spatialdbbox.JTS.extentB";
--CREATE ALIAS setSRID FOR "org.openplans.spatialdbbox.JTS.setSRID";


CREATE ALIAS equals FOR "org.openplans.spatialdbbox.StaticGeometry.equals";
CREATE ALIAS toString FOR "org.openplans.spatialdbbox.StaticGeometry.toString";
CREATE ALIAS contains FOR "org.openplans.spatialdbbox.StaticGeometry.contains";
CREATE ALIAS isEmpty FOR "org.openplans.spatialdbbox.StaticGeometry.isEmpty";
CREATE ALIAS length FOR "org.openplans.spatialdbbox.StaticGeometry.getLength";
CREATE ALIAS intersects FOR "org.openplans.spatialdbbox.StaticGeometry.intersects";
CREATE ALIAS isClosed FOR "org.openplans.spatialdbbox.StaticGeometry.isClosed";
--CREATE ALIAS geometryFromText FOR "org.openplans.spatialdbbox.StaticGeometry.geomFromWKT";

CREATE ALIAS isValid FOR "org.openplans.spatialdbbox.StaticGeometry.isValid";
CREATE ALIAS geometryType FOR "org.openplans.spatialdbbox.StaticGeometry.getGeometryType";
CREATE ALIAS sRID FOR "org.openplans.spatialdbbox.StaticGeometry.getSRID";
CREATE ALIAS numPoints FOR "org.openplans.spatialdbbox.StaticGeometry.getNumPoints";
CREATE ALIAS isSimple FOR "org.openplans.spatialdbbox.StaticGeometry.isSimple";
CREATE ALIAS distance FOR "org.openplans.spatialdbbox.StaticGeometry.distance";
CREATE ALIAS isWithinDistance FOR "org.openplans.spatialdbbox.StaticGeometry.isWithinDistance";
CREATE ALIAS area FOR "org.openplans.spatialdbbox.StaticGeometry.getArea";
CREATE ALIAS centroid FOR "org.openplans.spatialdbbox.StaticGeometry.getCentroid";
CREATE ALIAS interiorPoint FOR "org.openplans.spatialdbbox.StaticGeometry.getInteriorPoint";
CREATE ALIAS dimension FOR "org.openplans.spatialdbbox.StaticGeometry.getDimension";
CREATE ALIAS boundary FOR "org.openplans.spatialdbbox.StaticGeometry.getBoundary";
CREATE ALIAS boundaryDimension FOR "org.openplans.spatialdbbox.StaticGeometry.getBoundaryDimension";
--CREATE ALIAS envelope FOR "org.openplans.spatialdbbox.StaticGeometry.getEnvelope";
CREATE ALIAS disjoint FOR "org.openplans.spatialdbbox.StaticGeometry.disjoint";
CREATE ALIAS touches FOR "org.openplans.spatialdbbox.StaticGeometry.touches";
CREATE ALIAS crosses FOR "org.openplans.spatialdbbox.StaticGeometry.crosses";
CREATE ALIAS within FOR "org.openplans.spatialdbbox.StaticGeometry.within";
CREATE ALIAS overlaps FOR "org.openplans.spatialdbbox.StaticGeometry.overlaps";
CREATE ALIAS relatePattern FOR "org.openplans.spatialdbbox.StaticGeometry.relatePattern";
CREATE ALIAS relate FOR "org.openplans.spatialdbbox.StaticGeometry.relate";
CREATE ALIAS toText FOR "org.openplans.spatialdbbox.StaticGeometry.toText";
CREATE ALIAS buffer_with_segments FOR "org.openplans.spatialdbbox.StaticGeometry.buffer_with_segments";
CREATE ALIAS buffer FOR "org.openplans.spatialdbbox.StaticGeometry.buffer";
CREATE ALIAS convexHull FOR "org.openplans.spatialdbbox.StaticGeometry.convexHull";
CREATE ALIAS intersection FOR "org.openplans.spatialdbbox.StaticGeometry.intersection";
CREATE ALIAS unionGeom FOR "org.openplans.spatialdbbox.StaticGeometry.unionGeom";
CREATE ALIAS difference FOR "org.openplans.spatialdbbox.StaticGeometry.difference";
CREATE ALIAS symDifference FOR "org.openplans.spatialdbbox.StaticGeometry.symDifference";
CREATE ALIAS equalsExactTolerance FOR "org.openplans.spatialdbbox.StaticGeometry.equalsExactTolerance";
CREATE ALIAS equalsExact FOR "org.openplans.spatialdbbox.StaticGeometry.equalsExact";
CREATE ALIAS numGeometries FOR "org.openplans.spatialdbbox.StaticGeometry.getNumGeometries";
CREATE ALIAS geometryN FOR "org.openplans.spatialdbbox.StaticGeometry.getGeometryN";
CREATE ALIAS x FOR "org.openplans.spatialdbbox.StaticGeometry.getX";
CREATE ALIAS y FOR "org.openplans.spatialdbbox.StaticGeometry.getY";
CREATE ALIAS pointN FOR "org.openplans.spatialdbbox.StaticGeometry.getPointN";
CREATE ALIAS startPoint FOR "org.openplans.spatialdbbox.StaticGeometry.getStartPoint";
CREATE ALIAS endPoint FOR "org.openplans.spatialdbbox.StaticGeometry.getEndPoint";
CREATE ALIAS isRing FOR "org.openplans.spatialdbbox.StaticGeometry.isRing";
CREATE ALIAS exteriorRing FOR "org.openplans.spatialdbbox.StaticGeometry.getExteriorRing";
CREATE ALIAS numInteriorRing FOR "org.openplans.spatialdbbox.StaticGeometry.getNumInteriorRing";
CREATE ALIAS interiorRingN FOR "org.openplans.spatialdbbox.StaticGeometry.getInteriorRingN";

COMMIT;
