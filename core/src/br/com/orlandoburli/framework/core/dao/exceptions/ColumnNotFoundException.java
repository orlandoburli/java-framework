package br.com.orlandoburli.framework.core.dao.exceptions;

import java.lang.reflect.Field;

import br.com.orlandoburli.framework.core.dao.annotations.Column;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class ColumnNotFoundException extends DAOException {

	private static final long serialVersionUID = 1L;
	private Column column;
	private Class<BaseVo> classe;
	private Field field;
	
	public ColumnNotFoundException(String message, Class<BaseVo> classe, Column column, Field field) {
		super(message);
		this.setField(field);
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

	public Field getField() {
		return field;
	}

	private void setField(Field field) {
		this.field = field;
	}

}
