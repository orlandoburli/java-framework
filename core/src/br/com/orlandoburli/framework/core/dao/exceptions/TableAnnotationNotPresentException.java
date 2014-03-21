package br.com.orlandoburli.framework.core.dao.exceptions;

public class TableAnnotationNotPresentException extends DAOException {

	private static final long serialVersionUID = 1L;
	private Class<?> classe;

	public TableAnnotationNotPresentException(String message, Class<?> classe) {
		super(message);
		this.setClasse(classe);
	}

	public Class<?> getClasse() {
		return classe;
	}

	private void setClasse(Class<?> classe) {
		this.classe = classe;
	}

}
