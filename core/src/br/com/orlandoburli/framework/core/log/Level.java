package br.com.orlandoburli.framework.core.log;

public enum Level {

	FINE(6, "FINE"), DEBUG(5, "DEBUG"),  DEBUG_SQL(5, "DEBUG_SQL"), INFO(4, "INFO"), WARNING(3, "WARNING"), ERROR(2, "ERROR"), CRITICAL(1, "CRITICAL");

	private int level;
	private String description;

	Level(int level, String description) {
		this.level = level;
		this.description = description;
	}

	public int getLevel() {
		return this.level;
	}

	public String getDescription() {
		return description;
	}
}
