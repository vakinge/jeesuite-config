package com.jeesuite.admin.constants;

public enum UserType {
	
	user("普通用户"),groupAdmin("组管理员"),superAdmin("超级管理员");
	
	private final String cnName;

	private UserType(String cnName) {
		this.cnName = cnName;
	}

	public String getCnName() {
		return cnName;
	}
	
	
}
