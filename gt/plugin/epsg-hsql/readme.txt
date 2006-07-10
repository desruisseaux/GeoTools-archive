This module is used to allow geotools to communicate with the version of the
EPSG included for use with the java HSQL database.

The instructions are included formally in the javadocs for this module
(see org/geotools/referencing/factory/espg/doc-files/HSQL.html) reproduced
here for your convience.

-- Text of HSQL.html ---

Creating EPSG database for HSQL
The following gives some hints for creating a HSQL database and populate it
with EPSG data from the SQL scripts.

1. Copy the sqltool.rc file in the developper's home directory and edit it URL
   path provided in this file.

2. Prepare a copy of EPSG's SQL scripts and modify them for HSQL syntax. The
   following Ant file can be used for this purpose. As a safety, add the
   following lines at the end of every scripts:

     COMMIT;
     SHUTDOWN;

3. Creates the database using the following commands:

     java org.hsqldb.util.SqlTool --autoCommit EPSG-admin HSQL/EPSG_Tables.sql
     java org.hsqldb.util.SqlTool --autoCommit EPSG-admin HSQL/EPSG_Data.sql
     java org.hsqldb.util.SqlTool --autoCommit EPSG-admin HSQL/EPSG_FKeys.sql

4. Launch the following application:

     java org.hsqldb.util.DatabaseManagerSwing

   Make sure the database is okay, and invoke the following commands:

     SCRIPT 'EPSG.sql';
     SHUTDOWN COMPACT;

5. Quit the application. Edit the EPSG.sql script as below:

   - Remove the SET DELAY 60 statement; 60 is already the default. 
   - Remove the CREATE USER "SA" and GRANT DBA TO SA statements. This user will
     exists before the script is run.
   - Remove the CREATE SCHEMA PUBLIC and SET SCHEMA PUBLIC statements. This
     schema will exists before the script is run. 
   - In the table EPSG_DATUM, change the type of column REALIZATION_EPOCH to INTEGER. 
   - Copy all ALTER TABLE ... FOREIGN KEY statements to the end of the script. 
   - Remove all CONSTRAINT ... FOREIGN KEY from the CREATE TABLE statements at
     the begining of the script, and move them as ALTER TABLE ... FOREIGN KEY
     statements at the end of the script. 
   - Add SHUTDOWN COMPACT statement at the end of file. 

6. Run the following commands:

     java org.geotools.referencing.factory.epsg.Compactor

7. Copy the file created in the above step into the org.geotools.referencing.factory.epsg
   package directory using "EPSG.sql" filename. Finally, create the JAR file as usual,
   which should includes the EPSG.sql script.