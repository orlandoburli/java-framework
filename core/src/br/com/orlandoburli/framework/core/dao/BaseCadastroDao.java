package br.com.orlandoburli.framework.core.dao;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.orlandoburli.framework.core.dao.annotations.Column;
import br.com.orlandoburli.framework.core.dao.annotations.Join;
import br.com.orlandoburli.framework.core.dao.exceptions.ColumnNotFoundException;
import br.com.orlandoburli.framework.core.dao.exceptions.DAOException;
import br.com.orlandoburli.framework.core.dao.exceptions.ForeignKeyNotFoundException;
import br.com.orlandoburli.framework.core.dao.exceptions.SQLDaoException;
import br.com.orlandoburli.framework.core.dao.exceptions.SequenceNotExistsException;
import br.com.orlandoburli.framework.core.dao.exceptions.TableNotExistsException;
import br.com.orlandoburli.framework.core.dao.exceptions.UniqueConstraintNotFoundException;
import br.com.orlandoburli.framework.core.dao.exceptions.WrongColumnException;
import br.com.orlandoburli.framework.core.dao.exceptions.WrongNotNullException;
import br.com.orlandoburli.framework.core.log.Log;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public abstract class BaseCadastroDao<E extends BaseVo> extends BaseDao {

	static {
		BUFFER_TABELAS = new ArrayList<String>();
	}

	/**
	 * Buffer interno de controle de tabelas pre-checadas.
	 */
	public final static List<String> BUFFER_TABELAS;

	public BaseCadastroDao(DAOManager manager) {
		super(manager);
	}

	/**
	 * Insere um objeto no banco de dados.
	 * 
	 * @param vo
	 *            Objeto vo a ser inserido.
	 * @throws DAOException
	 */
	public void inserir(E vo) throws DAOException {

		checkTable();

		String sql = getBuilder().buildSqlInsertStatement(getVOClass());

		Log.debugsql(sql);

		try {

			PreparedStatement prepared = getManager().getConnection().prepareStatement(sql);

			setInsertParameters(prepared, vo, getVOClass());

			prepared.execute();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLDaoException("Erro ao executar insert no banco", e);
		}

	}

	/**
	 * Atualiza um objeto no banco de dados.
	 * 
	 * @param vo
	 *            Objeto a ser atualizado.
	 * @throws DAOException
	 */
	public void update(E vo) throws DAOException {
		checkTable();

		String sql = getBuilder().buildSqlUpdateStatement(getVOClass());

		Log.debugsql(sql);

		try {

			PreparedStatement prepared = getManager().getConnection().prepareStatement(sql);

			setUpdateParameters(prepared, vo, getVOClass());

			prepared.execute();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLDaoException("Erro ao executar update no banco", e);
		}
	}

	/**
	 * Exclui um objeto no banco de dados.
	 * 
	 * @param vo
	 *            Objeto a ser excluido.
	 * @throws DAOException
	 */
	public void delete(E vo) throws DAOException {
		checkTable();

		String sql = getBuilder().buildSqlDeleteStatement(getVOClass());

		Log.debugsql(sql);

		try {

			PreparedStatement prepared = getManager().getConnection().prepareStatement(sql);

			setDeleteParameters(prepared, vo, getVOClass());

			prepared.execute();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLDaoException("Erro ao executar delete no banco", e);
		}
	}

	/**
	 * Retorna um objeto do banco de dados.
	 * 
	 * @param vo
	 *            Objeto com os parametros (chave) a serem buscados.
	 * @return Objeto VO preenchido.
	 * @throws DAOException
	 */
	public E get(E vo) throws DAOException {

		List<E> list = getList(vo, null, null, null, null, true);

		if (list.size() == 1) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Retorna uma lista com todos os itens do banco de dados, sem filtros.
	 * 
	 * @return List do tipo <b>E</b>.
	 * @throws DAOException
	 */
	public List<E> getList() throws DAOException {

		return getList(null, null, null, null, null, false);
	}

	/**
	 * Retorna uma lista dos itens do banco de dados, usando como filtro os
	 * valores setados em <b>filter</b>.<br/>
	 * Todos os valores nao-nulos serao considerados no filtro.
	 * 
	 * @param filter
	 *            Objeto do tipo <b>E</b> com os valores a serem filtrados.
	 * @return List do tipo <b>E</b>.
	 * @throws DAOException
	 */
	public List<E> getList(E filter) throws DAOException {
		return getList(filter, null, null, null, null, false);
	}

	/**
	 * 
	 * Retorna uma lista dos itens do banco de dados, usando como filtro os
	 * valores setados em <b>filter</b>.<br/>
	 * Todos os valores nao-nulos serao considerados no filtro.
	 * 
	 * @param filter
	 *            Objeto do tipo <b>E</b> com os valores a serem filtrados.
	 * @param whereCondition
	 *            Condicao (Where) adicional. Usar os nomes dos fields na
	 *            pesquisa. Nao permite parametros.
	 * @param orderFields
	 *            Nomes dos Fields pelos quais a consulta sera ordenada.
	 * @param pageNumber
	 *            Numero da pagina de dados aonde a consulta se inicia. Nulo se
	 *            nao utilizar paginacao, caso contrario deve ser maior que
	 *            zero.
	 * @param pageSize
	 *            Tamanho da pagina de dados. Nulo se nao utilizado, caso
	 *            contrario deve ser maior que zero.
	 * @return List do tipo <b>E</b>.
	 * @throws DAOException
	 */
	public List<E> getList(E filter, String whereCondition, String orderFields, Integer pageNumber, Integer pageSize) throws DAOException {
		return getList(filter, whereCondition, orderFields, pageNumber, pageSize, false);
	}

	/**
	 * Retorna uma lista dos itens do banco de dados, usando como filtro os
	 * valores setados em <b>filter</b>.<br/>
	 * Todos os valores nao-nulos serao considerados no filtro.
	 * 
	 * @param filter
	 *            Objeto do tipo <b>E</b> com os valores a serem filtrados.
	 * @param keysOnly
	 *            Indica se ira filtrar somente pelos campos chave.
	 * @param whereCondition
	 *            Condicao (Where) adicional. Usar os nomes dos fields na
	 *            pesquisa. Nao permite parametros.
	 * @param orderFields
	 *            Nomes dos Fields pelos quais a consulta sera ordenada.
	 * @return List do tipo <b>E</b>.
	 * @throws DAOException
	 */
	@SuppressWarnings("unchecked")
	private List<E> getList(E filter, String whereCondition, String orderFields, Integer pageNumber, Integer pageSize, boolean keysOnly) throws DAOException {

		// TODO Implementar whereCondition;
		// TODO Implementar orderFields;
		// TODO Implementar pageNumber;
		// TODO Implementar pageSize;

		checkTable();

		StringBuilder sqlWhere = new StringBuilder();

		getBuilder().buildSqlWhereStatement(sqlWhere, getVOClass(), filter, keysOnly, getBuilder().getTablename(getVOClass()), new DaoControle(getMaxSubJoins()));

		String sql = getBuilder().buildSqlSelectStatement(getVOClass(), getMaxSubJoins()) + sqlWhere.toString();

		// TODO Construir um teste para este item
		sql += "\n" + getBuilder().buildSpecialWhereConditions(getVOClass(), whereCondition);

		// TODO Construir um teste para este item
		sql += "\n" + getBuilder().buildSqlOrderByStatement(orderFields);

		// TODO Construir um teste para este item
		sql += "\n" + getBuilder().buildSqlLimit(sql, pageNumber, pageSize);

		Log.debugsql(sql);

		List<E> list = new ArrayList<E>();

		try {
			PreparedStatement prepared = getManager().getConnection().prepareStatement(sql);

			setSelectWhereParameters(prepared, filter, getVOClass(), keysOnly, getBuilder().getTablename(getVOClass()), new DaoControle(getMaxSubJoins()), new DaoControle(0));

			ResultSet result = prepared.executeQuery();

			while (result.next()) {
				E item = (E) DaoUtils.getNewObject(getVOClass());

				// Seta o vo como nao-novo, pois estamos buscando do banco de
				// dados.
				item.setNew(false);

				String tablename = getBuilder().getTablename(getVOClass());

				resultToVo(item, result, tablename + "_", new DaoControle(getMaxSubJoins()));

				list.add(item);
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.critical(e);
			throw new SQLDaoException("Erro ao buscar lista", e);
		}

		return list;
	}

	public int getPageCount(E filter, String whereCondition, int pageSize) throws DAOException {
		// TODO Escrever um teste para este metodo
		checkTable();

		StringBuilder sqlWhere = new StringBuilder();

		getBuilder().buildSqlWhereStatement(sqlWhere, getVOClass(), filter, false, getBuilder().getTablename(getVOClass()), new DaoControle(getMaxSubJoins()));

		String sql = getBuilder().buildSqlCountStatement(getVOClass(), getMaxSubJoins()) + sqlWhere.toString();

		// TODO Construir um teste para este item
		sql += "\n" + getBuilder().buildSpecialWhereConditions(getVOClass(), whereCondition);

		Log.debugsql(sql);

		try {
			PreparedStatement prepared = getManager().getConnection().prepareStatement(sql);

			setSelectWhereParameters(prepared, filter, getVOClass(), false, getBuilder().getTablename(getVOClass()), new DaoControle(getMaxSubJoins()), new DaoControle(0));

			ResultSet result = prepared.executeQuery();

			int count = -1;

			while (result.next()) {
				// Este select so retorna 1 resultado, inteiro.
				count = result.getInt(1);
			}

			result.close();

			int inteiro = count / pageSize;
			int resto = count % pageSize;
			inteiro += (resto > 0) ? 1 : 0;

			return inteiro;

		} catch (SQLException e) {
			e.printStackTrace();
			Log.critical(e);
			throw new SQLDaoException("Erro ao buscar paginas da lista", e);
		}

	}

	/**
	 * Retorna o proximo valor da sequence.
	 * 
	 * @return Proximo valor da sequence.
	 * @throws SQLDaoException
	 */
	public Integer getSequenceNextVal() throws SQLDaoException {
		String sql = getBuilder().buildSqlNextSequence(getVOClass());

		Log.debugsql(sql);

		try {
			PreparedStatement prepared = getManager().getConnection().prepareStatement(sql);
			ResultSet result = prepared.executeQuery();

			if (result.next()) {
				int retorno = result.getInt(1);
				result.close();

				return retorno;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLDaoException("Erro ao buscar valor de sequence", e);
		}

		return null;
	}

	/**
	 * Seta os parametros para o insert.
	 * 
	 * @param prepared
	 *            PreparedStatement que tem o comando de insert
	 * @param vo
	 *            Objeto com os dados a serem inseridos
	 * @throws SQLException
	 * @throws SQLDaoException
	 */
	private void setInsertParameters(PreparedStatement prepared, E vo, Class<BaseVo> classe) throws SQLException, SQLDaoException {

		int posicao = 0;

		for (Field f : classe.getDeclaredFields()) {
			Column c = getBuilder().getColumn(f);

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
								Integer auto = getSequenceNextVal();
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
								Date date = new Date(((Calendar) value).getTime().getTime());
								prepared.setDate(posicao, date);
							}
						} else if (f.getType().equals(BigDecimal.class)) {
							prepared.setBigDecimal(posicao, (BigDecimal) value);
						} else {
							// Default - seta como object
							prepared.setObject(posicao, value);
						}
					} else {

						prepared.setNull(posicao, java.sql.Types.OTHER);
					}
				}
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
	@SuppressWarnings("unchecked")
	private void setSelectWhereParameters(PreparedStatement prepared, E vo, Class<BaseVo> classe, boolean keysOnly, String prefix, DaoControle controle, DaoControle posicao) throws SQLException, DAOException {
		// int posicao = 0;

		for (Field f : classe.getDeclaredFields()) {
			Column c = getBuilder().getColumn(f);
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
								Date date = new Date(((Calendar) value).getTime().getTime());
								prepared.setDate(posicao.getNumeroInteracoes(), date);
							} else {
								// Demais tipos, Date, Calendar, BigDecimal,
								// etc...
								prepared.setObject(posicao.getNumeroInteracoes(), value);
							}
						} else if (keysOnly) {
							posicao.incrementaInteracoes();

							Log.debugsql("Parametro SQL posicao: " + posicao.getNumeroInteracoes() + " valor: " + value);

							prepared.setNull(posicao.getNumeroInteracoes(), java.sql.Types.OTHER);
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

						String prefix2 = join.tableAlias().equals("") ? getBuilder().getTablename(f.getType()) : join.tableAlias();

						setSelectWhereParameters(prepared, (E) vo2, (Class<BaseVo>) vo2.getClass(), false, prefix2, controle, posicao);
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
	private void setUpdateParameters(PreparedStatement prepared, E vo, Class<BaseVo> classe) throws SQLException {
		int posicao = 0;

		// Passo 1 - Somente os atributos
		for (Field f : classe.getDeclaredFields()) {
			Column c = getBuilder().getColumn(f);

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
							Date date = new Date(((Calendar) value).getTime().getTime());
							prepared.setDate(posicao, date);
						} else if (f.getType().equals(BigDecimal.class)) {
							prepared.setBigDecimal(posicao, (BigDecimal) value);
						} else {
							// Demais tipos, Date, Calendar, BigDecimal,
							// etc...
							prepared.setObject(posicao, value);
						}
					} else {
						posicao++;
						prepared.setNull(posicao, java.sql.Types.OTHER);
					}
				}
			}
		}

		// Passo 2 - Somente os campos chave
		for (Field f : classe.getDeclaredFields()) {
			Column c = getBuilder().getColumn(f);

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
						prepared.setNull(posicao, java.sql.Types.OTHER);
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
	private void setDeleteParameters(PreparedStatement prepared, E vo, Class<BaseVo> classe) throws SQLException {
		int posicao = 0;

		for (Field f : classe.getDeclaredFields()) {
			Column c = getBuilder().getColumn(f);

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
						prepared.setNull(posicao, java.sql.Types.OTHER);
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
	private void resultToVo(BaseVo vo, ResultSet result, String prefix, DaoControle controle) throws DAOException {

		Log.info("ResultToVo - " + vo.getClass());

		for (Field f : vo.getClass().getDeclaredFields()) {
			Column c = getBuilder().getColumn(f);
			Join j = getBuilder().getJoin(f);

			if (c != null) {

				// Busca o getter para este field
				Method setter = DaoUtils.getSetterMethod(vo.getClass(), f);

				// Setter nao pode ser nulo
				if (setter != null) {

					String columnName = prefix + getBuilder().getColumnName(f);

					// Log.info("Column Name : " + columnName);

					if (f.getType().equals(Calendar.class)) {
						// Tratamento especial para o tipo Calendar - Converte
						// de java.sql.Date para java.util.Calendar
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
					} else {
						try {
							DaoUtils.setValue(setter, vo, result.getObject(columnName));
						} catch (SQLException e) {
							// Se nao achou, e porque nao existe o field no
							// selet
							Log.warning(e.getMessage());
						}
					}

				}
			} else if (j != null) {

				if (f.getType().getSuperclass().equals(BaseVo.class)) {
					// Se o tipo for BaseVo, faz injecao no VO

					// Se a chave que representa ele estiver nula, podemos
					// pular esta parte para dar agilidade.

					boolean hasNull = false;

					String prefix2 = j.tableAlias().equals("") ? getBuilder().getTablename(f.getType()) : j.tableAlias();

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
								resultToVo((BaseVo) voChild, result, prefix + prefix2 + "_", controle);
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

	@SuppressWarnings("unchecked")
	/**
	 * Retorna a classe VO que esta no Generics da classe.s
	 * @return Classe VO.
	 */
	protected Class<BaseVo> getVOClass() {
		return ((Class<BaseVo>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	}

	/**
	 * Checa se a tabela existe, e se os tipos de dados sao consistentes com o
	 * modelo de dados sugerido.
	 * 
	 * @return
	 * 
	 * @throws DAOException
	 */
	public BaseCadastroDao<E> checkTable() throws DAOException {
		if (isInBuffer()) {
			return this;
		}

		try {
			// Checa se a sequence existe
			getBuilder().sequenceExists(getVOClass(), getManager());
		} catch (SequenceNotExistsException e) {
			try {
				getBuilder().createSequence(getVOClass(), getManager());
			} catch (DAOException e1) {
				e1.printStackTrace();
			}
		}

		try {
			// Checa se a tabela exsite
			getBuilder().tableExists(getVOClass(), getManager());
		} catch (TableNotExistsException e) {
			getBuilder().createTable(getVOClass(), getManager());
		}

		boolean flagOk = false;

		// Fica em loop ate que esteja 100% OK
		while (!flagOk) {
			try {
				// Checa se a tabela esta ok com seus campos
				getBuilder().tableCheck(getVOClass(), getManager());
				flagOk = true;
			} catch (WrongNotNullException | WrongColumnException | ColumnNotFoundException e) {
				getBuilder().alterTable(getVOClass(), getManager(), e);
			}
		}

		// Checa as constraints
		flagOk = false;

		while (!flagOk) {
			try {
				getBuilder().constraintsCheck(getVOClass(), getManager());
				flagOk = true;
			} catch (UniqueConstraintNotFoundException e) {
				getBuilder().createUniqueConstraint(getVOClass(), e.getConstraint(), getManager());
			}
		}

		// Checa as chaves estrangeiras
		while (!flagOk) {
			try {
				getBuilder().foreignKeysCheck(getVOClass(), getManager());
				flagOk = true;
			} catch (ForeignKeyNotFoundException e) {
				getBuilder().createForeignKey(getVOClass(), e.getJoin(), getManager());
			}
		}

		addToBuffer();

		return this;
	}

	public BaseCadastroDao<E> dropTable() throws DAOException {

		try {
			getBuilder().tableExists(getVOClass(), getManager());
		} catch (TableNotExistsException e) {
			return this;
		}

		getBuilder().dropTable(getVOClass(), getManager());

		removeFromBuffer();

		return this;
	}

	public BaseCadastroDao<E> dropSequence() throws DAOException {

		try {
			getBuilder().sequenceExists(getVOClass(), getManager());
		} catch (SequenceNotExistsException e) {
			return this;
		}

		getBuilder().dropSequence(getVOClass(), getManager());

		removeFromBuffer();

		return this;
	}

	/**
	 * Indica se a tabela esta no buffer de controle de checagem de tabelas.
	 * 
	 * @return True se a tabela estiver no buffer.
	 * @throws DAOException
	 */
	private boolean isInBuffer() throws DAOException {
		for (String s : BUFFER_TABELAS) {
			String tableName = getBuilder().getTablename(getVOClass());

			if (s.equals(tableName)) {
				return true;
			}
		}
		return false;
	}

	private void removeFromBuffer() throws DAOException {
		for (String s : BUFFER_TABELAS) {
			String tableName = getBuilder().getTablename(getVOClass());

			if (s.equals(tableName)) {
				BUFFER_TABELAS.remove(s);
				break;
			}
		}
	}

	/**
	 * Adiciona a coluna ao buffer de controle de checagem, para evitar dupla
	 * checagem da estrutura.
	 * 
	 * @throws DAOException
	 */
	private void addToBuffer() throws DAOException {
		String tableName = getBuilder().getTablename(getVOClass());
		BUFFER_TABELAS.add(tableName);
	}

	/**
	 * Forca a limpeza do buffer para forcar a re-checagem das tabelas.
	 */
	public static void clearBuffer() {
		BUFFER_TABELAS.clear();
	}

	/**
	 * Esta funcao retorna quantos niveis no maximo o DAO ira seguir no join, a
	 * fim de evitar loop's infinitos por auto-referencia.
	 * 
	 * @return Numero de niveis.
	 */
	public int getMaxSubJoins() {
		return 10;
	}

}
