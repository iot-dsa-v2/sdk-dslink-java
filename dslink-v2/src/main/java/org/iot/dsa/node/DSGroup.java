package org.iot.dsa.node;

import org.iot.dsa.io.json.JsonAppender;

/**
 * An collection of elements.
 *
 * @author Aaron Hansen
 */
public abstract class DSGroup extends DSElement {

    // Fields
    // --------------

    private Object parent;

    // Public Methods
    // --------------

    /**
     * Removes all items.
     *
     * @return This
     */
    public abstract DSGroup clear();

    /**
     * @return Null if empty.
     */
    public abstract DSElement first();

    public boolean hasParent() {
        return parent != null;
    }

    /**
     * Returns true when childCount() == 0.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean isGroup() {
        return true;
    }

    /**
     * @return Null if empty.
     */
    public abstract DSElement last();

    /**
     * @return Null if empty.
     */
    public abstract DSElement removeFirst();

    /**
     * @return Null if empty.
     */
    public abstract DSElement removeLast();

    /**
     * The number of items is the group.
     */
    public abstract int size();

    @Override
    public DSGroup toGroup() {
        return this;
    }

    /**
     * Json encodes the graph, be careful.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        new JsonAppender(buf).value(this).close();
        return buf.toString();
    }

    // Protected Methods
    // -----------------

    void modified() {
        Object p = parent;
        while (p != null) {
            if (parent instanceof GroupListener) {
                ((GroupListener) parent).modified(this);
                return;
            }
            if (p instanceof DSGroup) {
                p = ((DSGroup)p).parent;
            } else {
                p = null;
            }
        }
    }

    /**
     * Sets the parent and returns this for un-parented groups, otherwise throws an
     * IllegalStateException.
     *
     * @param arg The new parent.
     * @return This
     * @throws IllegalStateException If already parented.
     */
    DSGroup setParent(Object arg) {
        if (arg == null) {
            this.parent = null;
            return this;
        }
        if (this.parent != null) {
            throw new IllegalStateException("Already parented");
        }
        this.parent = arg;
        return this;
    }


}
