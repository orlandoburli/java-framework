package br.com.orlandoburli.framework.core.dao.exceptions;

import br.com.orlandoburli.framework.core.vo.BaseVo;

public class SequenceNotExistsException extends DAOException {

	private static final long serialVersionUID = 1L;
	
	public SequenceNotExistsException(Class<BaseVo> classe) {
		super("Sequence  " + getSequenceName(classe) + " nao encontrada!");
	}

	private static String getSequenceName(Class<BaseVo> classe) {
		return null;
	}

}
