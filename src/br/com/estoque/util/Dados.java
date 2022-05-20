package br.com.estoque.util;

import br.com.estoque.tabelas.Produtos;
import br.com.estoque.tabelas.Usuarios;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Romuel Dias de Oliveira - Java Full Teoria e Prática - pg.277
 */
public class Dados {

	private final Connection connection;
	private Statement statement;
	private ResultSet resultSet;
	private ResultSetMetaData resultSetMetaData;
	private static String SQL_INSERT, SQL_UPDATE, SQL_DELETE, SQL_SELECT,
			SQL_SELECT_ONE, SQL_SELECT_TEXT_FULL, SQL_SELECT_TEXT_LIKE, ORDER;

	public Dados(Produtos object) {
		this.connection = JDBC.receberConexao();
		SQL_INSERT = "INSERT INTO produtos ("
				+ "descricao, unidade, valorUnitario, obs) VALUES ('"
				+ object.getDescricao() + "', '"
				+ object.getUnidade() + "', "
				+ object.getValorUnitario() + ", '"
				+ object.getObs() + "') ";
		SQL_UPDATE = "UPDATE produtos SET "
				+ "descricao ='" + object.getDescricao() + "', "
				+ "unidade ='" + object.getUnidade() + "', "
				+ "valorUnitario =" + object.getValorUnitario() + ", "
				+ "quantidade =" + object.getQuantidade() + ", "
				+ "obs ='" + object.getObs() + "' "
				+ " WHERE id =" + object.getId();
		SQL_DELETE = "DELETE FROM produtos WHERE id = " + object.getId();
		SQL_SELECT = "SELECT * FROM produtos ";
		SQL_SELECT_ONE = "SELECT * FROM produtos WHERE id = " + object.getId();
		SQL_SELECT_TEXT_FULL = "SELECT * FROM produtos "
				+ " WHERE lower(descricao) = lower('" + object.getDescricao().trim() + "') ";
		SQL_SELECT_TEXT_LIKE = "SELECT * FROM produtos "
				+ " WHERE lower(descricao) LIKE lower('%" + object.getDescricao().trim() + "%') ";
		ORDER = " ORDER BY descricao ";
	}

	public Dados(Usuarios object) {
		this.connection = JDBC.receberConexao();
		SQL_INSERT = "INSERT INTO usuarios ("
				+ "login, nivel, senha) VALUES ('"
				+ object.getLogin() + "', '"
				+ object.getNivel() + "', '"
				+ object.getSenha() + "') ";
		SQL_UPDATE = "UPDATE usuarios SET "
				+ "login ='" + object.getLogin() + "', "
				+ "nivel =" + object.getNivel() + ", "
				+ "senha ='" + object.getSenha() + "' "
				+ " WHERE id =" + object.getId();
		SQL_DELETE = "DELETE FROM usuarios WHERE id = " + object.getId();
		SQL_SELECT = "SELECT * FROM usuarios ";
		SQL_SELECT_ONE = "SELECT * FROM usuarios WHERE id = " + object.getId();
		SQL_SELECT_TEXT_FULL = "SELECT * FROM usuarios "
				+ " WHERE lower(login) = lower('" + object.getLogin().trim() + "') ";
		SQL_SELECT_TEXT_LIKE = "SELECT * FROM usuarios "
				+ " WHERE lower(login) LIKE lower('%" + object.getLogin().trim() + "%') ";
		ORDER = " ORDER BY login ";
	}

	public void create() {
		try {
			statement = connection.createStatement();
			statement.executeUpdate(SQL_INSERT);
		} catch (SQLException e) {
			e.getMessage();
		} finally {
			JDBC.fecharConexao(connection, statement);
		}
	}

	public void update() {
		try {
			statement = connection.createStatement();
			statement.executeUpdate(SQL_UPDATE);
		} catch (SQLException e) {
			e.getMessage();
		} finally {
			JDBC.fecharConexao(connection, statement);
		}
	}

	public void delete() {
		try {
			statement = connection.createStatement();
			statement.executeUpdate(SQL_DELETE);
		} catch (SQLException e) {
			e.getMessage();
		} finally {
			JDBC.fecharConexao(connection, statement);
		}
	}

	public String[][] listAll(boolean ordemAlfabetica) {
		if (ordemAlfabetica) {
			SQL_SELECT = SQL_SELECT + ORDER;
		}
		String[][] dadosRetorno = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(SQL_SELECT);
			resultSetMetaData = resultSet.getMetaData();
			resultSet.last();
			int rows = resultSet.getRow();
			int columns = resultSetMetaData.getColumnCount();
			dadosRetorno = new String[rows][columns];
			if (resultSet.first()) {
				int i = 0;
				do {
					for (int j = 0; j < columns; j++) {
						dadosRetorno[i][j] = resultSet.getString(j + 1);
					}
					i++;
				} while (resultSet.next());
			}
		} catch (SQLException e) {
			e.getMessage();
		} finally {
			JDBC.fecharConexao(connection, statement, resultSet);
		}
		return dadosRetorno;
	}

	public String[] findOne() {
		String[] dadosRetorno = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(SQL_SELECT_ONE);
			resultSetMetaData = resultSet.getMetaData();
			int columns = resultSetMetaData.getColumnCount();
			dadosRetorno = new String[columns];
			if (resultSet.first()) {
				for (int i = 0; i < columns; i++) {
					dadosRetorno[i] = resultSet.getString(i + 1);
				}
			}
		} catch (SQLException e) {
			e.getMessage();
		} finally {
			JDBC.fecharConexao(connection, statement, resultSet);
		}
		return dadosRetorno;
	}

	public String[] findByTextFull() {
		String[] dadosRetorno = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(SQL_SELECT_TEXT_FULL);
			resultSetMetaData = resultSet.getMetaData();
			int columns = resultSetMetaData.getColumnCount();
			dadosRetorno = new String[columns];
			if (resultSet.first()) {
				for (int i = 0; i < columns; i++) {
					dadosRetorno[i] = resultSet.getString(i + 1);
				}
			}
		} catch (SQLException e) {
			e.getMessage();
		} finally {
			JDBC.fecharConexao(connection, statement, resultSet);
		}
		return dadosRetorno;
	}

	public String[][] findByTextLike(boolean ordemAlfabetica) {
		if (ordemAlfabetica) {
			SQL_SELECT_TEXT_LIKE = SQL_SELECT_TEXT_LIKE + ORDER;
		}
		String[][] dadosRetorno = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(SQL_SELECT_TEXT_LIKE);
			resultSetMetaData = resultSet.getMetaData();
			resultSet.last();
			int rows = resultSet.getRow();
			int columns = resultSetMetaData.getColumnCount();
			dadosRetorno = new String[rows][columns];
			if (resultSet.first()) {
				int i = 0;
				do {
					for (int j = 0; j < columns; j++) {
						dadosRetorno[i][j] = resultSet.getString(j + 1);
					}
					i++;
				} while (resultSet.next());
			}
		} catch (SQLException e) {
			e.getMessage();
		} finally {
			JDBC.fecharConexao(connection, statement, resultSet);
		}
		return dadosRetorno;
	}

	public String novoCodigo() {
		String novoCodigo = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(SQL_SELECT);
			resultSet.last();
			novoCodigo = String.format("%04d", Integer.parseInt(resultSet.getString(1)) + 1);
			if (novoCodigo == null) {
				novoCodigo = "0";
			}
		} catch (SQLException e) {
			e.getMessage();
		} finally {
			JDBC.fecharConexao(connection, statement, resultSet);
		}
		return novoCodigo;
	}
}
