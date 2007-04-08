CREATE SCHEMA "geotools";

CREATE TABLE "geotools"."featureType1" ( "id" int AUTO_INCREMENT(1) primary key , "geometry" OTHER, "intProperty" int, "doubleProperty" double, "stringProperty" varchar );
INSERT INTO "geotools"."featureType1" ( "geometry", "intProperty","doubleProperty", "stringProperty" ) VALUES ( GeometryFromText('POINT(1 1)'),1,1.1,'one' );
INSERT INTO "geotools"."featureType1" ( "geometry", "intProperty","doubleProperty", "stringProperty" ) VALUES ( GeometryFromText('POINT(2 2)'),2,2.2,'two' );
INSERT INTO "geotools"."featureType1" ( "geometry", "intProperty","doubleProperty", "stringProperty" ) VALUES ( GeometryFromText('POINT(3 3)'),3,3.3,'three' );

COMMIT;