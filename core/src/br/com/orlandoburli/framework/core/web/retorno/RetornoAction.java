package br.com.orlandoburli.framework.core.web.retorno;

import br.com.orlandoburli.framework.core.utils.Utils;

public class RetornoAction {

	private boolean sucesso;
	private String mensagem;
	private String fieldFocus;
	private String codigoRetorno;
	private Object objeto;

	public RetornoAction(boolean sucesso, String mensagem) {
		this.sucesso = sucesso;
		this.mensagem = mensagem;
	}

	public RetornoAction(boolean sucesso, String mensagem, String fieldFocus) {
		this.sucesso = sucesso;
		this.mensagem = mensagem;
		this.fieldFocus = fieldFocus;
	}

	public RetornoAction(boolean sucesso, String mensagem, String fieldFocus, String codigoRetorno) {
		this.sucesso = sucesso;
		this.mensagem = mensagem;
		this.fieldFocus = fieldFocus;
		this.codigoRetorno = codigoRetorno;
	}

	public RetornoAction(boolean sucesso, String mensagem, String fieldFocus, Object objeto) {
		this.sucesso = sucesso;
		this.mensagem = mensagem;
		this.fieldFocus = fieldFocus;
		this.objeto = objeto;
	}

	public RetornoAction(boolean sucesso, String mensagem, Object objeto) {
		this.sucesso = sucesso;
		this.mensagem = mensagem;
		this.objeto = objeto;
	}

	public String toJson() {
		return Utils.voToJson(this);
	}

	public boolean isSucesso() {
		return this.sucesso;
	}

	public void setSucesso(boolean sucesso) {
		this.sucesso = sucesso;
	}

	public String getMensagem() {
		return this.mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public String getFieldFocus() {
		return this.fieldFocus;
	}

	public void setFieldFocus(String fieldFocus) {
		this.fieldFocus = fieldFocus;
	}

	public String getCodigoRetorno() {
		return this.codigoRetorno;
	}

	public void setCodigoRetorno(String codigoRetorno) {
		this.codigoRetorno = codigoRetorno;
	}

	public Object getObjeto() {
		return this.objeto;
	}

	public void setObjeto(Object objeto) {
		this.objeto = objeto;
	}
}
