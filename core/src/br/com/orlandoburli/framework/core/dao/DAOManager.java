package br.com.orlandoburli.framework.core.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import br.com.orlandoburli.framework.core.log.Log;

public class DAOManager {

	private Connection connection;

	public DAOManager() {

	}

	public void begin() {
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
		try {
			connection.setSavepoint();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Connection getNewConnection() throws ClassNotFoundException, SQLException, NamingException {
		
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
		begin();
		return this.connection;
	}
}