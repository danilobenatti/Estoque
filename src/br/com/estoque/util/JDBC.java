package br.com.estoque.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Romuel Dias de Oliveira - Java Full Teoria e Prática - pg.270
 */
public class JDBC {

	public static Connection receberConexao() {
		final String DRIVER = "com.mysql.cj.jdbc.Driver";
		final String USER = "root";
		final String PASSWORD = "123456";
		final String URL = "jdbc:mysql://localhost/estoque?"
				+ "useTimezone=true&serverTimezone=UTC"
				+ "&user=" + USER + "&password=" + PASSWORD;
		try {
			Class.forName(DRIVER);
			return DriverManager.getConnection(URL);
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

	public static void fecharConexao(Connection conn, Statement statement, ResultSet resultSet) {
		fechar(conn, statement, resultSet);
	}

	public static void fecharConexao(Connection conn, Statement statement) {
		fechar(conn, statement, null);
	}

	public static void fecharConexao(Connection conn) {
		fechar(conn, null, null);
	}
}
