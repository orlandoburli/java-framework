package br.com.orlandoburli.framework.core.web.filters;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import br.com.orlandoburli.framework.core.log.Log;

public class OutjectionFilter extends BaseFilter {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean doFilter(Object facade) throws IllegalArgumentException, IllegalAccessException {
		Log.info("Filtro de Outjection");

		Method[] methods = facade.getClass().getMethods();

		for (Method method : methods) {
			if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
				if (!method.getReturnType().equals(Void.class) && method.getParameterTypes().length == 0) {
					String attName = method.getName().startsWith("get") ? method.getName().substring(3) : method.getName().substring(2); // retira
					// o
					// "get"
					// ou
					// o
					// "is"
					try {
						// Seta na saida
						getRequest().setAttribute(attName.toLowerCase(), method.invoke(facade, new Object[] {}));
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// Atributos a serem salvos em sessao
		Field[] fields = facade.getClass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			OutjectSession session = field.getAnnotation(OutjectSession.class);
			if (session != null) {
				Log.fine("Setando valor na sessão: " + session.value() + " valor: " + field.get(facade));
				getRequest().getSession().setAttribute(session.value(), field.get(facade));
			}
		}

		// Atributos a serem salvos em sessao (classe pai)
		fields = facade.getClass().getSuperclass().getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			OutjectSession session = field.getAnnotation(OutjectSession.class);
			if (session != null) {
				Log.fine("Setando valor na sessão: " + field.getName() + " valor: " + field.get(facade));
				getRequest().getSession().setAttribute(field.getName(), field.get(facade));
			}
		}

		return true;
	}
}
