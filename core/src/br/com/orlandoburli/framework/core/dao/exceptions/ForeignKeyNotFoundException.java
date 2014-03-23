package br.com.orlandoburli.framework.core.dao.exceptions;

import br.com.orlandoburli.framework.core.dao.annotations.Join;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class ForeignKeyNotFoundException extends DAOException {

	private static final long serialVersionUID = 1L;
	private Class<BaseVo> classe;
	private Join join;

	public ForeignKeyNotFoundException(String message, Class<BaseVo> classe, Join join) {
		super(message);
		this.setClasse(classe);
		this.setJoin(join);
	}

	public Class<BaseVo> getClasse() {
		return classe;
	}

	private void setClasse(Class<BaseVo> classe) {
		this.classe = classe;
	}

	public Join getJoin() {
		return join;
	}

	private void setJoin(Join join) {
		this.join = join;
	}

}
