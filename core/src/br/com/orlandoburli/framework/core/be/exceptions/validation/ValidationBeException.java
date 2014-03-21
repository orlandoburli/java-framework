package br.com.orlandoburli.framework.core.be.exceptions.validation;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;

public class ValidationBeException extends BeException {

	private static final long serialVersionUID = 1L;

	public ValidationBeException(String message) {
		super(message);
	}

}
