package com.jeesuite.admin.model;

public class SelectOption {

	private String value;
	private String text;
	private boolean selected;
	private String parentValue;
	private String linkkey; //级联
	
	
	public SelectOption(String value, String text) {
		this.value = value;
		this.text = text;
	}
	
	public SelectOption(String value, String text, boolean selected) {
		this.value = value;
		this.text = text;
		this.selected = selected;
	}
	
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * @return the parentValue
	 */
	public String getParentValue() {
		return parentValue;
	}

	/**
	 * @param parentValue the parentValue to set
	 */
	public void setParentValue(String parentValue) {
		this.parentValue = parentValue;
	}

	public String getLinkkey() {
		return linkkey;
	}
	public void setLinkkey(String linkkey) {
		this.linkkey = linkkey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SelectOption other = (SelectOption) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
}
