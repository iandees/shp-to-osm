package com.yellowbkpk.osm.output;

import com.yellowbkpk.osm.OSMFile;

public interface OutputFilter {

    /**
     * @param out
     */
    OSMFile apply(OSMFile out);

}
