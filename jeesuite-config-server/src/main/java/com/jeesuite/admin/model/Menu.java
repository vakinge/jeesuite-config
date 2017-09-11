package com.jeesuite.admin.model;

import java.util.ArrayList;
import java.util.List;

public class Menu {

	private String title;
	private String icon;
	private boolean spread;
	private List<SubMenu> children = new ArrayList<>();
	
	public Menu() {}
	
	public Menu(String title, String icon, boolean spread) {
		super();
		this.title = title;
		this.icon = icon;
		this.spread = spread;
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
	public boolean isSpread() {
		return spread;
	}
	public void setSpread(boolean spread) {
		this.spread = spread;
	}

	public List<SubMenu> getChildren() {
		return children;
	}

	public void setChildren(List<SubMenu> children) {
		this.children = children;
	}
	
	
}
