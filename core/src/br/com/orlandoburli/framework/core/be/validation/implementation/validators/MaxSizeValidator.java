package br.com.orlandoburli.framework.core.be.validation.implementation.validators;

import java.lang.reflect.Field;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;
import br.com.orlandoburli.framework.core.be.exceptions.validation.ValidationBeException;
import br.com.orlandoburli.framework.core.be.validation.ValidatorUtils;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.MaxSize;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class MaxSizeValidator extends BaseValidator {

	@Override
	public void validate(BaseVo vo, Field f, Class<BaseVo> classe) throws BeException {

		Object value = DaoUtils.getValue(DaoUtils.getGetterMethod(classe, f), vo);

		if (value == null) {
			return;
		}

		MaxSize maxSize = f.getAnnotation(MaxSize.class);

		if (maxSize == null) {
			return;
		}

		if (value instanceof String) {
			String string = (String) value;

			if (string.length() > maxSize.value()) {
				excecao(f, maxSize, string);
			}
		} else {
			String string = value.toString();
			
			if (string.length() > maxSize.value()) {
				excecao(f, maxSize, string);
			}
		}
	}

	private void excecao(Field f, MaxSize maxSize, String string) throws ValidationBeException {
		throw new ValidationBeException("O Tamanho máximo do campo " + ValidatorUtils.getFieldDescription(f) + " é de " + maxSize.value() + ". Informado: " + string.length(), f.getName());
	}

}
