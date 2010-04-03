package com.yellowbkpk.geo.shp;


public class ShpToOsmException extends Exception {

    public ShpToOsmException(String string, Throwable e) {
        super(string, e);
    }

    public ShpToOsmException(String string) {
        super(string);
    }

}
