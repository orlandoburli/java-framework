package br.com.orlandoburli.framework.core.web;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.orlandoburli.framework.core.utils.Utils;
import br.com.orlandoburli.framework.core.web.filters.OutjectionFilter;
import br.com.orlandoburli.framework.core.web.retorno.RetornoAction;

public class BaseAction implements Serializable {

	private static final long serialVersionUID = 1L;

	private HttpServletRequest request;
	private HttpServletResponse response;
	private ServletContext context;
	private String methodName;

	public void forward(String url) {
		this.dispatch();

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		RequestDispatcher dispatcher = this.request.getRequestDispatcher(url);

		try {
			dispatcher.forward(this.request, this.response);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void redir(String url) {
		this.dispatch();

		try {
			this.response.sendRedirect(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dispatch() {
		// Filtro de Outjection (saida de dados)
		OutjectionFilter ofilter = new OutjectionFilter();
		ofilter.setContext(this.context);
		ofilter.setRequest(this.request);
		ofilter.setResponse(this.response);

		try {
			if (!ofilter.doFilter(this)) {
				return;
			}
		} catch (IllegalArgumentException e1) {
		} catch (IllegalAccessException e1) {
		}
	}

	public Object getAttribute(String key) {
		if (this.request != null) {
			if (this.request.getMethod().equalsIgnoreCase("POST")) {
				if (this.request.getAttribute(key) != null) {
					return this.request.getAttribute(key);
				} else {
					return this.request.getParameter(key);
				}
			} else {
				return this.request.getParameter(key);
			}
		} else {
			return null;
		}
	}

	/**
	 * Escreve na saida usando o encoding ISO-8859-1
	 * 
	 * @param value
	 */
	public void writeIso88591(String value) {
		this.write(value, "ISO-8859-1");
	}

	/**
	 * Escreve na saida usando o encoding especificado
	 * 
	 * @param value
	 * @param Encoding
	 */
	public void write(String value, String Encoding) {
		try {
			this.response.setCharacterEncoding(Encoding);
			this.response.getWriter().write(value);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Escreve na saida usando o encoding UTF-8
	 * 
	 * @param value
	 */
	public void write(String value) {
		this.write(value, "UTF-8");
	}

	public void writeErrorMessage(String errormessage) {
		this.writeErrorMessage(errormessage, null);
	}

	public void writeErrorMessage(String errormessage, String fieldName) {
		RetornoAction retorno = new RetornoAction(false, errormessage, fieldName);
		this.write(Utils.voToJson(retorno));
	}

	public void setAttribute(String key, Object value) {
		if (this.request != null) {
			this.request.setAttribute(key, value);
		}
	}

	public String getActionName() {
		return this.getClass().getSimpleName().replace("Action", "").toLowerCase();
	}

	public boolean isPost() {
		return this.request.getMethod().equalsIgnoreCase("POST");
	}

	public HttpServletRequest getRequest() {
		return this.request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return this.response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public ServletContext getContext() {
		return this.context;
	}

	public void setContext(ServletContext context) {
		this.context = context;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
}
