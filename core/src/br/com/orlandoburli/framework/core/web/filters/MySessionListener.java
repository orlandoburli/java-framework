package br.com.orlandoburli.framework.core.web.filters;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


public class MySessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent e) {
		System.out.print("Sess��o iniciada. Id da sess��o: ");
		System.out.println(e.getSession().getId());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent e) {
		System.out.println("Fim da sess��o : " + e.getSession().getId());
	}
}