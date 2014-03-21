package br.com.orlandoburli.framework.core.be.validation.implementation.validators;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.orlandoburli.framework.core.be.exceptions.BeException;
import br.com.orlandoburli.framework.core.be.exceptions.validation.ValidationBeException;
import br.com.orlandoburli.framework.core.be.validation.ValidatorUtils;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class EmailValidator extends BaseValidator {

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

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
				if (!validate(string)) {
					throw new ValidationBeException("Campo " + ValidatorUtils.getFieldDescription(f) + " não é um email válido!");
				}
			}
		}
	}

	public static boolean validate(final String hex) {
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(hex);
		return matcher.matches();

	}
}
