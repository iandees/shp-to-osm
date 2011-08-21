package com.yellowbkpk.geo.shp;

import com.yellowbkpk.osm.primitive.Primitive;
import com.yellowbkpk.osm.primitive.PrimitiveTypeEnum;

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
            // The rule should not exclude if the types are different
            return true;
        }
        
        if ("-".equals(this.key)) {
            // Dash key means exclude tagless primitives
            return w.hasTags();
        }

        String primitiveTagValue = w.getTagValue(key);
        boolean tagExists = primitiveTagValue != null;
        if (tagExists) {
            // Exclude primitives that have the specified tag key. If the user
            // puts a "*" in for the exclude key's value, then the primitive
            // will be excluded simply for having that tag (regardless of
            // value). Otherwise, the tag's key and value have to match what the
            // user enters.
            return "*".equals(this.value)
                    || !this.value.equals(primitiveTagValue);
        } else {
            // Exclude when the primitive has the tag but the value is empty
            // (and the user specified that it be empty).
            return !"".equals(this.value);
        }
    }
    
    public String toString() {
        return type + ": Exclude when " + key + "==" + value;
    }

}
