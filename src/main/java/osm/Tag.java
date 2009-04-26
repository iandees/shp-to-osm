package osm;


/**
 * @author Ian Dees
 *
 */
public class Tag {

    private String key;
    private String value;

    /**
     * @param key
     * @param value
     */
    public Tag(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * @return
     */
    public String getKey() {
        return key;
    }

    /**
     * @return
     */
    public String getValue() {
        return value;
    }

}
