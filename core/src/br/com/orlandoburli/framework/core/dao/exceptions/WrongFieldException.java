package br.com.orlandoburli.framework.core.dao.exceptions;

import br.com.orlandoburli.framework.core.dao.annotations.Column;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class WrongFieldException extends DAOException {

	private static final long serialVersionUID = 1L;

	private Column column;
	private Class<BaseVo> classe;

	public WrongFieldException(String message, Class<BaseVo> classe, Column column) {
		super(message);
		this.setClasse(classe);
		this.setColumn(column);
	}

	public Column getColumn() {
		return column;
	}

	private void setColumn(Column column) {
		this.column = column;
	}

	public Class<BaseVo> getClasse() {
		return classe;
	}

	private void setClasse(Class<BaseVo> classe) {
		this.classe = classe;
	}

}
