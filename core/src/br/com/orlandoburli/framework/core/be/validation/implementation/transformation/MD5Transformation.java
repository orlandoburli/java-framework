package br.com.orlandoburli.framework.core.be.validation.implementation.transformation;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import br.com.orlandoburli.framework.core.dao.DaoUtils;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class MD5Transformation extends BaseTransformation {

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

			string = md5(string);

			// Salva o novo valor.
			DaoUtils.setValue(DaoUtils.getSetterMethod(classe, f), vo, string);
		}
	}

	// Função para criar hash da senha informada
	public static String md5(String senha) {
		String sen = "";
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		BigInteger hash = new BigInteger(1, md.digest(senha.getBytes()));
		sen = hash.toString(16);
		return sen;
	}
}
