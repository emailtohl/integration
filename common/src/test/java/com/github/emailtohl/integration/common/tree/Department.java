package com.github.emailtohl.integration.common.tree;

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
