package br.com.orlandoburli.framework.core.dao.annotations;

/**
 * Join que indica quando o join sera realizado, se sempre (ALWAYS) ou apenas
 * quando solicitado (REQUESTED).
 * 
 * O Tipo MANUAL nunca fará join.
 * 
 * @author orlandoburli
 * 
 */
public enum JoinWhen {
	ALWAYS, @Deprecated
	REQUESTED, MANUAL
}
