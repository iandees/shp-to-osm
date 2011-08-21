package com.yellowbkpk.osm.output;


import java.util.LinkedList;
import java.util.List;

import com.yellowbkpk.osm.OSMFile;

public class SaveEverything extends AbstractOutputter {

    private List<OutputFilter> filters = new LinkedList<OutputFilter>();
    private OSMOutputter finalOutput;
    
    public SaveEverything(OSMOutputter finalOutput) {
        this.finalOutput = finalOutput;
    }
    
    private boolean checkChanges() {
        // Never save the file.
        return false;
    }
    
    public void write(OSMFile out) {
        for (OutputFilter filter : filters) {
            out = filter.apply(out);
        }
        
        finalOutput.write(out);
    }

    /**
     * @param filter The filter to chain in.
     * @return A copy of this outputter with the given filter added.
     */
    public SaveEverything withFilter(OutputFilter filter) {
        this.filters.add(filter);
        return this;
    }

}
