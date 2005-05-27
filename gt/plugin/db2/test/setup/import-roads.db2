!erase roadsexp*;
!erase roadsimp*;
connect to geotools;
drop table "Test"."Roads";
-- create SRS for New York  state-plane zone 3101 data
!db2se create_srs geotools
  -srsName      NY3101
  -srsId        3101
  -xOffset      0
  -yOffset      0
  -xScale       100
  -coordsysName NAD_1983_STATEPLANE_NEW_YORK_EAST_FIPS_3101_FEET
 ;

!db2se import_shape geotools
  -fileName data\roads.shp
  -srsName NY3101
  -tableSchema \"Test\"
  -tableName   \"Roads\"
  -spatialColumn \"Geom\"
  -client 1
  -messagesFile roadsimp.msg
  -exceptionFile roadsimperr
  -commitScope 100
  ;

!db2se register_spatial_column geotools
  -srsName NY3101
  -tableSchema \"Test\"
  -tableName   \"Roads\"
  -columnName    \"Geom\"
  ;

select count(*) from "Test"."Roads";
describe table  "Test"."Roads";

