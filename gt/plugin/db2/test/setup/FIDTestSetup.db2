connect to geotools;
drop table "Test".fidautoinc;
drop table "Test".fidnoprikey;
drop table "Test".fidintprikey;
drop table "Test".fidcharprikey;
drop table "Test".fidvcharprikey;
drop table "Test".fidmcolprikey;


create table "Test".fidautoinc (
    idcol integer not null primary key generated always as identity
   ,geom db2gse.st_point)
;
create table "Test".fidintprikey (
    idcol integer not null primary key
   ,geom db2gse.st_point)
;
create table "Test".fidnoprikey (
    idcol integer
   ,geom db2gse.st_point)
;
create table "Test".fidcharprikey (
    idcol char(32) not null primary key
   ,geom db2gse.st_point)
;
create table "Test".fidvcharprikey (
    idcol varchar(32) not null primary key
   ,geom db2gse.st_point
   )
;
create table "Test".fidmcolprikey (
    idcol1 char(32) not null
   ,idcol2 int      not null
   ,geom db2gse.st_point
   ,constraint mcol_pk primary key(idcol1, idcol2)
   )
;
