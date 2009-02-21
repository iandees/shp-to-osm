SHP to OSM 0.3Copyright Ian Dees, All rights reserved
20 Feb 2009
Project source: http://svn.yellowbkpk.com/geo/trunk/shp-to-osm/
Project website: http://redmine.yellowbkpk.com/projects/show/geo

Dependencies

 - GeoTools 2.5 RC0: http://downloads.sourceforge.net/geotools/geotools-2.5-RC0-bin.zip
   The following jars from the above zip file are needed in the build path:
    gt-epsg-wkt-2.5-M3.jar
    gt-shapefile-2.5-M3.jar
    log4j-1.2.12.jar
    gt-epsg-extension-2.5-M3.jar
    commons-lang-2.1.jar
    jsr-275-1.0-beta-2.jar
    jts-1.9.jar
    gt-api-2.5-M3.jar
    jai_core.jar
    gt-metadata-2.5-M3.jar
    gt-referencing-2.5-M3.jar
    geoapi-2.2-M1.jar
    gt-main-2.5-M3.jar

Rules file

 The rules file is a simple comma-separated text file:
 
 Field:  Description:
      1  The shapefile type to match (outer, inner, line, point)
      2  The source attribute name to match
      3  The source attribute value to match. Can be empty to match all values.
      4  The name of the tag to apply when the source key/value pair match.
      5  The value of the tag to apply. Use a sinlgle dash ("-") to use the original value.

Running


 Use the following command line to run the app. Also, you can use the .bat or .sh run files
to issue the same command as long as you give it the same set of arguments. The [-t] at the 
end of the command here is an optional flag to tell the application to onlyinclude ways that
have had a tag applied to them. For now, it is required to be at the end ofthe arguments list.

 java -cp shp-to-osm-0.3.jar:lib/gt-epsg-wkt-2.5-M3.jar:lib/gt-shapefile-2.5-M3.jar:lib/log4j-1.2.12.jar:lib/gt-epsg-extension-2.5-M3.jar:lib/commons-lang-2.1.jar:lib/jsr-275-1.0-beta-2.jar:lib/jts-1.9.jar:lib/gt-api-2.5-M3.jar:lib/jai_core.jar:lib/gt-metadata-2.5-M3.jar:lib/gt-referencing-2.5-M3.jar:lib/geoapi-2.2-M1.jar:lib/gt-main-2.5-M3.jar Main <path to input shapefile> <path to rules file> <path to output osm file> [-t]

Known Issues
 - There is a maximum of 15000 nodes in a way. Long ways are not split into smaller ones.