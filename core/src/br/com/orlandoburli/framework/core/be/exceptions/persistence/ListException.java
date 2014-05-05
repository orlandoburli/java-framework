package br.com.orlandoburli.framework.core.be.exceptions.persistence;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;

public class ListException extends BeException {

	private static final long serialVersionUID = 1L;

	public ListException(String message) {
		super(message);
	}
	
	public ListException() {
		super("Erro ao retornar dados. Consulte o administrador do sistema.");
	}

}
