package org.iot.dsa.security;

/**
 * Used to define the required permissions for various objects.
 *
 * @author Aaron Hansen
 */
public enum DSPermission {

    //Write permission NEVER = 0;
    LIST("list", 0x1),
    READ("read", 0x2),
    WRITE("write", 0x3),
    CONFIG("config", 0x4);

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

    /**
     * True if this level is higher than the given.
     */
    public boolean isGreaterThan(DSPermission arg) {
        return level >= arg.getLevel();
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

    public static DSPermission forString(String str) {
        for (DSPermission p : DSPermission.values()) {
            if (p.toString().equalsIgnoreCase(str)) {
                return p;
            }
        }
        throw new IllegalArgumentException("Unknown permission: " + str);
    }

    @Override
    public String toString() {
        return display;
    }

    public static DSPermission valueOf(int v2byte) {
        switch (v2byte) {
            case 0x10:
                return LIST;
            case 0x20:
                return READ;
            case 0x30:
                return WRITE;
            case 0x40:
                return CONFIG;
        }
        throw new IllegalArgumentException("Unknown permission: " + v2byte);
    }

}
