package br.com.orlandoburli.framework.core.be.validation.implementation.transformation;

import java.lang.reflect.Field;

import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.FilterOnly;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class FilterOnlyTransformation extends BaseTransformation {

	@Override
	public void transform(BaseVo vo, Field f, Class<BaseVo> classe) {

		FilterOnly filterOnly = f.getAnnotation(FilterOnly.class);

		// Se nao achar a annotation, sai do metodo.
		if (filterOnly == null) {
			return;
		}

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
			
			String retorno = "";

			for (int i = 0; i < string.length(); i++) {
				char c = string.charAt(i);

				boolean found = false;

				for (int j = 0; j < filterOnly.value().length(); j++) {
					char charFilter = filterOnly.value().charAt(j);

					if (c == charFilter) {
						found = true;
						break;
					}
				}

				if (found) {
					retorno += c;
				}

			}

			// Salva o novo valor.
			DaoUtils.setValue(DaoUtils.getSetterMethod(classe, f), vo, retorno);
		}
	}

}
