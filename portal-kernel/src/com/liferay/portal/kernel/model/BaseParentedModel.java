package com.liferay.portal.kernel.model;

public interface BaseParentedModel {
	public String getParentClassName();

	public String getParentClassPK();

	public String getTreePath();
}
