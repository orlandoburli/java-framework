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

		this.checkTable();

		String sql = this.getBuilder().buildSqlInsertStatement(this.getVOClass());

		Log.debugsql(sql);

		try {

			PreparedStatement prepared = this.getManager().getConnection().prepareStatement(sql);

			this.getBuilder().setInsertParameters(prepared, vo, this.getVOClass(), this.getSequenceNextVal());

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
		this.checkTable();

		String sql = this.getBuilder().buildSqlUpdateStatement(this.getVOClass());

		Log.debugsql(sql);

		try {

			PreparedStatement prepared = this.getManager().getConnection().prepareStatement(sql);

			this.getBuilder().setUpdateParameters(prepared, vo, this.getVOClass());

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
		this.checkTable();

		String sql = this.getBuilder().buildSqlDeleteStatement(this.getVOClass());

		Log.debugsql(sql);

		try {

			PreparedStatement prepared = this.getManager().getConnection().prepareStatement(sql);

			this.getBuilder().setDeleteParameters(prepared, vo, this.getVOClass());

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

		List<E> list = this.getList(vo, null, null, null, null, true);

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
		E filter = (E) DaoUtils.getNewObject(this.getVOClass());

		DaoUtils.setValueId(this.getVOClass(), filter, key);

		List<E> list = this.getList(filter, null, null, null, null, true);

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
		return this.getList(null, null, null, null, null, false);
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
		return this.getList(filter, null, null, null, null, false);
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
		return this.getList(filter, whereCondition, orderFields, pageNumber, pageSize, false);
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

		this.checkTable();

		StringBuilder sqlWhere = new StringBuilder();

		String sql = this.getBuilder().buildSqlSelectStatement(this.getVOClass(), this.getMaxSubJoins());

		this.getBuilder().buildSqlWhereStatement(sqlWhere, this.getVOClass(), filter, keysOnly, this.getBuilder().getTablename(this.getVOClass()), new DaoControle(this.getMaxSubJoins()));

		sql += sqlWhere.toString();

		// TODO Construir um teste para este item
		sql += "\n" + this.getBuilder().buildSpecialWhereConditions(this.getVOClass(), whereCondition);

		// TODO Construir um teste para este item
		sql += "\n" + this.getBuilder().buildSqlOrderByStatement(orderFields);

		// TODO Construir um teste para este item
		sql += "\n" + this.getBuilder().buildSqlLimit(sql, pageNumber, pageSize);

		Log.debugsql(sql);

		List<E> list = new ArrayList<E>();

		try {
			PreparedStatement prepared = this.getManager().getConnection().prepareStatement(sql);

			this.getBuilder().setSelectWhereParameters(prepared, filter, this.getVOClass(), keysOnly, this.getBuilder().getTablename(this.getVOClass()), new DaoControle(this.getMaxSubJoins()), new DaoControle(0));

			ResultSet result = prepared.executeQuery();

			while (result.next()) {
				E item = (E) DaoUtils.getNewObject(this.getVOClass());

				// Seta o vo como nao-novo, pois estamos buscando do banco de
				// dados.
				item.setNew(false);

				// String tablename = getBuilder().getTablename(getVOClass());

				this.getBuilder().resultToVo(item, result, this.getBuilder().getTablename(this.getVOClass()) + "_", new DaoControle(this.getMaxSubJoins()));

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
		this.checkTable();

		StringBuilder sqlWhere = new StringBuilder();

		this.getBuilder().buildSqlWhereStatement(sqlWhere, this.getVOClass(), filter, false, this.getBuilder().getTablename(this.getVOClass()), new DaoControle(this.getMaxSubJoins()));

		String sql = this.getBuilder().buildSqlCountStatement(this.getVOClass(), this.getMaxSubJoins()) + sqlWhere.toString();

		// TODO Construir um teste para este item
		sql += "\n" + this.getBuilder().buildSpecialWhereConditions(this.getVOClass(), whereCondition);

		Log.debugsql(sql);

		try {
			PreparedStatement prepared = this.getManager().getConnection().prepareStatement(sql);

			this.getBuilder().setSelectWhereParameters(prepared, filter, this.getVOClass(), false, this.getBuilder().getTablename(this.getVOClass()), new DaoControle(this.getMaxSubJoins()), new DaoControle(0));

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
		this.checkTable();

		StringBuilder sqlWhere = new StringBuilder();

		this.getBuilder().buildSqlWhereStatement(sqlWhere, this.getVOClass(), filter, false, this.getBuilder().getTablename(this.getVOClass()), new DaoControle(this.getMaxSubJoins()));

		String sql = this.getBuilder().buildSqlCountStatement(this.getVOClass(), this.getMaxSubJoins()) + sqlWhere.toString();

		// TODO Construir um teste para este item
		sql += "\n" + this.getBuilder().buildSpecialWhereConditions(this.getVOClass(), whereCondition);

		Log.debugsql(sql);

		try {
			PreparedStatement prepared = this.getManager().getConnection().prepareStatement(sql);

			this.getBuilder().setSelectWhereParameters(prepared, filter, this.getVOClass(), false, this.getBuilder().getTablename(this.getVOClass()), new DaoControle(this.getMaxSubJoins()), new DaoControle(0));

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
		String sql = this.getBuilder().buildSqlNextSequence(this.getVOClass());

		Log.debugsql(sql);

		if (sql == null) {
			return null;
		}

		try {
			PreparedStatement prepared = this.getManager().getConnection().prepareStatement(sql);
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
		if (this.isInBuffer()) {
			return this;
		}

		if (this.isUpdateSchema()) {
			try {
				// Checa se a sequence existe
				this.getBuilder().sequenceExists(this.getVOClass(), this.getManager());
			} catch (SequenceNotExistsException e) {
				try {
					this.getBuilder().createSequence(this.getVOClass(), this.getManager());
				} catch (DAOException e1) {
					Log.critical(e1);
				}
			}

			try {
				// Checa se a tabela exsite
				this.getBuilder().tableExists(this.getVOClass(), this.getManager());
			} catch (TableNotExistsException e) {
				this.getBuilder().createTable(this.getVOClass(), this.getManager());
			}

			boolean flagOk = false;

			// Fica em loop ate que esteja 100% OK
			while (!flagOk) {
				try {
					// Checa se a tabela esta ok com seus campos
					this.getBuilder().tableCheck(this.getVOClass(), this.getManager());
					flagOk = true;
				} catch (WrongNotNullException | WrongColumnException | ColumnNotFoundException e) {
					this.getBuilder().alterTable(this.getVOClass(), this.getManager(), e);
				}
			}

			// Checa as constraints
			flagOk = false;

			while (!flagOk) {
				try {
					this.getBuilder().constraintsCheck(this.getVOClass(), this.getManager());
					flagOk = true;
				} catch (UniqueConstraintNotFoundException e) {
					this.getBuilder().createUniqueConstraint(this.getVOClass(), e.getConstraint(), this.getManager());
				}
			}

			// Checa as chaves estrangeiras
			flagOk = false;

			while (!flagOk) {
				try {
					this.getBuilder().foreignKeysCheck(this.getVOClass(), this.getManager());
					flagOk = true;
				} catch (ForeignKeyNotFoundException e) {
					this.getBuilder().createForeignKey(this.getVOClass(), e.getJoin(), e.getField(), this.getManager());
				}
			}

			this.addToBuffer();
		}

		return this;
	}

	public BaseCadastroDao<E> dropTable() throws DAOException {

		try {
			this.getBuilder().tableExists(this.getVOClass(), this.getManager());
		} catch (TableNotExistsException e) {
			return this;
		}

		this.getBuilder().dropTable(this.getVOClass(), this.getManager());

		this.removeFromBuffer();

		return this;
	}

	public BaseCadastroDao<E> dropSequence() throws DAOException {

		try {
			this.getBuilder().sequenceExists(this.getVOClass(), this.getManager());
		} catch (SequenceNotExistsException e) {
			return this;
		}

		this.getBuilder().dropSequence(this.getVOClass(), this.getManager());

		this.removeFromBuffer();

		return this;
	}

	/**
	 * Indica se a tabela esta no buffer de controle de checagem de tabelas.
	 *
	 * @return True se a tabela estiver no buffer.
	 * @throws DAOException
	 */
	private boolean isInBuffer() throws DAOException {
		if (!this.isDbCheck()) {
			return true;
		}

		for (String s : BaseCadastroDao.BUFFER_TABELAS) {
			String tableName = this.getBuilder().getTablename(this.getVOClass());

			if (s.equals(tableName)) {
				return true;
			}
		}
		return false;
	}

	private void removeFromBuffer() throws DAOException {
		for (String s : BaseCadastroDao.BUFFER_TABELAS) {
			String tableName = this.getBuilder().getTablename(this.getVOClass());

			if (s.equals(tableName)) {
				BaseCadastroDao.BUFFER_TABELAS.remove(s);
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
		String tableName = this.getBuilder().getTablename(this.getVOClass());
		BaseCadastroDao.BUFFER_TABELAS.add(tableName);
	}

	/**
	 * Forca a limpeza do buffer para forcar a re-checagem das tabelas.
	 */
	public static void clearBuffer() {
		BaseCadastroDao.BUFFER_TABELAS.clear();
	}

	/**
	 * Esta funcao retorna quantos niveis no maximo o DAO ira seguir no join, a
	 * fim de evitar loop's infinitos por auto-referencia.
	 *
	 * @return Numero de niveis.
	 */
	public int getMaxSubJoins() {
		return 15;
	}

	/**
	 * Indica se é pra checar o schema
	 *
	 * @return
	 */
	public boolean isDbCheck() {
		String dbCheck = System.getProperty("db.check");

		if (dbCheck != null && dbCheck.equalsIgnoreCase("true")) {
			return true;
		}

		return false;
	}

	/**
	 * Indica se é para dar update nos objetos de banco de dados
	 *
	 * @return
	 */
	public boolean isUpdateSchema() {
		String dbUpdateSchema = System.getProperty("db.update.schema");

		if (dbUpdateSchema != null && dbUpdateSchema.equalsIgnoreCase("true")) {
			return true;
		}

		return false;
	}

}
