package br.com.orlandoburli.framework.core.vo;

import java.util.ArrayList;
import java.util.List;

import br.com.orlandoburli.framework.core.vo.exceptions.WrongDomainException;

public abstract class BaseDomain {

	public BaseDomain() {
		if (this.getValues().length != this.getDescriptions().length) {
			throw new WrongDomainException();
		}
	}

	public abstract String[] getValues();

	public abstract String[] getDescriptions();

	public boolean isInDomain(String value) {
		if (value == null) {
			return false;
		}

		for (String s : this.getValues()) {
			if (s.equals(value)) {
				return true;
			}
		}

		return false;
	}

	public String getDescription(Object value) {
		if (value == null) {
			return null;
		}
		String[] values = this.getValues();

		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(value.toString())) {
				return this.getDescriptions()[i];
			}
		}

		return null;
	}

	public List<DomainVo> getList() {
		List<DomainVo> list = new ArrayList<DomainVo>();

		for (int i = 0; i < this.getValues().length; i++) {
			list.add(new DomainVo(this.getValues()[i], this.getDescriptions()[i]));
		}

		return list;
	}
}
