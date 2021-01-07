package com.jeesuite.admin.constants;

public enum AppType {
	
	micro("微服务"),web("web应用"),other("其他");
	
	private final String cnName;

	private AppType(String cnName) {
		this.cnName = cnName;
	}

	public String getCnName() {
		return cnName;
	}
	
	
}
