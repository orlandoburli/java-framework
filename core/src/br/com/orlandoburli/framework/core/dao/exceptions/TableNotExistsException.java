package br.com.orlandoburli.framework.core.dao.exceptions;

import br.com.orlandoburli.framework.core.dao.annotations.Table;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class TableNotExistsException extends DAOException {

	private static final long serialVersionUID = 1L;
	
	private Class<BaseVo> classe;

	public TableNotExistsException(Class<BaseVo> classe) {
		super("Tabela " + getTableName(classe) + " nao encontrada!");
		this.classe = classe;
	}

	private static String getTableName(Class<BaseVo> classe) {
		Table table = classe.getAnnotation(Table.class);
		if (table != null && table.value() != null && !table.value().trim().equals("")) {
			return table.value();
		} else {
			return classe.getSimpleName();
		}
	}
	
	public Class<BaseVo> getClasse() {
		return this.classe;
	}

}
