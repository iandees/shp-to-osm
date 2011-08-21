package com.yellowbkpk.osm.primitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * @author Ian Dees
 *
 */
public abstract class Primitive {

    private int id;
    private User user;
    private boolean visible = true;
    private Integer version = null;
    private List<Tag> tagsList = new ArrayList<Tag>();
    
    public void setVersion(Integer ver) {
        this.version = ver;
    }
    
    public Integer getVersion() {
        return this.version;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isVisible() {
        return this.visible;
    }
    
    public void setID(int id) {
        this.id = id;
    }
    
    public int getID() {
        return this.id;
    }

    public Iterator<Tag> getTagIterator() {
        return tagsList.iterator();
    }

    public boolean hasTags() {
        return (tagsList.size() > 0);
    }

    public void addTag(Tag tag) {
        tagsList.add(tag);
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Primitive) {
            return ((Primitive) obj).id == id;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id;
    }

    public String getTagValue(String key) {
        for (Tag tag : tagsList) {
            if (key.equals(tag.getKey())) {
                return tag.getValue();
            }
        }
        return null;
    }

    public void copyTags(Primitive type) {
        Collections.copy(this.tagsList, type.tagsList);
    }

    /**
     * @return
     */
    public abstract PrimitiveTypeEnum getType();

    public void setUser(User user) {
        this.user = user;
    }
    
    public User getUser() {
        return user;
    }

}
