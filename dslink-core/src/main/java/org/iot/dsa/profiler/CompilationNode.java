package org.iot.dsa.profiler;

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;
import java.util.ArrayList;
import java.util.List;

import org.iot.dsa.node.DSString;

public class CompilationNode extends MXBeanNode {

	private CompilationMXBean mxbean;

	@Override
	public void setupMXBean() {
		mxbean = ManagementFactory.getCompilationMXBean();
	}

	@Override
	public void refreshImpl() {
		putProp("TotalCompilationTime", DSString.valueOf(ProfilerUtils.millisToString(mxbean.getTotalCompilationTime())));
	}

	@Override
	public PlatformManagedObject getMXBean() {
		return mxbean;
	}

	@Override
	public Class<? extends PlatformManagedObject> getMXInterface() {
		return CompilationMXBean.class;
	}
	
	private static List<String> overriden = new ArrayList<String>();
	static {
		overriden.add("TotalCompilationTime");
	}
	
	@Override
	public List<String> getOverriden() {
		return overriden;
	}

}
