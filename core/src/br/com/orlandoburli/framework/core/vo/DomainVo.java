package br.com.orlandoburli.framework.core.vo;

public class DomainVo extends BaseVo {

	private static final long serialVersionUID = 1L;

	private String valor;
	private String descricao;

	public DomainVo(String valor, String descricao) {
		this.setValor(valor);
		this.setDescricao(descricao);
	}

	public String getValor() {
		return valor;
	}

	private void setValor(String valor) {
		this.valor = valor;
	}

	public String getDescricao() {
		return descricao;
	}

	private void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
