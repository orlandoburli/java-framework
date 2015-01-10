package br.com.orlandoburli.framework.core.dao;

import java.util.ConcurrentModificationException;

import br.com.orlandoburli.framework.core.log.Log;

public class DAOManagerThread extends Thread {

	public DAOManagerThread() {
		super();
		this.setName("Thread DAOManager - Checagem de DAO's abertas");
	}

	@Override
	public void run() {

		Log.fine("Iniciando DAOManagerThread");

		while (true) {
			try {
				processa();

				// Aguarda X minutos ate a proxima tentativa
				// X minutos X 60 segundos X 1000 millisegundos

				Integer timeSleep = Integer.parseInt(System.getProperty("dao.manager.thread.interval"));

				Thread.sleep(timeSleep * 60 * 1000);
			} catch (InterruptedException e) {
				Log.fine("Thread DAOManagerThread interrompida...");
			} catch (ConcurrentModificationException e) {
				Log.critical("Erro de modificacao na lista de DAOManager's...");
			}
		}
	}

	private synchronized void processa() {
		Log.fine("Inicio checagem do DAOManager - " + DAOManager.pool.size() + " objetos no pool");

		DAOManager[] array = DAOManager.pool.toArray(new DAOManager[0]);

		for (DAOManager m : array) {

			if (m.isExpired()) {

				Log.debug("DAOManager expirado, expurgando...");
				Log.debug("Caller do DAOManager: " + m.getStackElement().getClassName() + "(" + m.getStackElement().getLineNumber() + ")");

				m.die();

				DAOManager.pool.remove(m);
			}
		}

	}
}
