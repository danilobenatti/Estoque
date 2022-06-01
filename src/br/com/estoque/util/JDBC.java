package br.com.estoque.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Romuel Dias de Oliveira - Java Full Teoria e Prática - pg.270
 */
public class JDBC {

	public static Connection receberConexao() {
//		final String DRIVER = "com.mysql.cj.jdbc.Driver";
//		final String URL = "jdbc:mysql://localhost:3306/estoque?useTimezone=true&serverTimezone=UTC";
//		final String USER = "root";
//		final String PASSWORD = "123456";
		final String DRIVER = "org.firebirdsql.jdbc.FBDriver";
		final String URL = "jdbc:firebirdsql://localhost:3050/D:/DATA/TESTDB2.FDB";
		final String USER = "SYSDBA";
		final String PASSWORD = "123456";
		Properties properties = new Properties();
		properties.setProperty("user", USER);
		properties.setProperty("password", PASSWORD);
		properties.setProperty("encoding", "UTF8");
		try {
			Class.forName(DRIVER);
			return DriverManager.getConnection(URL, properties);
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(JDBC.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	private static void fechar(Connection conn, Statement statement, ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			Logger.getLogger(JDBC.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	private static void fechar(Connection conn, PreparedStatement preparedStatement, ResultSet resultSet) {
		try {
			if (resultSet != null) {
				resultSet.close();
			}
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			Logger.getLogger(JDBC.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	public static void fecharConexao(Connection conn, Statement statement, ResultSet resultSet) {
		fechar(conn, statement, resultSet);
	}

	public static void fecharConexao(Connection conn, PreparedStatement preparedStatement, ResultSet resultSet) {
		fechar(conn, preparedStatement, resultSet);
	}

	public static void fecharConexao(Connection conn, Statement statement) {
		fechar(conn, statement, null);
	}

	public static void fecharConexao(Connection conn, PreparedStatement preparedStatement) {
		fechar(conn, preparedStatement, null);
	}

	public static void fecharConexao(Connection conn) {
		fechar(conn, null, null);
	}
}
