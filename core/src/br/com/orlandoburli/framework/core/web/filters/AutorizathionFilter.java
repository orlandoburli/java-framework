package br.com.orlandoburli.framework.core.web.filters;

import br.com.orlandoburli.framework.core.log.Log;
import br.com.orlandoburli.framework.core.utils.Constants;
import br.com.orlandoburli.framework.core.web.BaseAction;

public class AutorizathionFilter extends BaseFilter {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean doFilter(Object obj) throws IllegalArgumentException, IllegalAccessException {
		if (!(obj instanceof BaseAction)) {
			return true;
		}

		BaseAction action = (BaseAction) obj;

		if (action.getClass().getAnnotation(IgnoreFacadeAuthentication.class) != null) {
			return true;
		}

		if (action.getActionName().equalsIgnoreCase("login") || action.getActionName().equalsIgnoreCase("loginflex")) {
			return true;
		}

		// Verifica se o usuario esta logado
		if (this.getRequest().getSession().getAttribute(Constants.Session.SESSION_USUARIO) == null) {
			Log.debug("Usuario nao logado!");
			return false;
		}

		// try {
		// Method method = action.getClass().getMethod(action.getMethodName(),
		// new Class<?>[] {});
		// if (method.getAnnotation(IgnoreMethodAuthentication.class) !=
		// null) {
		// return true;
		// }
		// } catch (SecurityException e) {
		// e.printStackTrace();
		// } catch (NoSuchMethodException e) {
		// e.printStackTrace();
		// }

		return true;
	}
}