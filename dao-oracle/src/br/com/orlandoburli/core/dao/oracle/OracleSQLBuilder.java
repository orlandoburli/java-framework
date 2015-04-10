package br.com.orlandoburli.core.dao.oracle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import br.com.orlandoburli.framework.core.dao.DAOManager;
import br.com.orlandoburli.framework.core.dao.DaoControle;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.dao.annotations.Column;
import br.com.orlandoburli.framework.core.dao.annotations.DataType;
import br.com.orlandoburli.framework.core.dao.annotations.Join;
import br.com.orlandoburli.framework.core.dao.annotations.JoinType;
import br.com.orlandoburli.framework.core.dao.annotations.JoinWhen;
import br.com.orlandoburli.framework.core.dao.annotations.Table;
import br.com.orlandoburli.framework.core.dao.annotations.UniqueConstraint;
import br.com.orlandoburli.framework.core.dao.builder.SQLBuilder;
import br.com.orlandoburli.framework.core.dao.exceptions.ColumnNotFoundException;
import br.com.orlandoburli.framework.core.dao.exceptions.DAOException;
import br.com.orlandoburli.framework.core.dao.exceptions.ForeignKeyNotFoundException;
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

public class OracleSQLBuilder extends SQLBuilder {

	/**
	 * Map das tabelas traduzidas <br/>
	 * <b>Tabela</b> - Objeto que tem a classe da tabela, o nome e o nick <br/>
	 * <b>Class<?></b> - Classe da tabela
	 */
	private HashMap<Class<?>, Tabela> tabelasMap;
	/**
	 * Map das colunas traduzidas <br/>
	 * <b>Field</b> - Objeto que tem o Field e o apelido dado no select <br/>
	 * <b>String</b> - Chave da coluna que será TABELA_COLUNA
	 */
	private HashMap<Field, Coluna> colunasMap;

	/**
	 * Usa para que nao se repita os nomes de tabelas usadas
	 */
	private List<String> apelidosTabelasUsadas;
	/**
	 * Usa para que nao se repita os nomes de colunas usadas
	 */
	private List<String> apelidosCamposUsadas;

	public OracleSQLBuilder() {
		this.tabelasMap = new HashMap<Class<?>, OracleSQLBuilder.Tabela>();
		this.colunasMap = new HashMap<Field, OracleSQLBuilder.Coluna>();

		this.apelidosCamposUsadas = new ArrayList<String>();
		this.apelidosTabelasUsadas = new ArrayList<String>();
	}

	protected class Tabela {

		private String tableName;
		private String nick;

		public Tabela(String tableName, String nick) {
			this.setTableName(tableName);
			this.setNick(nick);
		}

		public String getTableName() {
			return this.tableName;
		}

		public void setTableName(String tableName) {
			this.tableName = tableName;
		}

		public String getNick() {
			return this.nick;
		}

		public void setNick(String nick) {
			this.nick = nick;
		}
	}

	protected class Coluna {
		private Field f;
		private String apelido;
		private Tabela tabela;

		public Coluna(Field f, String apelido, Tabela tabela) {
			this.f = f;
			this.apelido = apelido;
			this.setTabela(tabela);
		}

		public Field getF() {
			return this.f;
		}

		public void setF(Field f) {
			this.f = f;
		}

		public String getApelido() {
			return this.apelido;
		}

		public void setApelido(String apelido) {
			this.apelido = apelido;
		}

		public Tabela getTabela() {
			return this.tabela;
		}

		public void setTabela(Tabela tabela) {
			this.tabela = tabela;
		}
	}

	public String buildTableNickName() {
		int hash = Math.abs(new Random().nextInt(50));

		String nick = "T" + hash;

		while (this.apelidosTabelasUsadas.contains(nick)) {
			hash = Math.abs(new Random().nextInt(50));

			nick = "T" + hash;
		}

		return nick;
	}

	public String buildFieldNickName() {
		int hash = Math.abs(new Random().nextInt(100));

		String nick = "F" + hash;

		while (this.apelidosCamposUsadas.contains(nick)) {
			hash = Math.abs(new Random().nextInt(100));

			nick = "F" + hash;
		}

		return nick;
	}

	@Override
	public String buildSqlInsertStatement(Class<BaseVo> classe) throws DAOException {

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + this.getTablename(classe) + " (");

		Field[] fields = classe.getDeclaredFields();

		for (Field f : fields) {
			if (this.getColumn(f) != null) {
				sql.append(this.getColumnName(f) + ", ");
			}
		}

		sql.delete(sql.length() - 2, sql.length());

		sql.append(") VALUES (");

		for (Field f : fields) {
			if (this.getColumn(f) != null) {
				sql.append("?, ");
			}
		}

		sql.delete(sql.length() - 2, sql.length());

		sql.append(")");

		return sql.toString();
	}

	@Override
	public String buildSqlNextSequence(Class<BaseVo> classe) {
		String sequenceName = this.getSequenceName(classe);

		String schema = this.getSchemaName(classe);

		if (schema != null && !schema.trim().equals("")) {
			sequenceName = schema + "." + sequenceName;
		}

		if (sequenceName != null) {
			StringBuilder sb = new StringBuilder("SELECT " + sequenceName + ".NEXTVAL FROM DUAL");
			return sb.toString();
		}

		return null;
	}

	@Override
	public String buildSqlUpdateStatement(Class<BaseVo> classe) throws DAOException {

		StringBuilder sql = new StringBuilder();

		String nickName = this.buildTableNickName();
		String tableName = this.getTablename(classe);

		this.tabelasMap.put(classe, new Tabela(tableName, nickName));

		sql.append("\n UPDATE " + tableName + " " + nickName + " SET ");

		Field[] fields = classe.getDeclaredFields();

		for (Field f : fields) {
			Column column = this.getColumn(f);
			if (column != null && !column.isKey()) {
				sql.append("\n        " + this.getColumnName(f) + " = ?, ");
			}
		}

		sql.delete(sql.length() - 2, sql.length());

		this.buildSqlWhereStatement(sql, classe, null, true, tableName, new DaoControle(0));

		return sql.toString();
	}

	@Override
	public String buildSqlDeleteStatement(Class<BaseVo> classe) throws DAOException {

		StringBuilder sql = new StringBuilder();
		sql.append("\nDELETE FROM " + this.getTablename(classe) + " ");

		this.buildSqlWhereStatement(sql, classe, null, true, this.getTablename(classe), new DaoControle(0));

		return sql.toString();
	}

	@Override
	public String buildSqlSelectStatement(Class<BaseVo> classe, int maxSubJoins) throws DAOException {

		StringBuilder sql = new StringBuilder();
		sql.append("\n SELECT");

		String tablename = this.getTablename(classe);

		String nick = this.buildTableNickName();

		this.tabelasMap.put(classe, new Tabela(tablename, nick));

		Field[] fields = classe.getDeclaredFields();

		this.buildSelectFields(classe, sql, fields, nick, new DaoControle(maxSubJoins));

		// Remove a virgula e o espaco do final
		sql.delete(sql.length() - 2, sql.length());

		sql.append("\n\n   FROM " + tablename + " " + nick);

		// Loop dos Joins Encontrados
		List<String> buff = new ArrayList<String>();

		this.buidlSelectJoins(sql, fields, tablename, buff, nick, tablename, new DaoControle(maxSubJoins));

		return sql.toString();
	}

	@Override
	public String buildSqlCountStatement(Class<BaseVo> classe, int maxSubJoins) throws DAOException {
		StringBuilder sql = new StringBuilder();
		sql.append("\n SELECT");

		String tablename = this.getTablename(classe);

		sql.append("\n     COUNT(1)");

		sql.append("\n\n   FROM " + tablename);

		// Loop dos Joins Encontrados
		List<String> buff = new ArrayList<String>();

		Field[] fields = classe.getDeclaredFields();

		this.buidlSelectJoins(sql, fields, tablename, buff, tablename + "_", tablename, new DaoControle(maxSubJoins));

		return sql.toString();
	}

	@SuppressWarnings("unchecked")
	public void buildSelectFields(Class<BaseVo> classe, StringBuilder sql, Field[] fields, String prefix, DaoControle controle) throws DAOException {
		for (Field f : fields) {
			Join join = this.getJoin(f);
			Column column = this.getColumn(f);

			String fieldNickName = this.buildFieldNickName();

			this.colunasMap.put(f, new Coluna(f, fieldNickName, this.tabelasMap.get(classe)));

			if (column != null) {
				sql.append("\n        " + prefix + "." + this.getColumnName(f) + " AS " + fieldNickName + ", ");

			} else if (join != null && join.joinWhen() == JoinWhen.ALWAYS) {

				if (f.getType().getSuperclass().equals(BaseVo.class)) {
					// TODO - Ajustar o JOIN
					// Se o field for do tipo VO, ira fazer select de todas as
					// suas colunas.
					Field[] fieldsJoin = f.getType().getDeclaredFields();

					// sql.append("\n");

					String prefix2 = join.tableAlias().equals("") ? this.getTablename(f.getType()) : join.tableAlias();
					prefix2 += "";

					if (!controle.isMaximo()) {
						controle.incrementaInteracoes();
						this.buildSelectFields((Class<BaseVo>) f.getType(), sql, fieldsJoin, prefix + "_" + prefix2, controle);
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
		// TODO - Nao testei essa parte ainda!!!
		for (Field f : fields) {
			Join join = this.getJoin(f);

			if (join != null && join.joinWhen() == JoinWhen.ALWAYS) {
				String tableJoin = null;

				if (f.getType().getSuperclass().equals(BaseVo.class)) {
					tableJoin = this.getTablename(f.getType());
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

					sql.append("\n  " + this.getJoinType(join) + " " + tableJoin + " " + prefix + tableAlias);
					boolean first = true;

					for (int i = 0; i < join.columnsLocal().length; i++) {
						sql.append("\n    " + (first ? " ON" : "AND") + " " + prefix + tableAlias + "." + join.columnsRemote()[i] + " = " + tableOrigin + "." + join.columnsLocal()[i]);
					}
				}

				if (f.getType().getSuperclass().equals(BaseVo.class)) {
					if (!controle.isMaximo()) {
						controle.incrementaInteracoes();
						this.buidlSelectJoins(sql, f.getType().getDeclaredFields(), tableJoin, buff, prefix + tableAlias + "_", prefix + tableAlias, controle);
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
				Column column = this.getColumn(f);

				if ((column != null && !keysOnly) || (keysOnly && column != null && column.isKey())) {

					Method getter = DaoUtils.getGetterMethod(classe, f);

					Object value = null;

					if (filter != null) {
						value = DaoUtils.getValue(getter, filter);
					}

					if (value != null || keysOnly) {

						prefix = this.tabelasMap.get(classe).getNick();

						if (f.getType().equals(String.class)) {
							// Apenas o tipo STRING muda o filtro para LIKE
							sqlWhere.append("\n    AND UPPER(" + prefix + "." + this.getColumnName(f) + ") LIKE UPPER(?) ");
						} else {
							sqlWhere.append("\n    AND " + prefix + "." + this.getColumnName(f) + " = ? ");
						}
					}
				}

				// Filtro recursivo
				Join join = f.getAnnotation(Join.class);

				if (!keysOnly && join != null && join.joinWhen() == JoinWhen.ALWAYS) {
					Method getter = DaoUtils.getGetterMethod(classe, f);

					Object value = null;

					value = DaoUtils.getValue(getter, filter);

					if (value != null && !controle.isMaximo()) {
						controle.incrementaInteracoes();

						String prefix2 = join.tableAlias().equals("") ? this.getTablename(f.getType()) : join.tableAlias();

						this.buildSqlWhereStatement(sqlWhere, (Class<BaseVo>) f.getType(), (BaseVo) value, false, prefix + "_" + prefix2, controle);
					}
				}
			}
		}
	}

	@Override
	public void tableExists(Class<BaseVo> classe, DAOManager manager) throws DAOException {
		String tablename = this.getTablename(classe);

		Log.debug("Checando se a tabela " + tablename + " existe");

		try {

			ResultSet result = manager.getConnection().getMetaData().getTables(null, null, tablename.toUpperCase(), null);

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

			String sequenceName = this.getSequenceName(classe);

			Log.debug("Checando se a sequence " + sequenceName + " existe");

			if (sequenceName == null) {
				return;
			}
			PreparedStatement statement = manager.getConnection().prepareStatement("SELECT * FROM all_sequences u WHERE u.sequence_name = '" + sequenceName.toUpperCase() + "'");
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
		String tablename = this.getTablename(classe);

		Log.debug("Checando os campos da tabela " + tablename);

		try {

			for (Field f : classe.getDeclaredFields()) {

				// Busca a coluna na classe
				Column column = this.getColumn(f);

				String columnName = this.getColumnName(f);

				if (column != null) {

					ResultSet result = manager.getConnection().getMetaData().getColumns(null, null, tablename.toUpperCase(), columnName.toUpperCase());

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
							if (dataType != java.sql.Types.INTEGER && dataType != java.sql.Types.NUMERIC && dataType != java.sql.Types.DOUBLE && dataType != java.sql.Types.DECIMAL) {
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
							if (dataType != java.sql.Types.NUMERIC && dataType != java.sql.Types.DECIMAL) {
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
						} else if (column.dataType() == DataType.TEXT) {
							if (dataType != java.sql.Types.CLOB) {
								throw new WrongColumnException("Coluna " + columnName + " da tabela " + tablename + " do tipo errado! VO: " + column.dataType() + ", BD: " + dataType, classe, column, f);
							}

							// Checa o tipo de dados do VO, se é compativel com
							// o tipo de dados da coluna
							if (!f.getType().equals(String.class)) {
								throw new WrongFieldException("Field " + f.getName() + " do tipo errado, esperado: " + String.class.getName() + ", encontrado: " + f.getType().getName() + ", dataType: " + column.dataType(), classe, column);
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
		String tableName = this.getTablename(classe);

		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE " + tableName + " (");

		Field[] fields = classe.getDeclaredFields();

		String sqlPk = "";

		for (Field f : fields) {

			String columnName = this.getColumnName(f);
			Column column = this.getColumn(f);

			String columnType = "";

			if (column != null) {

				columnType = this.getColumnDeclaration(column);

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
				this.tableCheck(classe, manager);
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
			columnType = "NUMERIC(8)";
		} else if (column.dataType() == DataType.NUMERIC) {
			columnType = "NUMERIC";
		} else if (column.dataType() == DataType.TEXT) {
			columnType = "CLOB";
		} else if (column.dataType() == DataType.BYTE) {
			columnType = "BLOB";
		}

		if (column.maxSize() > 0) {
			columnSize = column.maxSize();
		} else {
			if (column.dataType() == DataType.VARCHAR) {
				columnSize = SQLBuilder.VARCHAR_SIZE_DEFAULT;
			} else if (column.dataType() == DataType.CHAR || column.dataType() == DataType.DOMAIN_STRING) {
				columnSize = SQLBuilder.CHAR_SIZE_DEFAULT;
			} else if (column.dataType() == DataType.NUMERIC) {
				columnSize = SQLBuilder.NUMERIC_DEFAULT_SIZE;
			}
		}

		if (column.precision() > 0) {
			columnType += " (" + columnSize + ", " + column.precision() + ") ";
		} else if (columnSize > 0) {
			columnType += " (" + columnSize + ") ";
		}

		if (column.defaultValue() != null && !column.defaultValue().trim().equals("")) {
			columnType += " DEFAULT " + column.defaultValue();
		}

		return columnType;
	}

	@Override
	public void createSequence(Class<BaseVo> classe, DAOManager manager) throws DAOException {
		String sequenceName = this.getSequenceName(classe);

		try {
			if (sequenceName == null) {
				return;
			}
			String sql = "CREATE SEQUENCE " + sequenceName + " START WITH 1 INCREMENT BY 1 NOCYCLE NOCACHE";

			Log.debugsql(sql);

			PreparedStatement statement = manager.getConnection().prepareStatement(sql);
			statement.execute();

		} catch (SQLException e) {
			throw new SQLDaoException("Erro ao criar sequence " + sequenceName, e);
		}
	}

	@Override
	public void alterTable(Class<BaseVo> classe, DAOManager manager, DAOException e) throws DAOException {
		String tableName = this.getTablename(classe);

		if (e instanceof WrongColumnException) {
			WrongColumnException e1 = (WrongColumnException) e;

			String columnName = this.getColumnName(e1.getField());
			String columnDeclaration = this.getColumnDeclaration(e1.getColumn());

			StringBuilder sql = new StringBuilder("ALTER TABLE " + tableName + " ");
			sql.append(" MODIFY " + columnName + " " + columnDeclaration);

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

			String columnName = this.getColumnName(e1.getField());
			String columnDeclaration = this.getColumnDeclaration(e1.getColumn());

			StringBuilder sql = new StringBuilder("ALTER TABLE " + tableName + " ");
			sql.append(" MODIFY " + columnName + (e1.getColumn().isNotNull() || e1.getColumn().isKey() ? " SET NOT NULL" : " DROP NOT NULL"));

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

			String columnName = this.getColumnName(e1.getField());
			String columnDeclaration = this.getColumnDeclaration(e1.getColumn());

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
		String tablename = this.getTablename(classe);
		String sql = "DROP TABLE " + tablename + " CASCADE CONSTRAINTS";

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
			this.sequenceExists(classe, manager);
		} catch (SequenceNotExistsException e1) {
			return;
		}

		String sequenceName = this.getSequenceName(classe);

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

					ResultSet result = manager.getConnection().getMetaData().getIndexInfo(null, null, this.getTablename(classe).toUpperCase(), true, true);

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
		Table table = classe.getAnnotation(Table.class);

		if (table == null) {
			return;
		}

		try {
			String sql = "ALTER TABLE " + this.getTablename(classe) + " ADD CONSTRAINT " + constraint.constraintName() + " UNIQUE (";

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

	@Override
	public String buildSqlOrderByStatement(String orderFields) throws DAOException {
		if (orderFields == null || orderFields.trim().equals("")) {
			return "";
		}

		StringBuilder sql = new StringBuilder();

		sql.append("ORDER BY ");
		sql.append(orderFields);

		return sql.toString();
	}

	@Override
	public String buildSpecialWhereConditions(Class<BaseVo> voClass, String whereCondition) {
		if (whereCondition == null || whereCondition.trim().equals("")) {
			return "";
		}
		return " AND (" + whereCondition + ")";
	}

	@Override
	public void foreignKeysCheck(Class<BaseVo> classe, DAOManager manager) throws DAOException {
		// TODO Check das constraints / foreign keys
		try {

			Field[] fields = classe.getDeclaredFields();

			for (Field f : fields) {
				Join join = f.getAnnotation(Join.class);
				if (join != null) {

					String tableName = this.getTablename(classe);

					String tableRemote = this.getTableNameRemote(f, join);

					String constraintName = this.getForeignKeyName(classe, join, f);

					String sql = "SELECT * FROM user_constraints uc WHERE table_name = ? AND constraint_type = 'R' AND constraint_name = ?";

					PreparedStatement prepared = manager.getConnection().prepareStatement(sql);
					prepared.setString(1, tableName.toUpperCase());
					prepared.setString(2, constraintName.toUpperCase());

					ResultSet result = prepared.executeQuery();

					if (!result.next()) {
						result.close();
						// Se estiver vazio, dispara exception
						throw new ForeignKeyNotFoundException("Foreign key " + constraintName + " nao encontrada na tabela " + tableName + " com a tabela " + tableRemote, classe, join, f);
					}

					result.close();

					// TODO 2 Passo - Checa os campos do relacionamento
				}
			}

		} catch (SQLException e) {
			Log.critical(e);
			throw new SQLDaoException("Erro ao buscar metadados de constraints", e);
		}
	}

	private String getTableNameRemote(Field f, Join join) throws DAOException {
		String tableRemote = join.tableRemote();

		if (tableRemote == null || tableRemote.trim().equals("")) {
			tableRemote = this.getTablename(f.getType());
		}
		return tableRemote;
	}

	private String getForeignKeyName(Class<BaseVo> classe, Join join, Field field) throws DAOException {
		String fk = "fk_";

		fk += this.getTablename(classe);
		fk += "_";
		fk += this.getTableNameRemote(field, join);

		// Limite Oracle
		if (fk.length() > 30) {
			fk = fk.substring(0, 25) + this.hashString(fk);
		}

		return fk;
	}

	@Override
	public void createForeignKey(Class<BaseVo> voClass, Join join, Field field, DAOManager manager) throws DAOException {
		// TODO Criar chave estrangeira

		String sql = "ALTER TABLE " + this.getTablename(voClass) + " ADD CONSTRAINT " + this.getForeignKeyName(voClass, join, field);
		sql += "  FOREIGN KEY (";

		for (String columnLocal : join.columnsLocal()) {
			sql += columnLocal + ", ";
		}

		// Tira a ultima virgula e espaco
		sql = sql.substring(0, sql.length() - 2);

		sql += ") REFERENCES " + this.getTableNameRemote(field, join) + " (";

		for (String columnRemote : join.columnsRemote()) {
			sql += columnRemote + ", ";
		}

		// Tira a ultima virgula e espaco
		sql = sql.substring(0, sql.length() - 2);

		sql += ")";

		Log.debugsql(sql);

		try {
			PreparedStatement prepared = manager.getConnection().prepareStatement(sql);
			prepared.execute();
		} catch (SQLException e) {
			Log.critical(e);
			throw new SQLDaoException("Erro ao criar foreign key!", e);
		}
	}

	@Override
	public String buildSqlLimit(String sql, Integer pageNumber, Integer pageSize) {
		if (pageSize != null && pageSize > 0 && pageNumber != null && pageNumber > 0) {
			return " LIMIT " + pageSize + " OFFSET " + pageSize * (pageNumber - 1);
		}
		return "";
	}

	/**
	 * Seta os parametros para o insert.
	 *
	 * @param prepared
	 *            PreparedStatement que tem o comando de insert
	 * @param vo
	 *            Objeto com os dados a serem inseridos
	 * @param auto
	 * @throws SQLException
	 * @throws SQLDaoException
	 */
	@Override
	public void setInsertParameters(PreparedStatement prepared, BaseVo vo, Class<BaseVo> classe, Integer auto) throws SQLException, SQLDaoException {

		int posicao = 0;

		for (Field f : classe.getDeclaredFields()) {
			Column c = this.getColumn(f);

			// Setar parametros
			if (c != null) {

				posicao++;

				// Busca o getter para este field
				Method getter = DaoUtils.getGetterMethod(classe, f);
				Method setter = DaoUtils.getSetterMethod(classe, f);

				// Getter nao pode ser nulo
				if (getter != null) {

					Object value = DaoUtils.getValue(getter, vo);
					if (!c.isAutoIncrement()) {
						Log.debugsql("Parametro SQL posicao: " + posicao + " valor: " + value);
					}

					if (value != null || c.isAutoIncrement()) {
						if (f.getType().equals(Integer.class)) {
							if (c.isAutoIncrement()) {
								// Integer auto = getSequenceNextVal();
								prepared.setInt(posicao, auto);
								DaoUtils.setValue(setter, vo, auto);
								Log.debugsql("Parametro SQL posicao: " + posicao + " valor: " + auto);
							} else {
								prepared.setInt(posicao, (Integer) value);
							}
						} else if (f.getType().equals(String.class)) {
							prepared.setString(posicao, (String) value);
						} else if (f.getType().equals(Calendar.class)) {
							if (value != null) {
								if (c.dataType() == DataType.DATE) {
									Date date = new Date(((Calendar) value).getTime().getTime());
									prepared.setDate(posicao, date);
								} else if (c.dataType() == DataType.DATETIME) {
									Timestamp t = new Timestamp(((Calendar) value).getTimeInMillis());
									prepared.setTimestamp(posicao, t);
								}
							}
						} else if (f.getType().equals(BigDecimal.class)) {
							prepared.setBigDecimal(posicao, (BigDecimal) value);
						} else {
							// Default - seta como object
							prepared.setObject(posicao, value);
						}
					} else {

						this.setNullParameter(prepared, posicao, f, c);
						// setNullParameter(prepared, posicao, f, c);
					}
				}
			}
		}
	}

	private void setNullParameter(PreparedStatement prepared, int posicao, Field f, Column c) throws SQLException {
		if (f.getType().equals(Integer.class)) {
			prepared.setNull(posicao, java.sql.Types.INTEGER);
		} else if (f.getType().equals(String.class)) {
			prepared.setNull(posicao, java.sql.Types.VARCHAR);
		} else if (f.getType().equals(BigDecimal.class)) {
			prepared.setNull(posicao, java.sql.Types.DECIMAL);
		} else if (f.getType().equals(Calendar.class)) {
			if (c.dataType() == DataType.DATE) {
				prepared.setNull(posicao, java.sql.Types.DATE);
			} else if (c.dataType() == DataType.DATETIME) {
				prepared.setNull(posicao, java.sql.Types.TIMESTAMP);
			}
		}
	}

	/**
	 * Seta os parametros de uma clausula WHERE, usado para SELECT.
	 *
	 * @param prepared
	 *            java.sql.PreparedStatement com o comando a ser executado
	 * @param vo
	 *            Objeto com os valores a serem setados
	 * @param classe
	 *            Classe do filtro
	 * @param keysOnly
	 *            Indica se serao usados somente as colunas com o filtro isKey =
	 *            true. Se for true, mesmo que a coluna esteja nula no vo, sera
	 *            utilizada a condicao, com o valor NULL.
	 * @param prefix
	 *            Prefixo dos campos, usado para filtros recursivos.
	 * @param controle
	 *            Objeto de controle recursivo, para evitar loops infinitos no
	 *            caso de auto-referenciamento.
	 * @param posicao
	 *            Objeto de controle para marcar a posicao / contador dos
	 *            filtros setados.
	 * @throws SQLException
	 * @throws DAOException
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setSelectWhereParameters(PreparedStatement prepared, BaseVo vo, Class<BaseVo> classe, boolean keysOnly, String prefix, DaoControle controle, DaoControle posicao) throws SQLException, DAOException {
		// int posicao = 0;

		for (Field f : classe.getDeclaredFields()) {
			Column c = this.getColumn(f);
			Join join = f.getAnnotation(Join.class);

			// Setar parametros
			if (c != null) {

				if ((keysOnly && c.isKey()) || !keysOnly) {

					// Busca o getter para este field
					Method getter = DaoUtils.getGetterMethod(classe, f);

					// Getter nao pode ser nulo
					if (getter != null) {

						Object value = DaoUtils.getValue(getter, vo);

						if (value != null) {

							posicao.incrementaInteracoes();

							Log.debugsql("Parametro SQL posicao: " + posicao.getNumeroInteracoes() + " valor: " + value);

							if (f.getType().equals(Integer.class)) {
								prepared.setInt(posicao.getNumeroInteracoes(), (Integer) value);
							} else if (f.getType().equals(String.class)) {
								prepared.setString(posicao.getNumeroInteracoes(), (String) value);
							} else if (f.getType().equals(Calendar.class)) {
								if (value != null) {
									if (c.dataType() == DataType.DATE) {
										Date date = new Date(((Calendar) value).getTime().getTime());
										prepared.setDate(posicao.getNumeroInteracoes(), date);
									} else if (c.dataType() == DataType.DATETIME) {
										Timestamp t = new Timestamp(((Calendar) value).getTimeInMillis());
										prepared.setTimestamp(posicao.getNumeroInteracoes(), t);
									}
								}
							} else {
								// Demais tipos, Date, Calendar, BigDecimal,
								// etc...
								prepared.setObject(posicao.getNumeroInteracoes(), value);
							}
						} else if (keysOnly) {
							posicao.incrementaInteracoes();

							Log.debugsql("Parametro SQL posicao: " + posicao.getNumeroInteracoes() + " valor: " + value);

							// prepared.setNull(posicao.getNumeroInteracoes(),
							// java.sql.Types.OTHER);
							this.setNullParameter(prepared, posicao.getNumeroInteracoes(), f, c);
						}
					}
				}
			} else if (join != null) {
				// Filtro recursivo

				if (!keysOnly && join != null) {
					Method getter = DaoUtils.getGetterMethod(classe, f);

					BaseVo vo2 = (BaseVo) DaoUtils.getValue(getter, vo);

					if (vo2 != null && !controle.isMaximo()) {
						controle.incrementaInteracoes();

						String prefix2 = join.tableAlias().equals("") ? this.getTablename(f.getType()) : join.tableAlias();

						this.setSelectWhereParameters(prepared, vo2, (Class<BaseVo>) vo2.getClass(), false, prefix2, controle, posicao);
					}
				}
			}
		}
	}

	/**
	 * Seta os parametros de uma clausula WHERE, usado para UPDATE.
	 *
	 * @param prepared
	 *            java.sql.PreparedStatement com o comando a ser executado
	 * @param vo
	 *            Objeto com os valores a serem setados
	 * @throws SQLException
	 */
	@Override
	public void setUpdateParameters(PreparedStatement prepared, BaseVo vo, Class<BaseVo> classe) throws SQLException {
		int posicao = 0;

		// Passo 1 - Somente os atributos
		for (Field f : classe.getDeclaredFields()) {
			Column c = this.getColumn(f);

			// Setar parametros
			if (c != null && !c.isKey()) {

				// Busca o getter para este field
				Method getter = DaoUtils.getGetterMethod(classe, f);

				// Getter nao pode ser nulo
				if (getter != null) {

					Object value = DaoUtils.getValue(getter, vo);

					Log.debugsql("Parametro SQL posicao: " + posicao + " valor: " + value);

					if (value != null) {

						posicao++;

						if (f.getType().equals(Integer.class)) {
							prepared.setInt(posicao, (Integer) value);
						} else if (f.getType().equals(String.class)) {
							prepared.setString(posicao, (String) value);
						} else if (f.getType().equals(Calendar.class)) {
							if (value != null) {
								if (c.dataType() == DataType.DATE) {
									Date date = new Date(((Calendar) value).getTime().getTime());
									prepared.setDate(posicao, date);
								} else if (c.dataType() == DataType.DATETIME) {
									Timestamp t = new Timestamp(((Calendar) value).getTimeInMillis());
									prepared.setTimestamp(posicao, t);
								}
							}
						} else if (f.getType().equals(BigDecimal.class)) {
							prepared.setBigDecimal(posicao, (BigDecimal) value);
						} else {
							// Demais tipos, Date, Calendar, BigDecimal,
							// etc...
							prepared.setObject(posicao, value);
						}
					} else {
						posicao++;
						this.setNullParameter(prepared, posicao, f, c);
					}
				}
			}
		}

		// Passo 2 - Somente os campos chave
		for (Field f : classe.getDeclaredFields()) {
			Column c = this.getColumn(f);

			// Setar parametros
			if (c != null && c.isKey()) {

				// Busca o getter para este field
				Method getter = DaoUtils.getGetterMethod(classe, f);

				// Getter nao pode ser nulo
				if (getter != null) {

					Object value = DaoUtils.getValue(getter, vo);

					if (value != null) {

						posicao++;

						if (f.getType().equals(Integer.class)) {
							prepared.setInt(posicao, (Integer) value);
						} else if (f.getType().equals(String.class)) {
							prepared.setString(posicao, (String) value);
						} else if (f.getType().equals(Calendar.class)) {
							Date date = new Date(((Calendar) value).getTime().getTime());
							prepared.setDate(posicao, date);
						} else {
							// Demais tipos, Date, Calendar, BigDecimal,
							// etc...
							prepared.setObject(posicao, value);
						}
					} else {
						posicao++;
						this.setNullParameter(prepared, posicao, f, c);
					}
				}
			}
		}

	}

	/**
	 * Seta os parametros de uma clausula WHERE, usado para DELETE.
	 *
	 * @param prepared
	 *            java.sql.PreparedStatement com o comando a ser executado
	 * @param vo
	 *            Objeto com os valores a serem setados
	 * @param classe
	 * @throws SQLException
	 */
	@Override
	public void setDeleteParameters(PreparedStatement prepared, BaseVo vo, Class<BaseVo> classe) throws SQLException {
		int posicao = 0;

		for (Field f : classe.getDeclaredFields()) {
			Column c = this.getColumn(f);

			// Setar parametros
			if (c != null && c.isKey()) {

				// Busca o getter para este field
				Method getter = DaoUtils.getGetterMethod(classe, f);

				// Getter nao pode ser nulo
				if (getter != null) {

					Object value = DaoUtils.getValue(getter, vo);

					if (value != null) {

						posicao++;

						if (f.getType().equals(Integer.class)) {
							prepared.setInt(posicao, (Integer) value);
						} else if (f.getType().equals(String.class)) {
							prepared.setString(posicao, (String) value);
						} else if (f.getType().equals(Calendar.class)) {
							if (value != null) {
								if (c.dataType() == DataType.DATE) {
									Date date = new Date(((Calendar) value).getTime().getTime());
									prepared.setDate(posicao, date);
								} else if (c.dataType() == DataType.DATETIME) {
									Timestamp t = new Timestamp(((Calendar) value).getTimeInMillis());
									prepared.setTimestamp(posicao, t);
								}
							}
						} else {
							// Demais tipos, Date, Calendar, BigDecimal,
							// etc...
							prepared.setObject(posicao, value);
						}
					} else {
						posicao++;
						this.setNullParameter(prepared, posicao, f, c);
					}
				}
			}
		}

	}

	/**
	 * Converte um java.sql.ResultSet em um VO.
	 *
	 * @param vo
	 *            Objeto que ira receber os dados.
	 * @param result
	 *            Objeto ResultSet com os dados.
	 * @throws DAOException
	 * @throws SQLException
	 */
	@Override
	public void resultToVo(BaseVo vo, ResultSet result, String prefix, DaoControle controle) throws DAOException {

		for (Field f : vo.getClass().getDeclaredFields()) {
			Column c = this.getColumn(f);
			Join j = this.getJoin(f);

			if (c != null) {

				// Busca o getter para este field
				Method setter = DaoUtils.getSetterMethod(vo.getClass(), f);

				// Setter nao pode ser nulo
				if (setter != null) {

					String columnName = this.colunasMap.get(f).getApelido();

					if (f.getType().equals(Calendar.class)) {
						if (c.dataType() == DataType.DATETIME) {
							Timestamp t = null;
							try {
								t = result.getTimestamp(columnName);
							} catch (SQLException e) {
								e.printStackTrace();
							}

							if (t != null) {
								Calendar cal = Calendar.getInstance();
								cal.setTimeInMillis(t.getTime());

								DaoUtils.setValue(setter, vo, cal);
							}

						} else {
							Date date = null;
							try {
								date = result.getDate(columnName);
							} catch (SQLException e) {
								Log.warning(e.getMessage());
							}

							if (date != null) {
								Calendar cal = Calendar.getInstance();
								cal.setTimeInMillis(date.getTime());

								DaoUtils.setValue(setter, vo, cal);
							}
						}
					} else {
						try {
							DaoUtils.setValue(setter, vo, result.getObject(columnName));
						} catch (SQLException e) {
							// Se nao achou, e porque nao existe o field no
							// selet
							Log.warning("Erro ao buscar coluna " + columnName + ":" + e.getMessage());
						}
					}

				}
			} else if (j != null) {

				if (f.getType().getSuperclass().equals(BaseVo.class)) {
					// Se o tipo for BaseVo, faz injecao no VO

					// Se a chave que representa ele estiver nula, podemos
					// pular esta parte para dar agilidade.

					boolean hasNull = false;

					String prefix2 = j.tableAlias().equals("") ? this.getTablename(f.getType()) : j.tableAlias();

					for (int i = 0; i < j.columnsRemote().length; i++) {
						String columnOrigin = j.columnsRemote()[0];
						Object aux = null;

						try {
							aux = result.getObject(prefix + prefix2 + "_" + columnOrigin);

							if (aux == null) {
								hasNull = true;
							}
						} catch (SQLException e) {
							Log.warning(e.getMessage());
							hasNull = true;
						}
					}

					if (!hasNull) {

						Method setterVo = DaoUtils.getSetterMethod(vo.getClass(), f);

						if (setterVo != null) {

							Object voChild = DaoUtils.getNewObject(f.getType());

							// Seta a nova instancia do VO auxiliar
							DaoUtils.setValue(setterVo, vo, voChild);

							// Seta o vo como nao-novo, pois estamos buscando do
							// banco de dados.
							((BaseVo) voChild).setNew(false);

							if (!controle.isMaximo()) {
								controle.incrementaInteracoes();
								this.resultToVo((BaseVo) voChild, result, prefix + prefix2 + "_", controle);
							}
						}
					}

				} else {
					// Senao, usa procedimentos normais.

					// Busca o getter para este field
					Method setter = DaoUtils.getSetterMethod(vo.getClass(), f);

					// Setter nao pode ser nulo
					if (setter != null) {

						String joinFieldName = j.tableRemote() + "_" + j.fieldRemote();

						if (f.getType().equals(Calendar.class)) {
							// Tratamento especial para o tipo Calendar
							// - Converte de java.sql.Date para
							// java.util.Calendar

							Date date = null;
							try {
								date = result.getDate(joinFieldName);
							} catch (SQLException e) {
								Log.warning(e.getMessage());
							}

							if (date != null) {
								Calendar cal = Calendar.getInstance();
								cal.setTimeInMillis(date.getTime());

								DaoUtils.setValue(setter, vo, cal);
							}
						} else {
							try {
								DaoUtils.setValue(setter, vo, result.getObject(joinFieldName));
							} catch (SQLException e) {
								// Se nao achou, e porque nao existe o field no
								// selet
							}
						}
					}
				}
			}
		}
	}
}
