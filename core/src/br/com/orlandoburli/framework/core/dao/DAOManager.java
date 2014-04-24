package br.com.orlandoburli.framework.core.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import br.com.orlandoburli.framework.core.log.Log;

public class DAOManager {

	protected static List<DAOManager> pool = new ArrayList<DAOManager>();

	static {
		// Thread para verificar o tempo de vida do Daomanager, e se nao tem
		// conexoes abertas
		DAOManagerThread thread = new DAOManagerThread();
		thread.start();
	}

	public static DAOManager getDAOManager() {
		DAOManager daoManager = new DAOManager();
		
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		int linha = 2;
		
		daoManager.setStackElement(stackTrace[linha]);

		pool.add(daoManager);

		return daoManager;
	}


	private Connection connection;
	private Calendar aliveTime;

	private boolean dead = false;

	private StackTraceElement stackElement;

	private DAOManager() {
		alive();
	}

	private void alive() {
		if (isDead()) {
			throw new RuntimeException("Class DAOManager está MORTA!");
		}
		this.aliveTime = Calendar.getInstance();
	}

	/**
	 * Verifica se o manager ja passou do tempo de "expirar".
	 * 
	 * @return manager expirado
	 */
	protected boolean isExpired() {
		long now = Calendar.getInstance().getTimeInMillis() / 60 / 1000;
		long alive = aliveTime.getTimeInMillis() / 60 / 1000;
		
		long difference = now - alive;
		
		int maxTimeOut = Integer.parseInt(System.getProperty("dao.manager.thread.timeout"));
		
		// Se a diferenca for maior que maxTimeOut, quer dizer que esta inativo a (maxTimeOut) minutos.
		// Neste caso, a execucao deve morrer.
		if (difference > maxTimeOut) {
			return true;
		}

		return false;
	}

	/**
	 * Metodo que "Mata" o DAOManager, para que ninguem mais o use
	 */
	protected void die() {
		rollback();
		this.setDead(true);
	}

	public void begin() {
		alive();

		if (this.connection == null) {
			try {
				this.connection = getNewConnection();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (NamingException e) {
				e.printStackTrace();
			}
		}
	}

	public void commit() {
		alive();
		if (connection != null) {
			try {
				if (System.getProperty("debug.sql").equals("true")) {
					Log.debugsql("COMMIT");
				}
				connection.commit();
				connection.close();
				connection = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void rollback() {
		alive();
		if (connection != null) {
			try {
				if (System.getProperty("debug.sql").equals("true")) {
					Log.debugsql("ROLLBACK");
				}
				connection.rollback();
				connection.close();
				connection = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void savepoint() {
		alive();
		try {
			connection.setSavepoint();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Connection getNewConnection() throws ClassNotFoundException, SQLException, NamingException {
		alive();

		if (System.getProperty("db.type").equalsIgnoreCase("datasource")) {
			Log.debugsql("NEW CONNECTION - DATA SOURCE");

			InitialContext cxt = new InitialContext();

			String dsName = System.getProperty("db.datasourcename");

			DataSource ds = (DataSource) cxt.lookup(dsName);

			if (ds == null) {
				return null;
			}
			Connection conn = ds.getConnection();

			conn.setAutoCommit(false);
			// conn.prepareStatement("set application_name = '';").execute();
			return conn;
		} else {
			Log.debugsql("NEW CONNECTION - LOCAL");

			Class.forName(System.getProperty("db.classdriver"));
			String url = System.getProperty("db.url");
			Connection conn = null;
			conn = DriverManager.getConnection(url, System.getProperty("db.user"), System.getProperty("db.pass"));
			conn.setAutoCommit(false);
			return conn;
		}
	}

	public Connection getConnection() {
		alive();

		begin();
		return this.connection;
	}

	public boolean isDead() {
		return dead;
	}

	private void setDead(boolean dead) {
		this.dead = dead;
	}

	public StackTraceElement getStackElement() {
		return stackElement;
	}

	private void setStackElement(StackTraceElement stackElement) {
		this.stackElement = stackElement;
	}
}