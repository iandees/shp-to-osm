package osm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ian Dees
 *
 */
public abstract class Primitive {

    private int id;
    private List<Tag> tagsList = new ArrayList<Tag>();
    
    public void setID(Integer id) {
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

    /**
     * @return
     */
    public abstract PrimitiveTypeEnum getType();

}
