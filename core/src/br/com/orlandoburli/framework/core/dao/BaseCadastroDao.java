package br.com.orlandoburli.framework.core.dao;

import java.lang.reflect.ParameterizedType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

			getBuilder().setInsertParameters(prepared, vo, getVOClass(), getSequenceNextVal());

			prepared.execute();

		} catch (SQLException e) {
			Log.critical(e);
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

			getBuilder().setUpdateParameters(prepared, vo, getVOClass());

			prepared.execute();

		} catch (SQLException e) {
			Log.critical(e);
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

			getBuilder().setDeleteParameters(prepared, vo, getVOClass());

			prepared.execute();

		} catch (SQLException e) {
			Log.critical(e);
			throw new SQLDaoException("Erro ao executar delete no banco", e);
		}
	}

	/**
	 * Retorna um objeto do banco de dados.
	 * 
	 * @param vo
	 *            Objeto com os parametros (chave) a serem buscados.
	 * @return Objeto VO preenchido, se encontrado.
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
	 * Retorna um objeto do banco de dados pela sua chave (Só funciona quando o
	 * item tem id único,não funciona em chaves compostas).
	 * 
	 * @param Chave
	 *            do registro.
	 * 
	 * @return Objeto VO preenchido, se encontrado.
	 * @throws DAOException
	 */
	public E get(Object key) throws DAOException {

		@SuppressWarnings("unchecked")
		E filter = (E) DaoUtils.getNewObject(getVOClass());

		DaoUtils.setValueId(getVOClass(), filter, key);

		List<E> list = getList(filter, null, null, null, null, true);

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

			getBuilder().setSelectWhereParameters(prepared, filter, getVOClass(), keysOnly, getBuilder().getTablename(getVOClass()), new DaoControle(getMaxSubJoins()), new DaoControle(0));

			ResultSet result = prepared.executeQuery();

			while (result.next()) {
				E item = (E) DaoUtils.getNewObject(getVOClass());

				// Seta o vo como nao-novo, pois estamos buscando do banco de
				// dados.
				item.setNew(false);

				String tablename = getBuilder().getTablename(getVOClass());

				getBuilder().resultToVo(item, result, tablename + "_", new DaoControle(getMaxSubJoins()));

				list.add(item);
			}

			result.close();
		} catch (SQLException e) {
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

			getBuilder().setSelectWhereParameters(prepared, filter, getVOClass(), false, getBuilder().getTablename(getVOClass()), new DaoControle(getMaxSubJoins()), new DaoControle(0));

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
			Log.critical(e);
			throw new SQLDaoException("Erro ao buscar paginas da lista", e);
		}

	}

	public int getListCount(E filter, String whereCondition) throws DAOException {
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

			getBuilder().setSelectWhereParameters(prepared, filter, getVOClass(), false, getBuilder().getTablename(getVOClass()), new DaoControle(getMaxSubJoins()), new DaoControle(0));

			ResultSet result = prepared.executeQuery();

			int count = -1;

			while (result.next()) {
				// Este select so retorna 1 resultado, inteiro.
				count = result.getInt(1);
			}

			result.close();

			return count;

		} catch (SQLException e) {
			Log.critical(e);
			throw new SQLDaoException("Erro ao buscar quantidade da lista", e);
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

		if (sql == null) {
			return null;
		}

		try {
			PreparedStatement prepared = getManager().getConnection().prepareStatement(sql);
			ResultSet result = prepared.executeQuery();

			if (result.next()) {
				int retorno = result.getInt(1);
				result.close();

				return retorno;
			}
		} catch (SQLException e) {
			Log.critical(e);
			throw new SQLDaoException("Erro ao buscar valor de sequence", e);
		}

		return null;
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
				Log.critical(e1);
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
		flagOk = false;

		while (!flagOk) {
			try {
				getBuilder().foreignKeysCheck(getVOClass(), getManager());
				flagOk = true;
			} catch (ForeignKeyNotFoundException e) {
				getBuilder().createForeignKey(getVOClass(), e.getJoin(), e.getField(), getManager());
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
