package org.adligo.tests4j_4jacoco.plugin.data.multi;


public class ThreadGroupFilter {
	private final String filterNamePart;
	
	public ThreadGroupFilter(String pFilterNamePart) {
		filterNamePart = pFilterNamePart;
	}
	
	/**
	 * Determines if this thread has a group that matches the filter
	 *   or if the groups parent, grandparent exc
	 *   matches the filter
	 * @return
	 */
	public boolean isDescendedFromFilteredGroup() {
		Thread currentThread = Thread.currentThread();
		ThreadGroup group = currentThread.getThreadGroup();
		String groupName = group.getName();
		if (groupName.indexOf(filterNamePart) == -1) {
			while (group != null) {
				group = group.getParent();
				if (group != null) {
					groupName = group.getName();
					if (groupName.indexOf(filterNamePart) != -1) {
						break;
					}
				}
			}
		}
		if (group == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public String getThreadGroupNameMatchingFilter() {
		Thread currentThread = Thread.currentThread();
		ThreadGroup group = currentThread.getThreadGroup();
		String groupName = group.getName();
		if (groupName.indexOf(filterNamePart) == -1) {
			while (group != null) {
				group = group.getParent();
				if (group != null) {
					groupName = group.getName();
					if (groupName.indexOf(filterNamePart) != -1) {
						break;
					}
				}
			}
		}
		return groupName;
	}
	
	
}
