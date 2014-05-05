package br.com.orlandoburli.framework.core.be;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.DeleteBeException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.InsertBeException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.ListException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.SaveBeException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.UpdateBeException;
import br.com.orlandoburli.framework.core.be.validation.ValidatorUtils;
import br.com.orlandoburli.framework.core.dao.BaseCadastroDao;
import br.com.orlandoburli.framework.core.dao.DAOManager;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.dao.exceptions.DAOException;
import br.com.orlandoburli.framework.core.log.Log;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public abstract class BaseBe<E extends BaseVo, F extends BaseCadastroDao<E>> {

	private DAOManager manager;

	public BaseBe(DAOManager manager) {
		if (manager == null) {
			throw new RuntimeException("DAOManager nao pode ser nulo!");
		}
		this.setManager(manager);
	}

	/**
	 * Valida, Transforma e persiste um objeto.
	 * 
	 * @param vo
	 *            Objeto a ser validado, transformado e persistido.
	 * @return O objeto passado no parametro <b>vo</b>, transformado e
	 *         validadado (se nao forem geradas excessoes).
	 * @throws BeException
	 */
	public E save(E vo) throws BeException {
		doBeforeSave(vo);

		boolean isNew = vo.isNew();

		if (isNew) {
			doBeforeInsert(vo);
		} else {
			doBeforeUpdate(vo);
		}

		F dao = getNewDao();

		try {
			if (isNew) {
				dao.inserir(vo);
			} else {
				dao.update(vo);
			}
		} catch (DAOException e) {
			e.printStackTrace();
			if (isNew) {
				throw new InsertBeException("Erro ao inserir registro, consulte o administrador do sistema.");
			} else {
				throw new UpdateBeException("Erro ao atualizar registro, consulte o administrador do sistema.");
			}
		}

		doAfterSave(vo);

		if (isNew) {
			doAfterInsert(vo);
		} else {
			doAfterUpdate(vo);
		}

		vo.setNew(false);

		return vo;
	}

	/**
	 * Remove um objeto do banco de dados.
	 * 
	 * @param vo
	 *            Objeto a ser removido.
	 * @throws DeleteBeException
	 */
	public void remove(E vo) throws DeleteBeException {
		doBeforeDelete(vo);

		F dao = getNewDao();

		try {
			dao.delete(vo);
		} catch (DAOException e) {
			e.printStackTrace();
			throw new DeleteBeException("Erro ao remover registro, consulte o administrador do sistema.");
		}

		doAfterDelete(vo);
	}

	/**
	 * Retorna um objeto do banco de dados.
	 * 
	 * @param vo
	 *            Objeto com os atributos de chave, devidamente setados.
	 * @return Objeto vo preenchido com os dados do banco de dados, se
	 *         encontrado.
	 * @throws ListException
	 */
	public E get(E vo) throws ListException {
		F dao = getNewDao();

		try {
			return dao.get(vo);
		} catch (DAOException e) {
			Log.critical(e);
			throw new ListException("Erro ao retornar dados. Consulte o suporte do sistema.");
		}
	}

	/**
	 * Retorna um objeto do banco de dados.
	 * 
	 * @param key
	 *            Id do objeto a ser retornado.
	 * @return Objeto vo preenchido com os dados do banco de dados, se
	 *         encontrado.
	 * @throws ListException
	 */
	public E get(Object key) throws ListException {
		F dao = getNewDao();

		try {
			return dao.get(key);
		} catch (DAOException e) {
			Log.critical(e);
			throw new ListException("Erro ao retornar dados. Consulte o suporte do sistema.");
		}
	}

	/**
	 * Retorna uma lista de objetos do banco de dados.
	 * 
	 * @param filter
	 *            Objeto que serve de base para filtros. Os valores que
	 *            estiverem setados serao usados para filtrar.
	 * @param whereCondition
	 *            Condicao (Where) especial, que nao pode ser atentida pelo
	 *            filtro <b>filter</b>.
	 * @param orderFields
	 *            Fields pelo qual ordenar os resultados.
	 * @param pageNumber
	 *            Numero da pagina de dados aonde a consulta se inicia. Nulo se
	 *            nao utilizar paginacao, caso contrario deve ser maior que
	 *            zero.
	 * @param pageSize
	 *            Tamanho da pagina de dados. Nulo se nao utilizado, caso
	 *            contrario deve ser maior que zero.
	 * @return Lista dos objetos encontrados com as condicoes.
	 * @throws ListException
	 */
	public List<E> getList(E filter, String whereCondition, String orderFields, Integer pageNumber, Integer pageSize) throws ListException {

		F dao = getNewDao();

		try {
			return dao.getList(filter, whereCondition, orderFields, pageNumber, pageSize);
		} catch (DAOException e) {
			Log.critical(e);
			throw new ListException("Erro ao retornar dados. Consulte o suporte do sistema.");
		}

	}

	/**
	 * Retorna uma lista de objetos do banco de dados.
	 * 
	 * @param filter
	 *            Objeto que serve de base para filtros. Os valores que
	 *            estiverem setados serao usados para filtrar.
	 * @return Lista dos objetos encontrados com as condicoes.
	 * @throws ListException
	 */
	public List<E> getList(E filter) throws ListException {
		return getList(filter, null, null);
	}

	public List<E> getList() throws ListException {
		return getList(null, null, null);
	}

	/**
	 * Retorna uma lista de objetos do banco de dados.
	 * 
	 * @param filter
	 *            Objeto que serve de base para filtros. Os valores que
	 *            estiverem setados serao usados para filtrar.
	 * @param whereCondition
	 *            Condicao (Where) especial, que nao pode ser atentida pelo
	 *            filtro <b>filter</b>.
	 * @param orderFields
	 *            Fields pelo qual ordenar os resultados.
	 * @return Lista dos objetos encontrados com as condicoes.
	 * @throws ListException
	 */
	public List<E> getList(E filter, String whereCondition, String orderFields) throws ListException {
		return getList(filter, whereCondition, orderFields, null, null);
	}

	public int getPageCount(E filter, String whereCondition, int pageSize) throws ListException {
		F dao = getNewDao();

		try {
			return dao.getPageCount(filter, whereCondition, pageSize);
		} catch (DAOException e) {
			Log.critical(e);
			throw new ListException("Erro ao retornar dados. Consulte o suporte do sistema.");
		}
	}

	public int getListCount(E filter, String whereCondition) throws ListException {
		F dao = getNewDao();

		try {
			return dao.getListCount(filter, whereCondition);
		} catch (DAOException e) {
			Log.critical(e);
			throw new ListException("Erro ao retornar dados. Consulte o suporte do sistema.");
		}
	}

	public void doBeforeSave(E vo) throws BeException {
		ValidatorUtils.validate(vo);
	}

	public void doBeforeUpdate(E vo) throws UpdateBeException {
	}

	public void doBeforeInsert(E vo) throws InsertBeException {
	}

	public void doBeforeDelete(E vo) throws DeleteBeException {
	}

	public void doAfterSave(E vo) throws SaveBeException {
	}

	public void doAfterUpdate(E vo) throws UpdateBeException {
	}

	public void doAfterInsert(E vo) throws InsertBeException {
	}

	public void doAfterDelete(E vo) throws DeleteBeException {
	}

	@SuppressWarnings("unchecked")
	private F getNewDao() {
		return (F) DaoUtils.getNewDao(getDAOlass(), getManager());
	}

	@SuppressWarnings("unchecked")
	/**
	 * Retorna a classe VO que esta no Generics da classe.s
	 * @return Classe VO.
	 */
	protected Class<BaseVo> getVOClass() {
		return ((Class<BaseVo>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	}

	@SuppressWarnings("unchecked")
	/**
	 * Retorna a classe DAO que esta no Generics da classe.s
	 * @return Classe DAO.
	 */
	protected Class<BaseCadastroDao<BaseVo>> getDAOlass() {
		return ((Class<BaseCadastroDao<BaseVo>>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1]);
	}

	public DAOManager getManager() {
		return manager;
	}

	public void setManager(DAOManager manager) {
		this.manager = manager;
	}
}
