package br.com.orlandoburli.framework.core.dao.exceptions;

import java.sql.SQLException;

public class SQLDaoException extends DAOException {

	private static final long serialVersionUID = 1L;

	private SQLException innerException;

	public SQLDaoException(String message, SQLException innerException) {
		super(message);
		innerException.printStackTrace();
		setInnerException(innerException);
	}

	public SQLException getInnerException() {
		return innerException;
	}

	private void setInnerException(SQLException innerException) {
		this.innerException = innerException;
	}

}
