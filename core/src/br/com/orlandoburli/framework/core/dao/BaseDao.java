package br.com.orlandoburli.framework.core.dao;

import br.com.orlandoburli.framework.core.dao.builder.SQLBuilder;

public abstract class BaseDao {
	
	private SQLBuilder builder;

	private DAOManager manager;

	public BaseDao(DAOManager manager) {
		setManager(manager);
		setBuilder();
	}

	private void setBuilder() {
		Class<?> builderClass;
		try {
			builderClass = Class.forName(System.getProperty("sql.builder.class"));
			builder = (SQLBuilder) builderClass.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public SQLBuilder getBuilder() {
		return this.builder;
	}

	public DAOManager getManager() {
		return manager;
	}

	private void setManager(DAOManager manager) {
		this.manager = manager;
	}
}
