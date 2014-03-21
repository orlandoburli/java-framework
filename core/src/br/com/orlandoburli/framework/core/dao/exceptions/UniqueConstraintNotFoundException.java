package br.com.orlandoburli.framework.core.dao.exceptions;

import br.com.orlandoburli.framework.core.dao.annotations.UniqueConstraint;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class UniqueConstraintNotFoundException extends DAOException {

	private static final long serialVersionUID = 1L;

	private UniqueConstraint constraint;
	private Class<BaseVo> classe;

	public UniqueConstraintNotFoundException(String message, Class<BaseVo> classe, UniqueConstraint constraint) {
		super(message);
		this.setConstraint(constraint);
		this.setClasse(classe);
	}

	public UniqueConstraint getConstraint() {
		return constraint;
	}

	private void setConstraint(UniqueConstraint constraint) {
		this.constraint = constraint;
	}

	public Class<BaseVo> getClasse() {
		return classe;
	}

	private void setClasse(Class<BaseVo> classe) {
		this.classe = classe;
	}

}
