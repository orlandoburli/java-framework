package br.com.orlandoburli.framework.core.be.validation.implementation.transformation;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import br.com.orlandoburli.framework.core.be.validation.annotations.transformation.Precision;
import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class PrecisionTransformation extends BaseTransformation {

	@Override
	public void transform(BaseVo vo, Field f, Class<BaseVo> classe) {
		// Se o campo ou o VO forem nulos, sai do metodo.
		if (vo == null || f == null) {
			return;
		}

		// Se a classe da anotacao nao for BigDecimal, sai do metodo.
		if (!f.getType().equals(BigDecimal.class)) {
			return;
		}
		
		Precision precision = f.getAnnotation(Precision.class);
		
		if (precision == null) {
			return;
		}

		Object value = DaoUtils.getValue(DaoUtils.getGetterMethod(classe, f), vo);

		// Se o valor do field for Null, sai do metodo.
		if (value != null) {
			BigDecimal number = (BigDecimal) value;

			number = number.setScale(precision.value(), BigDecimal.ROUND_HALF_EVEN);

			// Salva o novo valor.
			DaoUtils.setValue(DaoUtils.getSetterMethod(classe, f), vo, number);
		}
	}

}
