import java.io.File;

import osm.OSMFile;


public interface OSMOutputter {

    void write(OSMFile osmOut, File fileOut);

}
