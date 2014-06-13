package br.com.orlandoburli.framework.core.be.exceptions;

public abstract class BeException extends Exception {

	private static final long serialVersionUID = 1L;
	private String field;

	public BeException(String message, String field) {
		super(message);
		this.setField(field);
	}

	public String getField() {
		return field;
	}

	private void setField(String field) {
		this.field = field;
	}

}
