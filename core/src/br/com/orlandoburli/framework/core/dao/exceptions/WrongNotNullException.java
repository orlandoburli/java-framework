package br.com.orlandoburli.framework.core.dao.exceptions;

import java.lang.reflect.Field;

import br.com.orlandoburli.framework.core.dao.annotations.Column;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class WrongNotNullException extends DAOException {

	private static final long serialVersionUID = 1L;

	private Column column;
	private Field field;

	public WrongNotNullException(String message, Class<BaseVo> classe, Column column, Field field) {
		super(message);
		this.column = column;
		this.field = field;
	}

	public Column getColumn() {
		return this.column;
	}

	public Field getField() {
		return this.field;
	}
}
