package com.yellowbkpk.geo.glom;
import com.yellowbkpk.osm.OSMFile;
import com.yellowbkpk.osm.output.OutputFilter;


public class GlommingFilter implements OutputFilter {

    private Glommer glommer;

    /**
     * @param glomKey
     */
    public GlommingFilter(String glomKey) {
        glommer = new Glommer(glomKey);
    }

    /**
     * {@inheritDoc}
     * @return 
     */
    public OSMFile apply(OSMFile out) {
        System.out.println("Glomming " + out.getWayCount() + " ways.");
        return glommer.glom(out);
    }

}
