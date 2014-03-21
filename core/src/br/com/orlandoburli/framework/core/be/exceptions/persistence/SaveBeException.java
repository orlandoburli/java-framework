package br.com.orlandoburli.framework.core.be.exceptions.persistence;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;

public class SaveBeException extends BeException {

	private static final long serialVersionUID = 1L;

	public SaveBeException(String message) {
		super(message);
	}

}
