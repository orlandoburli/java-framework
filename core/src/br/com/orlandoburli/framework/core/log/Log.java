package br.com.orlandoburli.framework.core.log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public final class Log {

	public static void fine(Object message) {
		log(Level.FINE, message);
	}

	public static void debug(Object message) {
		log(Level.DEBUG, message);
	}

	public static void debugsql(Object message) {
		log(Level.DEBUG_SQL, message);
	}

	public static void info(Object message) {
		log(Level.INFO, message);
	}

	public static void warning(Object message) {
		log(Level.WARNING, message);
	}

	public static void error(Object message) {
		log(Level.ERROR, message);
	}

	public static void critical(Object message) {
		log(Level.CRITICAL, message);
	}

	public static void log(Level level, Object message) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		int linha = 3;
		int levelMin = Integer.parseInt(System.getProperty("log.level"));

		if (level.getLevel() <= levelMin) {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss:SSS");
			System.out.println(sdf.format(cal.getTime()) + "| " + stackTrace[linha].getClassName() + " (" + stackTrace[linha].getLineNumber() + ") |" + level.getLevel() + "|" + level.getDescription() + "|" + message);
		}
	}
}
