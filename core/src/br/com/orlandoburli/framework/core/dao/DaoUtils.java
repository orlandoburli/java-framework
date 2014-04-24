package br.com.orlandoburli.framework.core.dao;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import br.com.orlandoburli.framework.core.be.BaseBe;
import br.com.orlandoburli.framework.core.log.Log;
import br.com.orlandoburli.framework.core.vo.BaseVo;

public class DaoUtils {

	/**
	 * Retorna uma nova instancia do objeto vo.
	 * 
	 * @param classe
	 *            Classe VO que sera criada
	 * @return Objeto VO instanciado.
	 */
	public static Object getNewObject(Class<?> classe) {

		try {
			return classe.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retorna uma nova instancia do objeto dao.
	 * 
	 * @param classe
	 *            Classe DAO que sera criada
	 * @param manager
	 *            DAOManager para o construtor
	 * @return Objeto DAO Instanciado.
	 */
	public static BaseCadastroDao<BaseVo> getNewDao(Class<BaseCadastroDao<BaseVo>> classe, DAOManager manager) {
		try {
			Constructor<BaseCadastroDao<BaseVo>> constructor = classe.getConstructor(new Class[] { DAOManager.class });

			return constructor.newInstance(manager);

		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retorna uma nova instancia do objeto be.
	 * 
	 * @param classe
	 *            Classe Be que sera criada
	 * @param manager
	 *            DAOManager para o construtor
	 * @return Objecto Be instanciado.
	 */
	public static BaseBe<BaseVo, BaseCadastroDao<BaseVo>> getNewBe(Class<BaseBe<BaseVo, BaseCadastroDao<BaseVo>>> classe, DAOManager manager) {
		try {
			Constructor<BaseBe<BaseVo, BaseCadastroDao<BaseVo>>> constructor = classe.getConstructor(new Class[] { DAOManager.class });

			return constructor.newInstance(manager);

		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Retorna o metodo getter de um atributo.
	 * 
	 * @param classe
	 *            Classe do objeto
	 * @param f
	 *            Field que se quer o getter
	 * @return Objeto Method (java.lang.reflect)
	 */
	public static Method getGetterMethod(Class<BaseVo> classe, Field f) {
		String fieldName = f.getName();
		String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

		try {
			return classe.getMethod(methodName, new Class[] {});
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retorna o metodo setter de um atributo.
	 * 
	 * @param classe
	 *            Classe do objetio
	 * @param f
	 *            Field que se quer o setter
	 * @return Objeto Method (java.lang.reflect)
	 */
	public static Method getSetterMethod(Class<?> classe, Field f) {
		String fieldName = f.getName();
		String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

		try {
			return classe.getMethod(methodName, new Class[] { f.getType() });
		} catch (NoSuchMethodException | SecurityException e) {
			Log.error(e);
		}

		return null;
	}

	/**
	 * Retorna o valor de um metodo
	 * 
	 * @param getter
	 *            Metodo getter
	 * @param vo
	 *            Objeto vo a ser chamado
	 * @return Resultado do metodo, e nulo se houver alguma excessao.
	 */
	public static Object getValue(Method getter, Object vo) {
		try {
			if (getter == null || vo == null) {
				return null;
			}
			return getter.invoke(vo, new Object[] {});
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Log.error(e);
		}

		return null;
	}

	/**
	 * Seta o valor de um atributo pelo setter
	 * 
	 * @param setter
	 *            Metodo setter
	 * @param vo
	 *            Objecto vo a ser alterado
	 * @param value
	 *            Valor a ser setado
	 */
	public static void setValue(Method setter, Object vo, Object value) {
		try {
			setter.invoke(vo, value);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			Log.fine("Class: " + vo.getClass() + " Setter: " + setter.getName() + " Value: " + value);
			Log.error(e);
		}
	}
}
