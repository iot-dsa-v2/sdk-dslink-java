package org.iot.dsa.profiler;

import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;


public class ProfilerUtils {
	
	public static DSMap makeColumn(String name, DSValueType type) {
		return new DSMetadata().setName(name).setType(type).getMap();
	}
	
	public static Map<String, String> dsMapToMap(DSMap dsMap) {
		Map<String, String> map = new HashMap<String, String>();
		for(int i=0; i<dsMap.size(); i++) {
			Entry en = dsMap.getEntry(i);
			map.put(en.getKey(), en.getValue().toString());
		}
		return map;
	}
	
	public static DSList listToDSList(List<String> l) {
		String[] a = new String[l.size()];
		return DSList.valueOf(l.toArray(a));
	}
	
	public static void putInMap(DSMap map, String key, Object value) {
		if (value instanceof Long) {
			map.put(key, (Long) value);
		} else if (value instanceof Integer) {
			map.put(key, (Integer) value);
		} else if (value instanceof Number) {
			map.put(key, ((Number) value).doubleValue());
		} else if (value instanceof Boolean) {
			map.put(key, (Boolean) value);
		} else {
			map.put(key, value.toString());
		}
	}
	
	public static DSElement objectToDSIValue(Object o) {
		if (o instanceof Number) {
			if (o instanceof Long) {
				return DSLong.valueOf((Long) o);
			} else if (o instanceof Integer) {
				return DSLong.valueOf((Integer) o);
			} else if (o instanceof Double) {
				return DSDouble.valueOf((Double) o);
			} else if (o instanceof Float) {
				return DSDouble.valueOf((Float) o);
			} else {
				return DSDouble.valueOf(((Number) o).doubleValue());
			}
		} else if (o instanceof Boolean) {
			return DSBool.valueOf((Boolean) o);
		} else if (o instanceof Object[]) {
			DSList l = new DSList();
			for (Object elem: (Object[]) o) {
				l.add(objectToDSIValue(elem));
			}
			return l;
		} else if (o instanceof MemoryUsage) {
			DSMap m = new DSMap();
			MemoryUsage mu = (MemoryUsage) o;
			m.put("Init", mu.getInit());
			m.put("Used", mu.getUsed());
			m.put("Committed", mu.getCommitted());
			m.put("Max", mu.getMax());
			return m;
		} else {
			return DSString.valueOf(o.toString());
		}
	}
	
	public static String millisToString(long millis) {
		long totsecs = millis/1000;
		double millisfrac = (millis%1000)/1000.0; 
		long totmins = totsecs/60;
		double secs = totsecs%60 + millisfrac;
		long hrs = totmins/60;
		long mins = totmins%60;
		StringBuilder sb = new StringBuilder();
		sb.append(secs).append(" Seconds");
		if (mins > 0 || hrs > 0) {
			sb.insert(0, " Minutes, ");
			sb.insert(0, mins);
		}
		if (hrs > 0) {
			sb.insert(0, " Hours, ");
			sb.insert(0, hrs);
		}
		return sb.toString();
	}
	
	public static String stackTraceToString(StackTraceElement[] stackArr) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement elem: stackArr) {
			sb.append(elem.toString());
			sb.append('\n');
		}
		return sb.toString();
		
	}
}
