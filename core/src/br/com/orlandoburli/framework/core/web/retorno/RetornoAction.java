package br.com.orlandoburli.framework.core.web.retorno;

public class RetornoAction {

	private boolean sucesso;
	private String mensagem;
	private String fieldFocus;
	private String codigoRetorno;

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

	public boolean isSucesso() {
		return sucesso;
	}

	public void setSucesso(boolean sucesso) {
		this.sucesso = sucesso;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public String getFieldFocus() {
		return fieldFocus;
	}

	public void setFieldFocus(String fieldFocus) {
		this.fieldFocus = fieldFocus;
	}

	public String getCodigoRetorno() {
		return codigoRetorno;
	}

	public void setCodigoRetorno(String codigoRetorno) {
		this.codigoRetorno = codigoRetorno;
	}
}
