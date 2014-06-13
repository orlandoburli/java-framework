package br.com.orlandoburli.framework.core.be.validation.implementation.validators;

import java.lang.reflect.Field;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;
import br.com.orlandoburli.framework.core.be.exceptions.validation.ValidationBeException;
import br.com.orlandoburli.framework.core.be.validation.ValidatorUtils;
import br.com.orlandoburli.framework.core.be.validation.annotations.validators.NotEmpty;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class NotEmptyValidator extends BaseValidator {

	@Override
	public void validate(BaseVo vo, Field f, Class<BaseVo> classe) throws BeException {
		Object value = DaoUtils.getValue(DaoUtils.getGetterMethod(classe, f), vo);

		if (value == null) {
			return;
		}

		NotEmpty notEmpty = f.getAnnotation(NotEmpty.class);

		if (notEmpty != null) {
			String string = value.toString();

			if (string.trim().equals("")) {
				throw new ValidationBeException("Campo " + ValidatorUtils.getFieldDescription(f) + " é obrigatório!", f.getName());
			}
		}
	}
}