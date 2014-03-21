package br.com.orlandoburli.treinamento.core.dao.postgresql;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.orlandoburli.framework.core.dao.DAOManager;
import br.com.orlandoburli.framework.core.dao.DaoControle;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.dao.annotations.Column;
import br.com.orlandoburli.framework.core.dao.annotations.DataType;
import br.com.orlandoburli.framework.core.dao.annotations.Join;
import br.com.orlandoburli.framework.core.dao.annotations.JoinType;
import br.com.orlandoburli.framework.core.dao.annotations.Table;
import br.com.orlandoburli.framework.core.dao.annotations.UniqueConstraint;
import br.com.orlandoburli.framework.core.dao.builder.SQLBuilder;
import br.com.orlandoburli.framework.core.dao.exceptions.ColumnNotFoundException;
import br.com.orlandoburli.framework.core.dao.exceptions.DAOException;
import br.com.orlandoburli.framework.core.dao.exceptions.SQLDaoException;
import br.com.orlandoburli.framework.core.dao.exceptions.SequenceNotExistsException;
import br.com.orlandoburli.framework.core.dao.exceptions.StatementNotExecutedException;
import br.com.orlandoburli.framework.core.dao.exceptions.TableNotExistsException;
import br.com.orlandoburli.framework.core.dao.exceptions.UniqueConstraintNotFoundException;
import br.com.orlandoburli.framework.core.dao.exceptions.WrongColumnException;
import br.com.orlandoburli.framework.core.dao.exceptions.WrongFieldException;
import br.com.orlandoburli.framework.core.dao.exceptions.WrongJoinException;
import br.com.orlandoburli.framework.core.dao.exceptions.WrongNotNullException;
import br.com.orlandoburli.framework.core.log.Log;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class PostgresSQLBuilder extends SQLBuilder {

	@Override
	public String buildSqlInsertStatement(Class<BaseVo> classe) throws DAOException {

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + getTablename(classe) + " (");

		Field[] fields = classe.getDeclaredFields();

		for (Field f : fields) {
			if (getColumn(f) != null) {
				sql.append(this.getColumnName(f) + ", ");
			}
		}

		sql.delete(sql.length() - 2, sql.length());

		sql.append(") VALUES (");

		for (Field f : fields) {
			if (getColumn(f) != null) {
				sql.append("?, ");
			}
		}

		sql.delete(sql.length() - 2, sql.length());

		sql.append(")");

		return sql.toString();
	}

	@Override
	public String buildSqlNextSequence(Class<BaseVo> classe) {
		StringBuilder sb = new StringBuilder("SELECT nextval('" + getSequenceName(classe) + "')");
		return sb.toString();
	}

	@Override
	public String buildSqlUpdateStatement(Class<BaseVo> classe) throws DAOException {

		StringBuilder sql = new StringBuilder();
		sql.append("\n UPDATE " + getTablename(classe) + " SET ");

		Field[] fields = classe.getDeclaredFields();

		for (Field f : fields) {
			Column column = getColumn(f);
			if (column != null && !column.isKey()) {
				sql.append("\n        " + this.getColumnName(f) + " = ?, ");
			}
		}

		sql.delete(sql.length() - 2, sql.length());

		buildSqlWhereStatement(sql, classe, null, true, getTablename(classe), new DaoControle(0));

		return sql.toString();
	}

	@Override
	public String buildSqlDeleteStatement(Class<BaseVo> classe) throws DAOException {

		StringBuilder sql = new StringBuilder();
		sql.append("\nDELETE FROM " + getTablename(classe) + " ");

		buildSqlWhereStatement(sql, classe, null, true, getTablename(classe), new DaoControle(0));

		return sql.toString();
	}

	@Override
	public String buildSqlSelectStatement(Class<BaseVo> classe, int maxSubJoins) throws DAOException {

		StringBuilder sql = new StringBuilder();
		sql.append("\n SELECT");

		String tablename = getTablename(classe);

		Field[] fields = classe.getDeclaredFields();

		buildSelectFields(classe, sql, fields, tablename, new DaoControle(maxSubJoins));

		// Remove a virgula e o espaco do final
		sql.delete(sql.length() - 2, sql.length());

		sql.append("\n\n   FROM " + tablename);

		// Loop dos Joins Encontrados
		List<String> buff = new ArrayList<String>();

		buidlSelectJoins(sql, fields, tablename, buff, tablename + "_", tablename, new DaoControle(maxSubJoins));

		return sql.toString();
	}

	@SuppressWarnings("unchecked")
	public void buildSelectFields(Class<BaseVo> classe, StringBuilder sql, Field[] fields, String prefix, DaoControle controle) throws DAOException {
		for (Field f : fields) {
			Join join = getJoin(f);
			Column column = getColumn(f);

			if (column != null) {
				String prefixColumn = prefix + "_";

				sql.append("\n        " + prefix + "." + this.getColumnName(f) + " AS " + prefixColumn + this.getColumnName(f) + ", ");
			} else if (join != null) {

				if (f.getType().getSuperclass().equals(BaseVo.class)) {
					// Se o field for do tipo VO, ira fazer select de todas as
					// suas colunas.
					Field[] fieldsJoin = f.getType().getDeclaredFields();

					// sql.append("\n");

					String prefix2 = join.tableAlias().equals("") ? getTablename(f.getType()) : join.tableAlias();
					prefix2 += "";

					if (!controle.isMaximo()) {
						controle.incrementaInteracoes();
						buildSelectFields((Class<BaseVo>) f.getType(), sql, fieldsJoin, prefix + "_" + prefix2, controle);
					}
				} else {
					// Join de uma coluna so
					if (join.fieldRemote() == null || join.fieldRemote().trim().equals("")) {
						throw new WrongJoinException("Nome do fieldRemote do atributo " + f.getName() + " da classe " + classe.getSimpleName() + " não especificado!", classe, join);
					}

					if (join.tableRemote() == null || join.tableRemote().trim().equals("")) {
						throw new WrongJoinException("Nome do tableRemote do atributo " + f.getName() + " da classe " + classe.getSimpleName() + " não especificado!", classe, join);
					}

					sql.append("\n        " + join.tableRemote() + "." + join.fieldRemote() + " as " + join.tableRemote() + "_" + join.fieldRemote() + ", ");
				}
			}
		}
	}

	public void buidlSelectJoins(StringBuilder sql, Field[] fields, String tablename, List<String> buff, String prefix, String tableOrigin, DaoControle controle) throws SecurityException, DAOException {
		for (Field f : fields) {
			Join join = getJoin(f);

			if (join != null) {
				String tableJoin = null;

				if (f.getType().getSuperclass().equals(BaseVo.class)) {
					tableJoin = getTablename(f.getType());
				} else {
					tableJoin = join.tableRemote();
				}

				String tableAlias = "";
				if (!join.tableAlias().trim().equals("")) {
					tableAlias = join.tableAlias();
				} else {
					tableAlias = tableJoin;
				}

				boolean isInBuffer = false;
				// Verifica se esta tabela ja esta no join
				for (String s : buff) {
					if (s.equals(prefix + tableAlias)) {
						isInBuffer = true;
						break;
					}
				}

				if (!isInBuffer) {

					buff.add(prefix + tableAlias);

					sql.append("\n  " + getJoinType(join) + " " + tableJoin + " " + prefix + tableAlias);
					boolean first = true;

					for (int i = 0; i < join.columnsLocal().length; i++) {
						sql.append("\n    " + (first ? " ON" : "AND") + " " + prefix + tableAlias + "." + join.columnsRemote()[i] + " = " + tableOrigin + "." + join.columnsLocal()[i]);
					}
				}

				if (f.getType().getSuperclass().equals(BaseVo.class)) {
					if (!controle.isMaximo()) {
						controle.incrementaInteracoes();
						buidlSelectJoins(sql, f.getType().getDeclaredFields(), tableJoin, buff, prefix + tableAlias + "_", prefix + tableAlias, controle);
					}
				}
			}
		}
	}

	private String getJoinType(Join join) {
		if (join == null) {
			return null;
		}

		if (join.joinType() == JoinType.INNER) {
			return "INNER JOIN";
		} else if (join.joinType() == JoinType.LEFT) {
			return " LEFT JOIN";
		} else if (join.joinType() == JoinType.RIGHT) {
			return "RIGHT JOIN";
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void buildSqlWhereStatement(StringBuilder sqlWhere, Class<BaseVo> classe, BaseVo filter, boolean keysOnly, String prefix, DaoControle controle) throws DAOException {
		// Verifica se ja nao tem a condicao WHERE
		if (sqlWhere.indexOf("WHERE 1=1") < 0) {
			sqlWhere.append("\n  WHERE 1=1");
		}

		if (filter != null || keysOnly) {

			Field[] fields = classe.getDeclaredFields();

			for (Field f : fields) {
				Column column = getColumn(f);

				if ((column != null && !keysOnly) || (keysOnly && column != null && column.isKey())) {

					Method getter = DaoUtils.getGetterMethod(classe, f);

					Object value = null;

					if (filter != null) {
						value = DaoUtils.getValue(getter, filter);
					}

					if (value != null || keysOnly) {

						if (f.getType().equals(String.class)) {
							// Apenas o tipo STRING muda o filtro para LIKE
							sqlWhere.append("\n    AND " + prefix + "." + this.getColumnName(f) + " ILIKE ? ");
						} else {
							sqlWhere.append("\n    AND " + prefix + "." + this.getColumnName(f) + " = ? ");
						}
					}
				}

				// Filtro recursivo
				Join join = f.getAnnotation(Join.class);

				if (!keysOnly && join != null) {
					Method getter = DaoUtils.getGetterMethod(classe, f);

					Object value = null;

					value = DaoUtils.getValue(getter, filter);

					if (value != null && !controle.isMaximo()) {
						controle.incrementaInteracoes();

						String prefix2 = join.tableAlias().equals("") ? getTablename(f.getType()) : join.tableAlias();

						buildSqlWhereStatement(sqlWhere, (Class<BaseVo>) f.getType(), (BaseVo) value, false, prefix + "_" + prefix2, controle);
					}
				}
			}
		}
	}

	@Override
	public void tableExists(Class<BaseVo> classe, DAOManager manager) throws DAOException {
		String tablename = getTablename(classe);

		Log.debug("Checando se a tabela " + tablename + " existe");

		try {

			ResultSet result = manager.getConnection().getMetaData().getTables(null, null, tablename, null);

			if (!result.next()) {
				throw new TableNotExistsException(classe);
			}
		} catch (SQLException e) {
			throw new SQLDaoException("Erro ao retornar metadados", e);
		}
	}

	@Override
	public void sequenceExists(Class<BaseVo> classe, DAOManager manager) throws DAOException {
		try {

			String sequenceName = getSequenceName(classe);
			if (sequenceName == null) {
				return;
			}
			PreparedStatement statement = manager.getConnection().prepareStatement("SELECT * FROM information_schema.sequences u WHERE u.sequence_name = '" + sequenceName + "'");
			ResultSet result = statement.executeQuery();

			if (!result.next()) {
				throw new SequenceNotExistsException(classe);
			}
		} catch (SQLException e) {
			throw new SQLDaoException("Erro ao retornar metadados", e);
		}
	}

	@Override
	public void tableCheck(Class<BaseVo> classe, DAOManager manager) throws DAOException {
		String tablename = getTablename(classe);

		Log.debug("Checando os campos da tabela " + tablename);

		try {

			for (Field f : classe.getDeclaredFields()) {

				// Busca a coluna na classe
				Column column = getColumn(f);

				String columnName = getColumnName(f);

				if (column != null) {

					ResultSet result = manager.getConnection().getMetaData().getColumns(null, null, tablename, columnName);

					if (result.next()) {

						int dataType = result.getInt("DATA_TYPE");
						int columnSize = result.getInt("COLUMN_SIZE");
						int columnPrecision = result.getInt("DECIMAL_DIGITS");

						String isNullable = result.getString("IS_NULLABLE");

						// Checa o tipo da coluna
						if (column.dataType() == DataType.VARCHAR) {
							if (dataType != java.sql.Types.VARCHAR) {
								throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tipo errado! VO: " + column.dataType() + ", BD: " + dataType, classe, column, f);
							} else {
								// Checa o tamanho, nao pode ser menor, pode ser
								// maior ou igual.
								if (columnSize < column.maxSize()) {
									throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tamanho errado! VO: " + column.maxSize() + ", BD: " + columnSize, classe, column, f);
								}
							}

							// Checa o tipo de dados do VO, se é compativel com
							// o tipo de dados da coluna
							if (!f.getType().equals(String.class)) {
								throw new WrongFieldException("Field " + f.getName() + " do tipo errado, esperado: " + String.class.getName() + ", encontrado: " + f.getType().getName() + ", dataType: " + column.dataType(), classe, column);
							}
						} else if (column.dataType() == DataType.CHAR) {
							if (dataType != java.sql.Types.VARCHAR && dataType != java.sql.Types.CHAR) {
								throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tipo errado! VO: " + column.dataType() + ", BD: " + dataType, classe, column, f);
							} else {
								// Checa o tamanho, nao pode ser menor, pode ser
								// maior ou igual.
								if (columnSize < column.maxSize()) {
									throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tamanho errado! VO: " + column.maxSize() + ", BD: " + columnSize, classe, column, f);
								}
							}

							// Checa o tipo de dados do VO, se é compativel com
							// o tipo de dados da coluna
							if (!f.getType().equals(String.class)) {
								throw new WrongFieldException("Field " + f.getName() + " do tipo errado, esperado: " + String.class.getName() + ", encontrado: " + f.getType().getName() + ", dataType: " + column.dataType(), classe, column);
							}
						} else if (column.dataType() == DataType.DOMAIN_STRING) {
							if (dataType != java.sql.Types.VARCHAR && dataType != java.sql.Types.CHAR) {
								throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tipo errado! VO: " + column.dataType() + ", BD: " + dataType, classe, column, f);
							} else {
								// Checa o tamanho, nao pode ser menor, pode ser
								// maior ou igual.
								if (columnSize < column.maxSize()) {
									throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tamanho errado! VO: " + column.maxSize() + ", BD: " + columnSize, classe, column, f);
								}
							}

							// Checa o tipo de dados do VO, se é compativel com
							// o tipo de dados da coluna
							if (!f.getType().equals(String.class)) {
								throw new WrongFieldException("Field " + f.getName() + " do tipo errado, esperado: " + String.class.getName() + ", encontrado: " + f.getType().getName() + ", dataType: " + column.dataType(), classe, column);
							}
						} else if (column.dataType() == DataType.DOMAIN_NUMERIC) {
							if (dataType != java.sql.Types.INTEGER && dataType != java.sql.Types.NUMERIC) {
								throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tipo errado! VO: " + column.dataType() + ", BD: " + dataType, classe, column, f);
							} else {
								// Checa o tamanho, nao pode ser menor, pode ser
								// maior ou igual.
								if (columnSize < column.maxSize()) {
									throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tamanho errado! VO: " + column.maxSize() + ", BD: " + columnSize, classe, column, f);
								}

								// Checa a precisao, nao pode ser menor, pode
								// ser maior ou igual.
								if (columnPrecision < column.precision()) {
									throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " de precisao errada! VO: " + column.precision() + ", BD: " + columnPrecision, classe, column, f);
								}
							}

							// Checa o tipo de dados do VO, se é compativel com
							// o tipo de dados da coluna
							if (!f.getType().equals(Integer.class)) {
								throw new WrongFieldException("Field " + f.getName() + " do tipo errado, esperado: " + Integer.class.getName() + ", encontrado: " + f.getType().getName() + ", dataType: " + column.dataType(), classe, column);
							}
						} else if (column.dataType() == DataType.INT) {
							if (dataType != java.sql.Types.INTEGER && dataType != java.sql.Types.NUMERIC && dataType != java.sql.Types.DOUBLE) {
								throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tipo errado! VO: " + column.dataType() + ", BD: " + dataType, classe, column, f);
							} else {
								// Checa o tamanho, nao pode ser menor, pode ser
								// maior ou igual.
								if (columnSize < column.maxSize()) {
									throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tamanho errado! VO: " + column.maxSize() + ", BD: " + columnSize, classe, column, f);
								}

								// Checa a precisao, nao pode ser menor, pode
								// ser maior ou igual.
								if (columnPrecision < column.precision()) {
									throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " de precisao errada! VO: " + column.precision() + ", BD: " + columnPrecision, classe, column, f);
								}
							}

							// Checa o tipo de dados do VO, se é compativel com
							// o tipo de dados da coluna
							if (!f.getType().equals(Integer.class)) {
								throw new WrongFieldException("Field " + f.getName() + " do tipo errado, esperado: " + Integer.class.getName() + ", encontrado: " + f.getType().getName() + ", dataType: " + column.dataType(), classe, column);
							}
						} else if (column.dataType() == DataType.NUMERIC) {
							if (dataType != java.sql.Types.NUMERIC) {
								throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tipo errado! VO: " + column.dataType() + ", BD: " + dataType, classe, column, f);
							}

							// Checa o tipo de dados do VO, se é compativel com
							// o tipo de dados da coluna
							if (!f.getType().equals(BigDecimal.class)) {
								throw new WrongFieldException("Field " + f.getName() + " do tipo errado, esperado: " + BigDecimal.class.getName() + ", encontrado: " + f.getType().getName() + ", dataType: " + column.dataType(), classe, column);
							}
						} else if (column.dataType() == DataType.DATE) {
							if (dataType != java.sql.Types.DATE && dataType != java.sql.Types.TIMESTAMP) {
								throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tipo errado! VO: " + column.dataType() + ", BD: " + dataType, classe, column, f);
							}

							// Checa o tipo de dados do VO, se é compativel com
							// o tipo de dados da coluna
							if (!f.getType().equals(Calendar.class)) {
								throw new WrongFieldException("Field " + f.getName() + " do tipo errado, esperado: " + Calendar.class.getName() + ", encontrado: " + f.getType().getName() + ", dataType: " + column.dataType(), classe, column);
							}
						}

						// Checa a opcao Nullable
						if ((column.isNotNull() || column.isKey()) && isNullable.equalsIgnoreCase("YES")) {
							throw new WrongNotNullException("Coluna " + columnName + " da tabela " + tablename + " com opcao NULLABLE errada! VO: " + (column.isNotNull() || column.isKey()) + ", BD: " + isNullable, classe, column, f);
						} else if (!column.isNotNull() && !column.isKey() && isNullable.equalsIgnoreCase("NO")) {
							throw new WrongNotNullException("Coluna " + columnName + " da tabela " + tablename + " com opcao NULLABLE errada! VO: " + (column.isNotNull() || column.isKey()) + ", BD: " + isNullable, classe, column, f);
						}

					} else {
						// Coluna nao encontrada
						throw new ColumnNotFoundException("Coluna " + columnName + " da tabela " + tablename + " nao existente!", classe, column, f);
					}
				}
			}

		} catch (SQLException e) {
			throw new SQLDaoException("Erro ao retornar metadados", e);
		}

	}

	@Override
	public void createTable(Class<BaseVo> classe, DAOManager manager) throws DAOException {
		String tableName = getTablename(classe);

		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE " + tableName + " (");

		Field[] fields = classe.getDeclaredFields();

		String sqlPk = "";

		for (Field f : fields) {

			String columnName = getColumnName(f);
			Column column = getColumn(f);

			String columnType = "";

			if (column != null) {

				columnType = getColumnDeclaration(column);

				if (column.isKey()) {
					sqlPk += columnName + ", ";
				}

				sql.append(columnName + " " + columnType + (column.isNotNull() || column.isKey() ? " NOT NULL" : " NULL") + ", ");
			}

		}

		sql.delete(sql.length() - 2, sql.length());

		if (sqlPk != null && !sqlPk.trim().equals("")) {
			sqlPk = sqlPk.substring(0, sqlPk.length() - 2);
			sql.append(", CONSTRAINT PK_" + tableName + " PRIMARY KEY (" + sqlPk + ")");
		}

		sql.append(")");

		Log.debugsql(sql);

		try {
			CallableStatement statement = manager.getConnection().prepareCall(sql.toString());
			statement.execute();

			try {
				// Confirma se a tabela foi gerada corretamente.
				tableCheck(classe, manager);
			} catch (TableNotExistsException e) {
				throw new StatementNotExecutedException("Tabela [" + tableName + "] nao criada!");
			}
		} catch (SQLException e) {
			throw new SQLDaoException("Erro ao criar tabela [" + tableName + "]", e);
		}
	}

	@Override
	public String getColumnDeclaration(Column column) {
		String columnType = "";
		int columnSize = 0;

		if (column.dataType() == DataType.VARCHAR || column.dataType() == DataType.DOMAIN_STRING) {
			columnType = "VARCHAR";
		} else if (column.dataType() == DataType.CHAR) {
			columnType = "CHAR";
		} else if (column.dataType() == DataType.DATE) {
			columnType = "DATE";
		} else if (column.dataType() == DataType.DATETIME) {
			columnType = "TIMESTAMP";
		} else if (column.dataType() == DataType.INT || column.dataType() == DataType.DOMAIN_NUMERIC) {
			columnType = "INTEGER";
		} else if (column.dataType() == DataType.NUMERIC) {
			columnType = "NUMERIC";
		}

		if (column.maxSize() > 0) {
			columnSize = column.maxSize();
		} else {
			if (column.dataType() == DataType.VARCHAR) {
				columnSize = VARCHAR_SIZE_DEFAULT;
			} else if (column.dataType() == DataType.CHAR || column.dataType() == DataType.DOMAIN_STRING) {
				columnSize = CHAR_SIZE_DEFAULT;
			} else if (column.dataType() == DataType.NUMERIC) {
				columnSize = NUMERIC_DEFAULT_SIZE;
			}
		}

		if (column.precision() > 0) {
			columnType += " (" + columnSize + ", " + column.precision() + ") ";
		} else if (columnSize > 0) {
			columnType += " (" + columnSize + ") ";
		}

		return columnType;
	}

	@Override
	public void createSequence(Class<BaseVo> classe, DAOManager manager) throws DAOException {
		String sequenceName = getSequenceName(classe);

		try {
			if (sequenceName == null) {
				return;
			}
			String sql = "CREATE SEQUENCE " + sequenceName + "";

			Log.debugsql(sql);

			PreparedStatement statement = manager.getConnection().prepareStatement(sql);
			statement.execute();

		} catch (SQLException e) {
			throw new SQLDaoException("Erro ao criar sequence " + sequenceName, e);
		}
	}

	@Override
	public void alterTable(Class<BaseVo> classe, DAOManager manager, DAOException e) throws DAOException {
		String tableName = getTablename(classe);

		if (e instanceof WrongColumnException) {
			WrongColumnException e1 = (WrongColumnException) e;

			String columnName = getColumnName(e1.getField());
			String columnDeclaration = getColumnDeclaration(e1.getColumn());

			StringBuilder sql = new StringBuilder("ALTER TABLE " + tableName + " ");
			sql.append(" ALTER " + columnName + " TYPE " + columnDeclaration);

			Log.debugsql(sql);

			PreparedStatement statement;

			try {
				statement = manager.getConnection().prepareStatement(sql.toString());
				statement.execute();

			} catch (SQLException e2) {
				throw new SQLDaoException("Erro ao executar alteracao da tabela " + tableName + " coluna " + columnName + " " + columnDeclaration, e2);
			}

		} else if (e instanceof WrongNotNullException) {
			WrongNotNullException e1 = (WrongNotNullException) e;

			String columnName = getColumnName(e1.getField());
			String columnDeclaration = getColumnDeclaration(e1.getColumn());

			StringBuilder sql = new StringBuilder("ALTER TABLE " + tableName + " ");
			sql.append(" ALTER " + columnName + (e1.getColumn().isNotNull() || e1.getColumn().isKey() ? " SET NOT NULL" : " DROP NOT NULL"));

			Log.debugsql(sql);

			PreparedStatement statement;

			try {
				statement = manager.getConnection().prepareStatement(sql.toString());
				statement.execute();

			} catch (SQLException e2) {
				throw new SQLDaoException("Erro ao executar alteracao da tabela " + tableName + " coluna " + columnName + " " + columnDeclaration, e2);
			}
		} else if (e instanceof ColumnNotFoundException) {
			ColumnNotFoundException e1 = (ColumnNotFoundException) e;

			String columnName = getColumnName(e1.getField());
			String columnDeclaration = getColumnDeclaration(e1.getColumn());

			StringBuilder sql = new StringBuilder("ALTER TABLE " + tableName + " ");
			sql.append(" ADD " + columnName + " " + columnDeclaration + (e1.getColumn().isNotNull() || e1.getColumn().isKey() ? " NOT NULL" : " NULL"));

			Log.debugsql(sql);

			PreparedStatement statement;

			try {
				statement = manager.getConnection().prepareStatement(sql.toString());
				statement.execute();

			} catch (SQLException e2) {
				throw new SQLDaoException("Erro ao executar alteracao da tabela " + tableName + " coluna " + columnName + " " + columnDeclaration, e2);
			}

		} else if (e instanceof WrongFieldException) {
			// Nao tem o que fazer, devolve a excecao.
			throw e;
		} else {
			// Qualquer outra excecao nao tratada, devolve
			throw e;
		}
	}

	@Override
	public void dropTable(Class<BaseVo> classe, DAOManager manager) throws DAOException {
		String tablename = getTablename(classe);
		String sql = "DROP TABLE " + tablename + " CASCADE";

		Log.debugsql(sql);

		PreparedStatement statement;

		try {
			statement = manager.getConnection().prepareStatement(sql);
			statement.execute();

		} catch (SQLException e) {
			throw new SQLDaoException("Erro ao apagar tabela " + tablename, e);
		}
	}

	@Override
	public void dropSequence(Class<BaseVo> classe, DAOManager manager) throws DAOException {
		try {
			sequenceExists(classe, manager);
		} catch (SequenceNotExistsException e1) {
			return;
		}

		String sequenceName = getSequenceName(classe);

		try {
			if (sequenceName == null) {
				return;
			}
			String sql = "DROP SEQUENCE " + sequenceName + "";

			Log.debugsql(sql);

			PreparedStatement statement = manager.getConnection().prepareStatement(sql);
			statement.execute();

		} catch (SQLException e) {
			throw new SQLDaoException("Erro ao apagar sequence " + sequenceName, e);
		}
	}

	@Override
	public void constraintsCheck(Class<BaseVo> classe, DAOManager manager) throws DAOException {
		// Check das constraints
		// TODO Verificar se a constraint possui as chaves corretas

		Table table = classe.getAnnotation(Table.class);

		if (table == null) {
			return;
		}

		try {
			UniqueConstraint[] constraints = table.constraints();

			if (constraints != null) {
				for (UniqueConstraint constraint : constraints) {

					Log.info("Constraint : " + constraint.constraintName());

					ResultSet result = manager.getConnection().getMetaData().getIndexInfo(null, null, getTablename(classe), true, true);

					boolean found = false;

					while (result.next()) {
						if (result.getString("INDEX_NAME") != null && result.getString("INDEX_NAME").equalsIgnoreCase(constraint.constraintName())) {
							found = true;
							break;
						}
					}

					result.close();

					if (!found) {
						throw new UniqueConstraintNotFoundException("Constraint não encontrada!", classe, constraint);
					}
				}
			}

		} catch (SQLException e) {
			throw new SQLDaoException("Erro ao buscar informações sobre as constraints", e);
		}
	}

	@Override
	public void createUniqueConstraint(Class<BaseVo> classe, UniqueConstraint constraint, DAOManager manager) throws DAOException {
		// TODO Auto-generated method stub
		Table table = classe.getAnnotation(Table.class);

		if (table == null) {
			return;
		}

		try {
			String sql = "ALTER TABLE " + getTablename(classe) + " ADD CONSTRAINT " + constraint.constraintName() + " UNIQUE (";

			if (constraint.column() != null && !constraint.column().trim().equals("")) {
				sql += constraint.column() + ", ";
			}
			for (String c : constraint.columns()) {
				sql += c + ", ";
			}

			sql = sql.substring(0, sql.length() - 2);

			sql += ")";

			Log.debugsql(sql);

			PreparedStatement statement = manager.getConnection().prepareStatement(sql);
			statement.execute();

		} catch (SQLException e) {
			throw new SQLDaoException("Erro ao criar constraint", e);
		}

	}

}