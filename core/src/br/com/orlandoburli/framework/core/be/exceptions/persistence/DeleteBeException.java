package br.com.orlandoburli.framework.core.be.exceptions.persistence;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;

public class DeleteBeException extends BeException {

	private static final long serialVersionUID = 1L;

	public DeleteBeException(String message, String field) {
		super(message, field);
	}

}
