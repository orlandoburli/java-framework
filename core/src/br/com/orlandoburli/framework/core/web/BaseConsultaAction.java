package br.com.orlandoburli.framework.core.web;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.orlandoburli.framework.core.be.BaseBe;
import br.com.orlandoburli.framework.core.be.exceptions.persistence.ListException;
import br.com.orlandoburli.framework.core.dao.BaseCadastroDao;
import br.com.orlandoburli.framework.core.dao.DAOManager;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;
import br.com.orlandoburli.framework.core.web.filters.IgnoreMethodAuthentication;
import br.com.orlandoburli.framework.core.web.filters.InjectionFilter;

public abstract class BaseConsultaAction<E extends BaseVo, F extends BaseCadastroDao<E>, G extends BaseBe<E, F>> extends BaseAction {

	private static final long serialVersionUID = 1L;

	private int PageSize;
	private int PageNumber;

	private String opcao;

	private String PesquisarPor;
	private String ParametroPesquisa;

	private DAOManager manager;

	public void execute() {
		this.doBeforeExecute();
		if (this.getOpcao() != null && this.getOpcao().equalsIgnoreCase("grid")) {
			this.grid();
		} else {
			forward(this.getJspConsulta());
		}
	}

	public void doBeforeExecute() {

	}

	@SuppressWarnings("unchecked")
	@IgnoreMethodAuthentication
	public void grid() {

		try {
			G be = (G) DaoUtils.getNewBe((Class<BaseBe<BaseVo, BaseCadastroDao<BaseVo>>>) this.getBEClass(), this.getManager());

			E filter = (E) this.getVOClass().newInstance();

			StringBuilder whereCondition = new StringBuilder();

			this.doBeforeFilter(filter, be, getRequest(), getResponse(), whereCondition);

			try {
				List<E> list = be.getList(filter, whereCondition.toString(), this.getOrderFields(), this.getPageNumber(), this.getPageSize());
				int pageCount = be.getPageCount(filter, whereCondition.toString(), this.getPageSize());

				this.doBeforeSetList(list, pageCount, this.getPageSize(), this.getPageNumber());

				getRequest().setAttribute("listSource", list);
				getRequest().setAttribute("pageNumber", this.getPageNumber());
				getRequest().setAttribute("pageSize", this.getPageSize());
				getRequest().setAttribute("pageCount", pageCount);
			} catch (ListException e) {
				e.printStackTrace();
				getRequest().setAttribute("mensagemErro", e.getMessage());
			}

			InjectionFilter injection = new InjectionFilter();
			injection.setContext(getContext());
			injection.setRequest(getRequest());
			injection.setResponse(getResponse());
			injection.doFilter(this);

			forward(this.getJspGridConsulta());

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} finally {
			this.getManager().commit();
		}
	}

	public void doBeforeSetList(List<E> list, int pageCount, int pageSize, int pageNumber) throws ListException {

	}

	protected Class<?> getVOClass() {
		return ((Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
	}

	protected Class<?> getDAOClass() {
		return ((Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1]);
	}

	protected Class<?> getBEClass() {
		return ((Class<?>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[2]);
	}

	public abstract String getJspConsulta();

	public abstract String getJspGridConsulta();

	public abstract String getOrderFields();

	public abstract void doBeforeFilter(E filter, G be, HttpServletRequest request, HttpServletResponse response, StringBuilder whereCondition);

	public int getPageSize() {
		return this.PageSize;
	}

	public void setPageSize(int pageSize) {
		this.PageSize = pageSize;
	}

	public int getPageNumber() {
		return this.PageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.PageNumber = pageNumber;
	}

	public String getOpcao() {
		return this.opcao;
	}

	public void setOpcao(String opcao) {
		this.opcao = opcao;
	}

	public String getPesquisarPor() {
		return this.PesquisarPor == null ? "" : this.PesquisarPor;
	}

	public void setPesquisarPor(String pesquisarPor) {
		this.PesquisarPor = pesquisarPor;
	}

	public String getParametroPesquisa() {
		return this.ParametroPesquisa;
	}

	public void setParametroPesquisa(String parametroPesquisa) {
		this.ParametroPesquisa = parametroPesquisa;
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
}