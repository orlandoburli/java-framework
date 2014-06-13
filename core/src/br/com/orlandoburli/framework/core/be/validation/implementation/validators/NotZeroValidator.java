package br.com.orlandoburli.framework.core.be.validation.implementation.validators;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;
import br.com.orlandoburli.framework.core.be.exceptions.validation.ValidationBeException;
import br.com.orlandoburli.framework.core.be.validation.ValidatorUtils;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.NotZero;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class NotZeroValidator extends BaseValidator {

	@Override
	public void validate(BaseVo vo, Field f, Class<BaseVo> classe) throws BeException {
		Object value = DaoUtils.getValue(DaoUtils.getGetterMethod(classe, f), vo);

		if (value == null) {
			return;
		}

		NotZero notZero = f.getAnnotation(NotZero.class);

		if (notZero != null) {
			if (value instanceof Integer) {
				Integer i = (Integer) value;
				if (i <= 0) {
					excecao(f);
				}
			} else if (value instanceof BigDecimal) {
				BigDecimal b = (BigDecimal) value;

				if (b.compareTo(BigDecimal.ZERO) <= 0) {
					excecao(f);
				}
			}
		}
	}

	private void excecao(Field f) throws ValidationBeException {
		throw new ValidationBeException("Campo " + ValidatorUtils.getFieldDescription(f) + " é obrigatório e deve ser maior que zero!", f.getName());
	}
}
