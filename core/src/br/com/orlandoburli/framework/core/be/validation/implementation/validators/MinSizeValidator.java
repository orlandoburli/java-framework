package br.com.orlandoburli.framework.core.be.validation.implementation.validators;

import java.lang.reflect.Field;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;
import br.com.orlandoburli.framework.core.be.exceptions.validation.ValidationBeException;
import br.com.orlandoburli.framework.core.be.validation.ValidatorUtils;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.MinSize;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class MinSizeValidator extends BaseValidator {

	@Override
	public void validate(BaseVo vo, Field f, Class<BaseVo> classe) throws BeException {

		Object value = DaoUtils.getValue(DaoUtils.getGetterMethod(classe, f), vo);

		MinSize minSize = f.getAnnotation(MinSize.class);

		if (minSize == null) {
			return;
		}

		if (value instanceof String) {
			String string = (String) value;

			if (string == null || string.length() < minSize.value()) {
				this.excecao(f, minSize, string.length());
			}
		} else {
			if (value == null) {
				return;
			}
			String string = value.toString();

			if (string.length() > 0 && string.length() < minSize.value()) {
				this.excecao(f, minSize, string.length());
			}
		}
	}

	private void excecao(Field f, MinSize minSize, int length) throws ValidationBeException {
		if (minSize.customError() != null && !minSize.customError().trim().equals("")) {
			throw new ValidationBeException(minSize.customError(), f.getName());
		} else {
			throw new ValidationBeException("O Tamanho mínimo do campo " + ValidatorUtils.getFieldDescription(f) + " é de " + minSize.value() + ". Informado: " + length, f.getName());
		}
	}

}
