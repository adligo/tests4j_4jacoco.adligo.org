package org.adligo.tests4j_4jacoco.plugin.instrumentation.common;

public enum ObtainProbesStrategyType implements I_ObtainProbesOfType  {
	INTERFACE(0), CLASS(1);

	private int id_;
	
	@Override
	public int getId() {
		return id_;
	}
	
	private ObtainProbesStrategyType(int id) {
		id_ = id;
	}
	
	public static ObtainProbesStrategyType get(I_ObtainProbesOfType type) {
		switch (type.getId()) {
			case 1:
				return CLASS;
			default:
				return INTERFACE;
		}
	}
	
	public static I_ObtainProbesOfType get(int id) {
		switch (id) {
			case 1:
				return CLASS;
			default:
				return INTERFACE;
		}
	}
}
