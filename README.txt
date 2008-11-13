SHP to OSM 0.1
Copyright Ian Dees, All rights reserved
12 Nov 2008

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

 java -cp shp-to-osm-0.1.jar:lib/gt-epsg-wkt-2.5-M3.jar:lib/gt-shapefile-2.5-M3.jar:lib/log4j-1.2.12.jar:lib/gt-epsg-extension-2.5-M3.jar:lib/commons-lang-2.1.jar:lib/jsr-275-1.0-beta-2.jar:lib/jts-1.9.jar:lib/gt-api-2.5-M3.jar:lib/jai_core.jar:lib/gt-metadata-2.5-M3.jar:lib/gt-referencing-2.5-M3.jar:lib/geoapi-2.2-M1.jar:lib/gt-main-2.5-M3.jar <path to input shapefile> <path to rules file> <path to output osm file> 
 
Known Issues
 - The app does not read point data right now.
 - The app will apply a tag even if the data in the shapefile is an empty string or a single space.