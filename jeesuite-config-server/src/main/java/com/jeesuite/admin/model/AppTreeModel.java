package com.jeesuite.admin.model;

import java.util.ArrayList;
import java.util.List;

public class AppTreeModel {

	private String id;
	private String name;
	private String code;
	private String serviceId;
	private List<AppTreeModel> children;

	public AppTreeModel() {}
	
	public AppTreeModel(String id, String name, String code) {
		super();
		this.id = id;
		this.name = name;
		this.code = code;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public List<AppTreeModel> getChildren() {
		return children;
	}

	public void setChildren(List<AppTreeModel> children) {
		this.children = children;
	}
	
	public void addChild(AppTreeModel child) {
		if(this.children == null) {
			this.children = new ArrayList<>();
		}
		this.children.add(child);
	}
}
