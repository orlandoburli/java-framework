package br.com.orlandoburli.framework.core.web;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import br.com.orlandoburli.framework.core.be.BaseBe;
import br.com.orlandoburli.framework.core.be.exceptions.BeException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.DeleteBeException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.InsertBeException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.ListException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.SaveBeException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.UpdateBeException;
import br.com.orlandoburli.framework.core.dao.BaseCadastroDao;
import br.com.orlandoburli.framework.core.dao.DAOManager;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;
import br.com.orlandoburli.framework.core.vo.utils.MessageVo;
import br.com.orlandoburli.framework.core.web.filters.InjectionFilter;
import br.com.orlandoburli.framework.core.web.retorno.RetornoAction;

public abstract class BaseCadastroAction<E extends BaseVo, F extends BaseCadastroDao<E>, G extends BaseBe<E, F>> extends BaseAction {

	private static final long serialVersionUID = 1L;

	protected List<MessageVo> messages;
	protected List<String> info;

	private boolean writeVoOnInsert = false;
	private boolean writeVoOnUpdate = false;

	private String operacao;

	private String term;

	private DAOManager manager;

	protected Class<?> getDAOClass() {
		return ((Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1]);
	}

	protected Class<?> getVOClass() {
		return ((Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	}

	protected Class<?> getBEClass() {
		return ((Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[2]);
	}

	public abstract String getJspCadastro();

	@SuppressWarnings("unchecked")
	protected E getNewVo() {
		try {
			return (E) getVOClass().newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected G getNewBe(DAOManager manager) {
		return (G) DaoUtils.getNewBe((Class<BaseBe<BaseVo, BaseCadastroDao<BaseVo>>>) getBEClass(), manager);
	}

//	public boolean doBeforeSave(E vo, DAOManager manager) {
//		return true;
//	}

	public void inserir() {
		E vo = getNewVo();
		G be = getNewBe(getManager());

		try {
			getManager().begin();

			injectVo(vo);

			doBeforeInserir(vo, getManager());
			doBeforeSalvar(vo, getManager());

			be.save(vo);

			doAfterInserir(vo, getManager());
			doAfterSalvar(vo, getManager());

			getManager().commit();

			write(new Gson().toJson(new RetornoAction(true, "Registro inserido com sucesso!")));

		} catch (BeException e) {
			getManager().rollback();
			write(new Gson().toJson(new RetornoAction(false, e.getMessage(), e.getField())));
		} finally {
			getManager().commit();
		}
	}

	public void alterar() {
		DAOManager manager = DAOManager.getDAOManager();
		try {
			manager.begin();

			@SuppressWarnings("unchecked")
			E vo = (E) getRequest().getSession().getAttribute(getVoSessionId()); // getNewVo();
			G be = getNewBe(manager);

			doBeforeAlterar(vo, manager);
			doBeforeSalvar(vo, manager);

			injectVo(vo);

			doAfterAlterar(vo, manager);
			doAfterSalvar(vo, manager);

			be.save(vo);

			manager.commit();

			write(new Gson().toJson(new RetornoAction(true, "Registro alterado com sucesso!", "")));

		} catch (BeException e) {
			manager.rollback();
			write(new Gson().toJson(new RetornoAction(false, e.getMessage(), e.getField())));
		} finally {
			manager.commit();
		}
	}

	public void excluir() {
		DAOManager manager = DAOManager.getDAOManager();
		try {
			manager.begin();

			E vo = getNewVo();
			G be = getNewBe(manager);

			doBeforeExcluir(vo, manager);

			injectVo(vo);

			doAfterExcluir(vo, manager);

			be.remove(vo);

			write(new Gson().toJson(new RetornoAction(true, "Registro excluído com sucesso!", "")));

			manager.commit();
		} catch (DeleteBeException e) {
			manager.rollback();
			write(new Gson().toJson(new RetornoAction(false, e.getMessage(), e.getField())));
		} finally {
			manager.commit();
		}
	}

	public void visualizar() {
		DAOManager manager = DAOManager.getDAOManager();

		try {

			G be = getNewBe(manager);

			E vo = getNewVo();

			injectVo(vo);

			doBeforeVisualizar(getRequest(), getResponse(), vo, be, manager);

			vo = be.get(vo);

			doBeforeWriteVo(vo);

			getRequest().setAttribute("vo", vo);

			getRequest().getSession().setAttribute(getVoSessionId(), vo);

			forward(getJspCadastro());

		} catch (ListException e) {
			new Gson().toJson(new RetornoAction(false, e.getMessage(), e.getField()));
		} finally {
			manager.commit();
		}
	}

	public void injectVo(E vo) {
		InjectionFilter filter = new InjectionFilter();
		filter.setContext(getContext());
		filter.setRequest(getRequest());
		filter.setResponse(getResponse());

		try {
			filter.doFilter(vo);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public void doBeforeVisualizar(HttpServletRequest request, HttpServletResponse response, E vo, G be, DAOManager manager) {

	}

	public void doBeforeWriteVo(E vo) {
	}

	public void consultar() {
		visualizar();
	}

	public boolean doBeforeDelete(E vo) throws DeleteBeException {
		return true;
	}

	/**
	 * Sobrescrever o metodo para operacoes realizadas apos inserir um registro
	 */
	public void doAfterInserir(E vo, DAOManager manager) throws InsertBeException {
	}

	/**
	 * Sobrescrever o metodo para operacoes realizadas apos alterar um registro
	 */
	public void doAfterAlterar(E vo, DAOManager manager) throws UpdateBeException {

	}

	/**
	 * Sobrescrever o metodo para operacoes relalizadas apos inserir ou alterar
	 * um registro
	 */
	public void doAfterSalvar(E vo, DAOManager manager) throws SaveBeException {

	}

	/**
	 * Sobrescrever o metodo para operacoes realizadas apos excluir um registro
	 */
	public void doAfterExcluir(E vo, DAOManager manager) throws DeleteBeException {

	}

	/**
	 * Sobrescrever o metodo para operacoes realizadas antes de inserir um
	 * registro
	 */
	public void doBeforeInserir(E vo, DAOManager manager) throws InsertBeException {

	}

	/**
	 * Sobrescrever o metodo para operacoes realizadas antes de alterar um
	 * registro
	 */
	public void doBeforeAlterar(E vo, DAOManager manager) throws UpdateBeException {

	}

	/**
	 * Sobrescrever o metodo para operacoes realizadas antes de inserir ou
	 * alterar um registro
	 */
	public void doBeforeSalvar(E vo, DAOManager manager) throws SaveBeException {

	}

	/**
	 * Sobrescrever o metodo para operacoes realizadas antes de excluir um
	 * registro
	 */
	public void doBeforeExcluir(E vo, DAOManager manager) throws DeleteBeException {

	}

	/**
	 * Essa propriedade indica se vai ser escrito um "ok" ou o proprio VO, no
	 * caso de sucesso num insert.
	 * 
	 * @param writeVoOnInsert
	 */
	public void setWriteVoOnInsert(boolean writeVoOnInsert) {
		this.writeVoOnInsert = writeVoOnInsert;
	}

	/**
	 * Essa propriedade indica se vai ser escrito um "ok" ou o proprio VO, no
	 * caso de sucesso num insert.
	 * 
	 * @return
	 */
	public boolean isWriteVoOnInsert() {
		return writeVoOnInsert;
	}

	/**
	 * Essa propriedade indica se vai ser escrito um "ok" ou o proprio VO, no
	 * caso de sucesso num update.
	 * 
	 * @param writeVoOnUpdate
	 */
	public void setWriteVoOnUpdate(boolean writeVoOnUpdate) {
		this.writeVoOnUpdate = writeVoOnUpdate;
	}

	/**
	 * Essa propriedade indica se vai ser escrito um "ok" ou o proprio VO, no
	 * caso de sucesso num update.
	 * 
	 * @return
	 */
	public boolean isWriteVoOnUpdate() {
		return writeVoOnUpdate;
	}

	public void setOperacao(String operacao) {
		this.operacao = operacao;
	}

	public String getOperacao() {
		return operacao;
	}

	protected String getNomeEntidade() {
		return getVOClass().getSimpleName().replace("Vo", "").toLowerCase();
	}

	public String getVoSessionId() {
		return getNomeEntidade() + "_cadastro_vo";
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public DAOManager getManager() {
		if (manager == null) {
			manager = DAOManager.getDAOManager();
		}
		return manager;
	}

	public void setManager(DAOManager manager) {
		this.manager = manager;
	}
}
