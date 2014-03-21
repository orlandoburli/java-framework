package br.com.orlandoburli.framework.core.be.validation.implementation.transformation;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.ZeroIfNull;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class ZeroIfNullTransformation extends BaseTransformation {

	@Override
	public void transform(BaseVo vo, Field f, Class<BaseVo> classe) {
		// Se o campo ou o VO forem nulos, sai do metodo.
		if (vo == null || f == null) {
			return;
		}

		// Se a classe da anotacao nao for BigDecimal ou Integer, sai do metodo.
		if (!f.getType().equals(BigDecimal.class) && !f.getType().equals(Integer.class)) {
			return;
		}

		ZeroIfNull zeroIfNull = f.getAnnotation(ZeroIfNull.class);

		if (zeroIfNull == null) {
			return;
		}

		Object value = DaoUtils.getValue(DaoUtils.getGetterMethod(classe, f), vo);

		// Somente se o valor for nulo.
		if (value == null) {
			if (f.getType().equals(BigDecimal.class)) {
				DaoUtils.setValue(DaoUtils.getSetterMethod(classe, f), vo, BigDecimal.ZERO);
			} else if (f.getType().equals(Integer.class)) {
				DaoUtils.setValue(DaoUtils.getSetterMethod(classe, f), vo, new Integer(0));
			}
		}
	}

}
