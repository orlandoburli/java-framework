package br.com.orlandoburli.framework.core.vo;

public abstract class BaseVo {

	private boolean isNew;

	public BaseVo() {
		setNew(true);
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public String getVoName() {
		return this.getClass().getSimpleName();
	}
}