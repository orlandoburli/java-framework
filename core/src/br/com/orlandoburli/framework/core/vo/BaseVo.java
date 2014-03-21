package br.com.orlandoburli.framework.core.vo;

import br.com.orlandoburli.framework.core.utils.Utils;

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
	
	@Override
	public String toString() {
		return Utils.voToJson(this);
	}
}