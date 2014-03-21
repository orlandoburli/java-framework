package br.com.orlandoburli.framework.core.be.validation.implementation.validators;

import java.lang.reflect.Field;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;
import br.com.orlandoburli.framework.core.be.exceptions.validation.ValidationBeException;
import br.com.orlandoburli.framework.core.be.validation.ValidatorUtils;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.Domain;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseDomain;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class DomainValidator extends BaseValidator {

	@Override
	public void validate(BaseVo vo, Field f, Class<BaseVo> classe) throws BeException {

		// Se o vo for nulo ou o field for nulo, sai do metodo.
		if (vo == null || f == null || classe == null) {
			return;
		}

		Domain domainAnnotation = f.getAnnotation(Domain.class);

		Class<?> domainClass = domainAnnotation.value();

		if (!domainClass.getSuperclass().equals(BaseDomain.class)) {
			return;
		}

		BaseDomain domain = null;

		try {
			domain = (BaseDomain) domainClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			return;
		}

		if (f.getType().equals(String.class)) {
			String string = (String) DaoUtils.getValue(DaoUtils.getGetterMethod(classe, f), vo);

			domain.getValues();

			if (!domain.isInDomain(string)) {
				String camposPermitidos = "";

				for (String s : domain.getValues()) {
					camposPermitidos += s + ", ";
				}

				camposPermitidos = camposPermitidos.substring(0, camposPermitidos.length() - 2);

				throw new ValidationBeException("Valor '" + string + "' não permitido em " + ValidatorUtils.getFieldDescription(f) + "! Valores permitidos são " + camposPermitidos + ".");
			}
		}
	}
}
