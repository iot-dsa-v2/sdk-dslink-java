package org.iot.dsa.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.PlatformManagedObject;
import java.util.ArrayList;
import java.util.List;

public class MemoryNode extends MXBeanNode {

	private MemoryMXBean mxbean;

	@Override
	public void setupMXBean() {
		mxbean = ManagementFactory.getMemoryMXBean();
	}

	@Override
	public void refreshImpl() {
		// TODO Auto-generated method stub
	}

	@Override
	public PlatformManagedObject getMXBean() {
		return mxbean;
	}

	@Override
	public Class<? extends PlatformManagedObject> getMXInterface() {
		return MemoryMXBean.class;
	}
	
	private static List<String> overriden = new ArrayList<String>();
	
	@Override
	public List<String> getOverriden() {
		return overriden;
	}

}
