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

	private static String driver;
	private static String url;
	private static String user;
	private static String password;
	private static String encoding;

	public static Connection receberConexao() {

		String connProp = "mysql";
		switch (connProp) {
			case "mysql":
				user = "root";
				password = "123456";
				encoding = "UTF8";
				driver = "com.mysql.cj.jdbc.Driver";
				url = "jdbc:mysql://localhost:3306/estoque?useTimezone=true&serverTimezone=UTC";
				break;
			case "postgresql":
				user = "postgres";
				password = "123456";
				encoding = "UTF8";
				driver = "org.postgresql.Driver";
				url = "jdbc:postgresql://localhost:5432/estoque";
				break;
			case "firebird":
				user = "SYSDBA";
				password = "123456";
				encoding = "UTF8";
				driver = "org.firebirdsql.jdbc.FBDriver";
				url = "jdbc:firebirdsql://localhost:3050/d:/data/testdb2.fdb";
				break;
			default:
				break;
		}

		Properties props = new Properties();
		props.setProperty("user", user);
		props.setProperty("password", password);
		props.setProperty("encoding", encoding);

		try {
			Class.forName(driver);
			return DriverManager.getConnection(url, props);
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
