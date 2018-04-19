package org.iot.dsa.profiler;

import java.lang.management.ThreadInfo;

import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;

public class ThreadInfoNode extends DSNode {
	
	private Long id;
	
	public ThreadInfoNode() {	
	}
	
	public ThreadInfoNode(long id) {
		this.id = id;
	}
	
	@Override
	public void declareDefaults() {
		super.declareDefaults();
		
	}
	
	@Override
	public void onStable() {
		if (id == null) {
			getParent().remove(getInfo());
		}
	}
	
	public void update(ThreadInfo info, long threadCpuTime, long threadUserTime) {
		putProp("ThreadCpuTime", DSString.valueOf(ProfilerUtils.millisToString(threadCpuTime)));
		putProp("ThreadUserTime", DSString.valueOf(ProfilerUtils.millisToString(threadUserTime)));
		putProp("ThreadId", DSLong.valueOf(info.getThreadId()));
		putProp("ThreadName", DSString.valueOf(info.getThreadName()));
		putProp("ThreadState", DSString.valueOf(info.getThreadState()));
		putProp("InNative", DSBool.valueOf(info.isInNative()));
		putProp("Suspended", DSBool.valueOf(info.isSuspended()));
		putProp("BlockedCount", DSLong.valueOf(info.getBlockedCount()));
		putProp("BlockedTime", DSString.valueOf(ProfilerUtils.millisToString(info.getBlockedTime())));
		putProp("WaitedCount", DSLong.valueOf(info.getWaitedCount()));
		putProp("WaitedTime", DSString.valueOf(ProfilerUtils.millisToString(info.getWaitedTime())));
		putProp("LockName", DSString.valueOf(info.getLockName()));
		putProp("LockOwnerId", DSLong.valueOf(info.getLockOwnerId()));
		putProp("LockOwnerName", DSString.valueOf(info.getLockOwnerName()));
		putProp("StackTrace", DSString.valueOf(ProfilerUtils.stackTraceToString(info.getStackTrace())));
	}
	
	private void putProp(String name, DSIObject obj) {
		put(name, obj).setReadOnly(true).setTransient(true);
	}

}
