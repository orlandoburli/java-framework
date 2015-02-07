package br.com.orlandoburli.framework.core.be.validation.implementation.validators;

import java.lang.reflect.Field;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;
import br.com.orlandoburli.framework.core.be.exceptions.validation.ValidationBeException;
import br.com.orlandoburli.framework.core.be.validation.utils.CNPJUtils;
import br.com.orlandoburli.framework.core.be.validation.utils.CPFUtils;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class CpfCnpjValidator extends BaseValidator {

	@Override
	public void validate(BaseVo vo, Field f, Class<BaseVo> classe) throws BeException {
		// Se o vo for nulo ou o field for nulo, sai do metodo.
		if (vo == null || f == null || classe == null) {
			return;
		}

		// So valida se o field for do tipo string.
		if (f.getType().equals(String.class)) {
			String string = (String) DaoUtils.getValue(DaoUtils.getGetterMethod(classe, f), vo);

			// So valida se a string nao for nula e nem vazia
			if (string != null && !string.trim().equals("")) {

				if (string.length() == 11) {
					if (!CPFUtils.isCPF(string)) {
						throw new ValidationBeException("CPF inválido!", f.getName());
					}
				} else if (string.length() == 14) {
					if (!CNPJUtils.isCNPJ(string)) {
						throw new ValidationBeException("CNPJ inválido!", f.getName());
					}
				} else {
					throw new ValidationBeException("CPF/CNPJ inválido!", f.getName());
				}
			}
		}
	}
}
