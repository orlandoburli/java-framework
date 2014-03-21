package br.com.orlandoburli.framework.core.vo;

import br.com.orlandoburli.framework.core.vo.exceptions.WrongDomainException;

public abstract class BaseDomain {

	public BaseDomain() {
		if (getValues().length != getDescriptions().length) {
			throw new WrongDomainException();
		}
	}

	public abstract String[] getValues();

	public abstract String[] getDescriptions();

	public boolean isInDomain(String value) {
		if (value == null) {
			return false;
		}

		for (String s : getValues()) {
			if (s.equals(value)) {
				return true;
			}
		}

		return false;
	}

	public String getDescription(String value) {
		String[] values = getValues();
		
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(value)) {
				return getDescriptions()[i];
			}
		}

		return null;
	}
}
