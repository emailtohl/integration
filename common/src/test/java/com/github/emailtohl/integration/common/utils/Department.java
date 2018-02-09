package com.github.emailtohl.integration.common.utils;

import com.github.emailtohl.integration.common.utils.Node;

class Department implements Node {
	String name;
	Department parent;
	
	public Department(String name, Department parent) {
		super();
		this.name = name;
		this.parent = parent;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getKey() {
		return name;
	}

	@Override
	public Node getParent() {
		return parent;
	}

}
