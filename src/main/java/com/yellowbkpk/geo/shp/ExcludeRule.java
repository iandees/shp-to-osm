package com.yellowbkpk.geo.shp;

import osm.primitive.Primitive;
import osm.primitive.PrimitiveTypeEnum;

public class ExcludeRule {

    private PrimitiveTypeEnum type;
    private String key;
    private String value;

    public ExcludeRule(PrimitiveTypeEnum type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public boolean allows(Primitive w) {
        if (type != w.getType()) {
            return false;
        }

        String primitiveTagValue = w.getTagValue(key);
        boolean tagExists = primitiveTagValue != null;
        if (!tagExists) {
            return "".equals(this.key);
        } else {
            return "*".equals(this.value)
                    || this.value.equals(primitiveTagValue);
        }
    }
    
    public String toString() {
        return type + ": Exclude when " + key + "==" + value;
    }

}
