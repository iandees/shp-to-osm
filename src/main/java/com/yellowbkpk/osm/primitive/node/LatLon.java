package com.yellowbkpk.osm.primitive.node;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public class LatLon extends Double {

    public LatLon(double lat, double lon) {
        super(lat, lon);
    }
    
    public double getLat() {
        return x;
    }
    
    public double getLon() {
        return y;
    }

    @Override
    public void setLocation(double x, double y) {
        // Don't allow any setting
    }

    @Override
    public void setLocation(Point2D p) {
        // Don't allow any setting
    }

}
