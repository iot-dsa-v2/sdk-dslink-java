package org.iot.dsa.time;

import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValue;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;
import org.iot.dsa.node.action.DSISetAction;

/**
 * Time of day in the format hh:mm:ss.  Hours are 0-23.
 *
 * @author Aaron Hansen
 */
public class DSTime extends DSValue implements DSISetAction {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    private static final String HOUR = "Hour";
    private static final String MINUTE = "Minute";
    private static final String SECOND = "Second";
    public static final DSTime NULL = new DSTime(0, 0, 0, DSString.NULL);

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private int hour;
    private int minute;
    private int second;
    private DSString string;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    private DSTime(int hour, int minute, int second, DSString string) {
        this.hour = checkHour(hour);
        this.minute = checkMinute(minute);
        this.second = checkSecond(second);
        if (string == null) {
            StringBuilder buf = new StringBuilder();
            if (hour < 10) {
                buf.append('0');
            }
            buf.append(hour);
            buf.append(':');
            if (minute < 10) {
                buf.append('0');
            }
            buf.append(minute);
            buf.append(':');
            if (second < 10) {
                buf.append('0');
            }
            buf.append(second);
        }
        this.string = string;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSTime) {
            DSTime arg = (DSTime) obj;
            return arg.hour == hour &&
                    arg.minute == minute &&
                    arg.second == second;
        }
        return false;
    }

    /**
     * 0 - 23
     */
    public int getHour() {
        return hour;
    }

    /**
     * 0 - 59
     */
    public int getMinute() {
        return minute;
    }

    /**
     * 0 - 59
     */
    public int getSecond() {
        return second;
    }

    @Override
    public DSAction getSetAction() {
        return SetAction.INSTANCE;
    }

    /**
     * String.
     */
    @Override
    public DSValueType getValueType() {
        return DSValueType.STRING;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    @Override
    public DSElement toElement() {
        if (string == null) {
            StringBuilder buf = new StringBuilder();
            if (hour < 10) {
                buf.append('0');
            }
            buf.append(hour);
            buf.append(':');
            if (minute < 10) {
                buf.append('0');
            }
            buf.append(minute);
            buf.append(':');
            if (second < 10) {
                buf.append('0');
            }
            buf.append(second);
            string = DSString.valueOf(buf.toString());
        }
        return string;
    }

    /**
     * Formatted as hh:mm:ss
     */
    @Override
    public String toString() {
        return toElement().toString();
    }

    /**
     * Creates a DSDateTime for the given range.
     */
    public static DSTime valueOf(int hour, int minute, int second) {
        return new DSTime(hour, minute, second, null);
    }

    @Override
    public DSTime valueOf(DSElement element) {
        return valueOf(element.toString());
    }

    /**
     * Decodes the format "hh:mm:ss"
     */
    public static DSTime valueOf(String string) {
        if (string == null) {
            return NULL;
        }
        if (string.isEmpty() || "null".equals(string)) {
            return NULL;
        }
        String[] split = string.split(":");
        if (split.length != 3) {
            throw new IllegalArgumentException("Invalid format: " + string);
        }
        int hour = Integer.parseInt(split[0]);
        int min = Integer.parseInt(split[1]);
        int sec = Integer.parseInt(split[2]);
        return new DSTime(hour, min, sec, DSString.valueOf(string));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    private int checkHour(int hour) {
        if ((hour < 0) || (hour > 23)) {
            throw new IllegalArgumentException("Invalid hour: " + hour);
        }
        return hour;
    }

    private int checkMinute(int minute) {
        if ((minute < 0) || (minute > 59)) {
            throw new IllegalArgumentException("Invalid minute: " + minute);
        }
        return minute;
    }

    private int checkSecond(int second) {
        if ((second < 0) || (second > 59)) {
            throw new IllegalArgumentException("Invalid second: " + second);
        }
        return second;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    public static class SetAction extends DSAction {

        public static final SetAction INSTANCE = new SetAction();

        @Override
        public ActionResults invoke(DSIActionRequest request) {
            DSMap params = request.getParameters();
            int hr = params.get(HOUR, 0);
            int min = params.get(MINUTE, 0);
            int sec = params.get(SECOND, 0);
            DSInfo<?> target = request.getTargetInfo();
            target.getParent().put(target, valueOf(hr, min, sec));
            return null;
        }

        @Override
        public void prepareParameter(DSInfo<?> target, DSMap parameter) {
            DSTime dt = (DSTime) target.get();
            String name = parameter.get(DSMetadata.NAME, "");
            switch (name) {
                case HOUR:
                    parameter.put(DSMetadata.DEFAULT, dt.getHour());
                    break;
                case MINUTE:
                    parameter.put(DSMetadata.DEFAULT, dt.getMinute());
                    break;
                case SECOND:
                    parameter.put(DSMetadata.DEFAULT, dt.getSecond());
                    break;
            }
        }

        {
            addParameter(HOUR, DSInt.NULL, null);
            addParameter(MINUTE, DSInt.NULL, null);
            addParameter(SECOND, DSInt.NULL, null);
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSTime.class, NULL);
    }

}
