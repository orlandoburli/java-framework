package br.com.orlandoburli.framework.core.dao.builder;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import br.com.orlandoburli.framework.core.dao.DAOManager;
import br.com.orlandoburli.framework.core.dao.DaoControle;
import br.com.orlandoburli.framework.core.dao.annotations.Column;
import br.com.orlandoburli.framework.core.dao.annotations.Join;
import br.com.orlandoburli.framework.core.dao.annotations.Table;
import br.com.orlandoburli.framework.core.dao.annotations.UniqueConstraint;
import br.com.orlandoburli.framework.core.dao.exceptions.DAOException;
import br.com.orlandoburli.framework.core.dao.exceptions.SQLDaoException;
import br.com.orlandoburli.framework.core.dao.exceptions.TableAnnotationNotPresentException;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public abstract class SQLBuilder {

	public static final int VARCHAR_SIZE_DEFAULT = 2000;

	public static final int CHAR_SIZE_DEFAULT = 10;

	public static final int NUMERIC_DEFAULT_SIZE = 20;

	public abstract String buildSqlInsertStatement(Class<BaseVo> classe) throws DAOException;

	public abstract String buildSqlNextSequence(Class<BaseVo> classe);

	public abstract String buildSqlUpdateStatement(Class<BaseVo> classe) throws DAOException;

	public abstract String buildSqlDeleteStatement(Class<BaseVo> classe) throws DAOException;

	public abstract String buildSqlSelectStatement(Class<BaseVo> classe, int maxSubJoins) throws DAOException;

	public abstract String buildSqlCountStatement(Class<BaseVo> classe, int maxSubJoins) throws DAOException;

	public abstract String buildSqlOrderByStatement(String orderFields) throws DAOException;

	public abstract void buildSqlWhereStatement(StringBuilder sqlWhere, Class<BaseVo> classe, BaseVo filter, boolean keysOnly, String prefix, DaoControle controle) throws DAOException;

	public abstract String buildSpecialWhereConditions(Class<BaseVo> voClass, String whereCondition);

	public abstract String buildSqlLimit(String sql, Integer pageNumber, Integer pageSize);

	public abstract void tableExists(Class<BaseVo> classe, DAOManager manager) throws DAOException;

	public abstract void tableCheck(Class<BaseVo> classe, DAOManager manager) throws DAOException;

	public abstract void constraintsCheck(Class<BaseVo> classe, DAOManager manager) throws DAOException;

	public abstract void foreignKeysCheck(Class<BaseVo> classe, DAOManager manager) throws DAOException;

	public abstract void createForeignKey(Class<BaseVo> voClass, Join join, Field field, DAOManager manager) throws DAOException;

	public abstract void createUniqueConstraint(Class<BaseVo> classe, UniqueConstraint constraint, DAOManager manager) throws DAOException;

	public abstract void alterTable(Class<BaseVo> voClass, DAOManager manager, DAOException e) throws DAOException;

	public abstract void createTable(Class<BaseVo> classe, DAOManager manager) throws DAOException;

	public abstract void sequenceExists(Class<BaseVo> classe, DAOManager manager) throws DAOException;

	public abstract void createSequence(Class<BaseVo> classe, DAOManager manager) throws DAOException;

	public abstract void dropTable(Class<BaseVo> classe, DAOManager manager) throws DAOException;

	public abstract void dropSequence(Class<BaseVo> classe, DAOManager manager) throws DAOException;

	public Column getColumn(Field f) {
		Column c = f.getAnnotation(Column.class);
		return c;
	}

	public Column getColumn(String columnName, Class<BaseVo> classe) {
		for (Field f : classe.getDeclaredFields()) {
			Column c = f.getAnnotation(Column.class);
			if (c != null && c.name().equalsIgnoreCase(columnName)) {
				return c;
			}
		}

		return null;
	}

	public Field getField(String columnName, Class<BaseVo> classe) {
		for (Field f : classe.getDeclaredFields()) {
			if (f.getName().equalsIgnoreCase(columnName)) {
				return f;
			}
		}
		return null;
	}

	public String getColumnName(Field f) {
		Column c = getColumn(f);

		if (c != null) {
			if (c.name() != null && !c.name().equals("")) {
				return c.name();
			} else if (c.value() != null && !c.value().equals("")) {
				return c.value();
			}
		}

		return f.getName();
	}

	public String getTablename(Class<?> classe) throws DAOException {
		Table t = getTable(classe);

		if (t != null) {
			if (t.value() != null && !t.value().trim().equals("")) {
				return t.value();
			}
		}

		throw new TableAnnotationNotPresentException("Anotação @Table não encontrada em " + classe.getSimpleName(), classe);
		// return null; //classe.getSimpleName().substring(0,
		// classe.getSimpleName().length() - 2);
	}

	public String getSequenceName(Class<?> classe) {

		Field[] fields = classe.getDeclaredFields();

		for (Field f : fields) {
			Column c = getColumn(f);
			if (c != null) {

				if (c.isAutoIncrement()) {
					if (c.sequenceName() != null && !c.sequenceName().trim().equals("")) {
						return c.sequenceName();
					} else {
						// Se o nome da sequence nao for definida, assume valor
						// padrao.
						return "seq_" + getColumnName(f);
					}
				}
			}
		}

		return null;
	}

	private Table getTable(Class<?> classe) {
		return classe.getAnnotation(Table.class);
	}

	public Join getJoin(Field f) {
		return f.getAnnotation(Join.class);
	}

	public String getJoinFieldName(Class<?> classe, Field f) throws DAOException {
		return this.getTablename(classe) + "." + this.getColumnName(f);
	}

	public String getJoinFieldAlias(Class<?> classe, Field f) throws DAOException {
		return this.getTablename(classe) + "_" + this.getColumnName(f);
	}

	public abstract String getColumnDeclaration(Column column);

	protected Integer hashString(String valor) {
		Integer resultado = 0;

		for (int i = 0; i < valor.length(); i++) {
			resultado += valor.charAt(i);
		}

		return resultado;
	}

	public abstract void setInsertParameters(PreparedStatement prepared, BaseVo vo, Class<BaseVo> classe, Integer auto) throws SQLException, SQLDaoException;

	public abstract void setSelectWhereParameters(PreparedStatement prepared, BaseVo vo, Class<BaseVo> classe, boolean keysOnly, String prefix, DaoControle controle, DaoControle posicao) throws SQLException, DAOException;

	public abstract void setUpdateParameters(PreparedStatement prepared, BaseVo vo, Class<BaseVo> classe) throws SQLException;

	public abstract void setDeleteParameters(PreparedStatement prepared, BaseVo vo, Class<BaseVo> classe) throws SQLException;

	public abstract void resultToVo(BaseVo vo, ResultSet result, String prefix, DaoControle controle) throws DAOException;
}
