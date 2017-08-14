package org.iot.dsa.security;

/**
 * Used to define the required permissions for various objects.
 *
 * @author Aaron Hansen
 */
public enum DSPermission {

    //Write permission NEVER = 0;
    LIST("list", 1),
    READ("read", 2),
    WRITE("write", 3),
    CONFIG("config", 4);

    private String display;
    private int level;

    DSPermission(String display, int level) {
        this.display = display;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public boolean isConfig() {
        return this == CONFIG;
    }

    public boolean isRead() {
        return this == READ;
    }

    public boolean isWrite() {
        return this == WRITE;
    }

    public boolean isList() {
        return this == LIST;
    }

    @Override
    public String toString() {
        return display;
    }

    public static DSPermission forString(String str) {
        for (DSPermission p : DSPermission.values()) {
            if (p.toString().equals(str)) {
                return p;
            }
        }
        return null;
    }

}
