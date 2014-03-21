package br.com.orlandoburli.framework.core.dao;

public class DaoControle {

	private int numeroInteracoes;

	private int maximoInteracoes;

	public DaoControle(int maximoInteracoes) {
		this.numeroInteracoes = 0;
		this.maximoInteracoes = maximoInteracoes;
	}

	public boolean isMaximo() {
		return numeroInteracoes >= maximoInteracoes;
	}

	public int getNumeroInteracoes() {
		return numeroInteracoes;
	}

	public void incrementaInteracoes() {
		numeroInteracoes++;
	}

	public int getMaximoInteracoes() {
		return maximoInteracoes;
	}

}
