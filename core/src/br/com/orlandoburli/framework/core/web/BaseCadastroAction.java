package br.com.orlandoburli.framework.core.web;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.orlandoburli.framework.core.be.BaseBe;
import br.com.orlandoburli.framework.core.be.exceptions.BeException;
import br.com.orlandoburli.framework.core.be.exceptions.RotinaNaoImplementadaException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.DeleteBeException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.InsertBeException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.ListException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.SaveBeException;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.UpdateBeException;
import br.com.orlandoburli.framework.core.dao.BaseCadastroDao;
import br.com.orlandoburli.framework.core.dao.DAOManager;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.utils.Utils;
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
			return (E) this.getVOClass().newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected G getNewBe(DAOManager manager) {
		return (G) DaoUtils.getNewBe((Class<BaseBe<BaseVo, BaseCadastroDao<BaseVo>>>) this.getBEClass(), manager);
	}

	public void inserir() {
		E vo = getVoSession() == null ? this.getNewVo() : getVoSession();
		G be = this.getNewBe(this.getManager());

		try {
			this.getManager().begin();

			this.injectVo(vo);

			this.doBeforeInserir(vo, this.getManager());
			this.doBeforeSalvar(vo, this.getManager());

			be.save(vo);

			this.doAfterInserir(vo, this.getManager());
			this.doAfterSalvar(vo, this.getManager());

			this.getManager().commit();

			this.write(Utils.voToJson(new RetornoAction(true, "Registro inserido com sucesso!")));

		} catch (BeException e) {
			this.getManager().rollback();
			this.write(Utils.voToJson(new RetornoAction(false, e.getMessage(), e.getField())));
		} finally {
			this.getManager().commit();
		}
	}

	public void alterar() {
		DAOManager manager = DAOManager.getInstance();
		try {
			manager.begin();

			@SuppressWarnings("unchecked")
			E vo = (E) getRequest().getSession().getAttribute(this.getVoSessionId()); // getNewVo();
			G be = this.getNewBe(manager);

			this.doBeforeAlterar(vo, manager);
			this.doBeforeSalvar(vo, manager);

			this.injectVo(vo);

			this.doAfterAlterar(vo, manager);
			this.doAfterSalvar(vo, manager);

			be.save(vo);

			manager.commit();

			this.write(Utils.voToJson(new RetornoAction(true, "Registro alterado com sucesso!", "")));

		} catch (BeException e) {
			manager.rollback();
			this.write(Utils.voToJson(new RetornoAction(false, e.getMessage(), e.getField())));
		} finally {
			manager.commit();
		}
	}

	public void excluir() {
		DAOManager manager = DAOManager.getInstance();
		try {
			manager.begin();

			E vo = this.getNewVo();
			G be = this.getNewBe(manager);

			this.doBeforeExcluir(vo, manager);

			this.injectVo(vo);

			this.doAfterExcluir(vo, manager);

			be.remove(vo);

			this.write(Utils.voToJson(new RetornoAction(true, "Registro excluído com sucesso!", "")));

			manager.commit();
		} catch (BeException e) {
			manager.rollback();
			this.write(Utils.voToJson(new RetornoAction(false, e.getMessage(), e.getField())));
		} finally {
			manager.commit();
		}
	}

	public void visualizar() {
		DAOManager manager = DAOManager.getInstance();

		try {

			G be = this.getNewBe(manager);

			E vo = this.getNewVo();

			this.injectVo(vo);

			this.doBeforeVisualizar(getRequest(), getResponse(), vo, be, manager);

			vo = be.get(vo);

			this.doBeforeWriteVo(vo);

			getRequest().setAttribute("vo", vo);

			getRequest().getSession().setAttribute(this.getVoSessionId(), vo);

			// Disabled especiais
			if (this.operacao.equalsIgnoreCase("alterar")) {
				getRequest().setAttribute("disabledAlterar", "disabled=\"disabled\"");
			}
			if (this.operacao.equalsIgnoreCase("inserir")) {
				getRequest().setAttribute("disabledInserir", "disabled=\"disabled\"");
			}

			forward(this.getJspCadastro());

		} catch (ListException e) {
			this.write(Utils.voToJson(new RetornoAction(false, e.getMessage(), e.getField())));
		} finally {
			manager.commit();
		}
	}

	public void vo() {
		DAOManager manager = DAOManager.getInstance();

		try {

			G be = this.getNewBe(manager);

			E vo = this.getNewVo();

			this.injectVo(vo);

			this.doBeforeVisualizar(getRequest(), getResponse(), vo, be, manager);

			vo = be.get(vo);

			this.doBeforeWriteVo(vo);

			this.write(Utils.voToJson(vo));

		} catch (ListException e) {
			Utils.voToJson(new RetornoAction(false, e.getMessage(), e.getField()));
		} finally {
			manager.commit();
		}
	}

	@SuppressWarnings("unchecked")
	public E getVoSession() {
		return (E) getRequest().getSession().getAttribute(getVoSessionId());
	}

	public void setVoSession(E vo) {
		getRequest().getSession().setAttribute(getVoSessionId(), vo);
	}

	public void injectVo(BaseVo vo) {
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

	public void doBeforeVisualizar(HttpServletRequest request, HttpServletResponse response, E vo, G be, DAOManager manager) throws ListException {

	}

	public void doBeforeWriteVo(E vo) {
	}

	public void consultar() {
		this.visualizar();
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
	 *
	 * @throws BeException
	 */
	public void doAfterSalvar(E vo, DAOManager manager) throws SaveBeException, BeException {

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
	 *
	 * @throws BeException
	 */
	public void doBeforeAlterar(E vo, DAOManager manager) throws UpdateBeException, BeException {

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
		return this.writeVoOnInsert;
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
		return this.writeVoOnUpdate;
	}

	public void setOperacao(String operacao) {
		this.operacao = operacao;
	}

	public String getOperacao() {
		if (this.operacao == null) {
			this.operacao = "";
		}
		return this.operacao;
	}

	protected String getNomeEntidade() {
		return this.getVOClass().getSimpleName().replace("Vo", "").toLowerCase();
	}

	public String getVoSessionId() {
		return this.getNomeEntidade() + "_cadastro_vo";
	}

	public String getTerm() {
		return this.term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public DAOManager getManager() {
		if (this.manager == null) {
			this.manager = DAOManager.getInstance();
		}
		return this.manager;
	}

	public void setManager(DAOManager manager) {
		this.manager = manager;
	}

	public void rapido() {
		throw new RotinaNaoImplementadaException("Rotina de cadastro rápido não implementada nesta classe!");
	}
}
