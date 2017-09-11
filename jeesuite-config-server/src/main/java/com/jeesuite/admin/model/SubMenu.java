package com.jeesuite.admin.model;

public class SubMenu {

	private String title;
	private String icon;
	private String href;
	
	public SubMenu() {}
	
	public SubMenu(String title, String icon, String href) {
		super();
		this.title = title;
		this.icon = icon;
		this.href = href;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}
	
	
}
