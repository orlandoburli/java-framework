package br.com.orlandoburli.framework.core.be.exceptions;

public abstract class BeException extends Exception {

	private static final long serialVersionUID = 1L;

	public BeException(String message) {
		super(message);
	}
}
