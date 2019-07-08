package org.iot.dsa.rollup;

import java.util.TreeMap;
import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSElement;

/**
 * The most common value in the the rollup.
 *
 * @author Aaron Hansen
 */
final class ModeRollupFunction extends AbstractRollupFunction {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private int curCnt;
    private DSElement curVal;
    private TreeMap<DSElement, Integer> map = new TreeMap<DSElement, Integer>();

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public ModeRollupFunction() {
        super(DSRollup.MODE);
        reset();
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Public and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public DSElement getValue() {
        if (getCount() == 0) {
            return DSDouble.NULL;
        }
        return curVal;
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Protected and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    protected void onReset() {
        map.clear();
        curCnt = 0;
        curVal = null;
    }

    @Override
    protected boolean onUpdate(DSElement arg, int status) {
        if (getCount() == 0) {
            curCnt = 1;
            curVal = arg;
            map.put(curVal, curCnt);
        } else {
            int cnt = 1;
            Integer i = map.get(arg);
            if (i != null) {
                cnt += i;
            }
            map.put(arg, cnt);
            if (cnt > curCnt) {
                curCnt = cnt;
                curVal = arg;
            }
        }
        return true;
    }

}
