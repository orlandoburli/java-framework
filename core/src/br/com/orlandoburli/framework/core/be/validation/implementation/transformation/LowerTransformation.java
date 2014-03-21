package br.com.orlandoburli.framework.core.be.validation.implementation.transformation;

import java.lang.reflect.Field;

import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class LowerTransformation extends BaseTransformation {

	@Override
	public void transform(BaseVo vo, Field f, Class<BaseVo> classe) {
		// Se o campo ou o VO forem nulos, sai do metodo.
		if (vo == null || f == null) {
			return;
		}

		// Se a classe da anotacao nao for String, sai do metodo.
		if (!f.getType().equals(String.class)) {
			return;
		}

		Object value = DaoUtils.getValue(DaoUtils.getGetterMethod(classe, f), vo);

		// Se o valor do field for Null, sai do metodo.
		if (value != null) {
			String string = (String) value;

			string = string.toLowerCase();

			// Salva o novo valor.
			DaoUtils.setValue(DaoUtils.getSetterMethod(classe, f), vo, string);
		}
	}
}
