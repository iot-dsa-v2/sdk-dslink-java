package org.iot.dsa.node.topic;

/**
 * Basic implementation of DSITopic.
 *
 * @author Aaron Hansen
 */
public class DSTopic implements DSITopic {

    private String id;

    public DSTopic(String id) {
        if (id == null) {
            throw new NullPointerException("ID cannot be null");
        }
        this.id = id;
    }

    /**
     * Returns this.
     */
    @Override
    public DSTopic copy() {
        return this;
    }

    /**
     * True if is a topic IDs match.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSITopic) {
            return ((DSITopic) obj).getTopicId().equals(id);
        }
        return false;
    }

    @Override
    public String getTopicId() {
        return id;
    }

    /**
     * Returns the hashCode of the ID.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Only tests instance equality.
     */
    @Override
    public boolean isEqual(Object obj) {
        return obj == this;
    }

    /**
     * False
     */
    @Override
    public boolean isNull() {
        return false;
    }

    /**
     * Returns the topic ID.
     */
    @Override
    public String toString() {
        return id;
    }

}
