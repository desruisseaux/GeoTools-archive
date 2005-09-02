GeoWidgets 1.0.M1
*****************

This is a milestone release, that is, it is expected to be fairly stable,
but currently NOT AT ALL feature complete or "mature" for production use.

Current content:
- GeoWidgets framework (utilities for localization, logging, validation, UI, etc.)
- CRS widgets, such as the CRS assembly widgets and units dropdown
- Swing and SWT/JFace implementations.

GeoWidgets is LGPL. The release contains both binary and source jar as well
as the stable project structure. There will hopefully be a Subversion (SVN)
repository, where also code in development is to be found.

Usage
-----
More detailed documentation will be up soon as PDF.
Please note that GeoWidgets is not an executable Java application, but rather
a library. It was made to be used inside a Java SDK to develop own applications.
If you intend developing ON GeoWidgets using Eclipse, unpack the .jar and copy
the libraries into the project's "lib" directory. (Otherwise you will have to
set the classpath manually to point to the libraries.)

The CRS widgets rely on the EPSG (European Petroleum Survey Group) database.
GeoWidgets comes with the GeoTools libraries that support the EPSG database
as Access (usually under Windows) or HSQL version. If you prefer the former,
make sure that the database is installed as "EPSG" in the ODBC dialog
in the Windows settings. There are JUnit test cases included, which you might
run to check if things like the EPSG database work correctly.

Look for these Swing widgets in org.geowidgets.crs.widgets.swing:
- JUnitComboBox
  (to select linear/angular/both units)
- JAxisDirComboBox
  (to select coordinate system axis directions from)
- JProjectedCRSAssemblyPanel, JGeographicCRSAssemblyPanel and others
  (to create coordinate reference systems and related objects)
Look for this SWT widgets in org.geowidgets.crs.widgets.swt:
- UnitComboBox
Look for this Eclipse-based widgets in org.geowidgets.crs.widgets.propertysheet:
- CRSElementPropertyViewer
Details on them will be in the detailed documentation.

Any feedback is appreciated.

Matthias Basler