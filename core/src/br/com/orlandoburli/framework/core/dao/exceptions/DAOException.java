package br.com.orlandoburli.framework.core.dao.exceptions;

public abstract class DAOException extends Exception {

	private static final long serialVersionUID = 1L;

	public DAOException(String message) {
		super(message);
	}

}
