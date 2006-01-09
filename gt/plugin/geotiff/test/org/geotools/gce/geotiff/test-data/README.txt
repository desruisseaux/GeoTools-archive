This directory was used to contains the following file:

    Name                                 Size
    ----------------------------------   --------
    002025_0100_010722_l7_01_utm21.tif   55473968
    meghan.tif                               3393

Those files have been removed after revision 17464 in order to trim
the SVN checkout and source download size. The "meghan" file is not
big but was unusued.

Other files that may need to be revisited are:

    Name                                 Size
    ----------------------------------   --------
    non-arc-meghan.tif                     159626
    fire.jpg                                13334
    fire.tif                                37943
 
The "fire" files are not that big, but the test just check that they
are not GeoTIFF files. Smaller images (like "cir.tif", which is 1026
bytes) should be enough for such tests.

Issue tracking: http://jira.codehaus.org/browse/GEOT-794
