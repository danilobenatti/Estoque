package br.com.estoque.util;

import br.com.estoque.tabelas.Produtos;
import br.com.estoque.tabelas.Usuarios;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 *
 * @author Romuel Dias de Oliveira - Java Full Teoria e Prática - pg.277
 */
public class Dados {

	private final Connection connection;
	private Statement statement;
	private PreparedStatement preparedStatement;
	private ResultSet resultSet;
	private ResultSetMetaData resultSetMetaData;
	private String SQL_INSERT, SQL_UPDATE, SQL_DELETE, SQL_SELECT,
			SQL_SELECT_ONE, SQL_SELECT_TEXT_FULL, SQL_SELECT_TEXT_LIKE, ORDER;

	public Dados() {
		this.connection = JDBC.receberConexao();
	}

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
				+ "WHERE id =" + object.getId();
		SQL_DELETE = "DELETE FROM produtos WHERE id = " + object.getId();
		SQL_SELECT = "SELECT * FROM produtos";
		SQL_SELECT_ONE = "SELECT * FROM produtos WHERE id = " + object.getId();
		SQL_SELECT_TEXT_FULL = "SELECT * FROM produtos"
				+ " WHERE lower(descricao) = lower('" + object.getDescricao().trim() + "') ";
		SQL_SELECT_TEXT_LIKE = "SELECT * FROM produtos"
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
				+ "WHERE id =" + object.getId();
		SQL_DELETE = "DELETE FROM usuarios WHERE id = " + object.getId();
		SQL_SELECT = "SELECT * FROM usuarios";
		SQL_SELECT_ONE = "SELECT * FROM usuarios WHERE id = " + object.getId();
		SQL_SELECT_TEXT_FULL = "SELECT * FROM usuarios"
				+ " WHERE lower(login) = lower('" + object.getLogin().trim() + "') ";
		SQL_SELECT_TEXT_LIKE = "SELECT * FROM usuarios"
				+ " WHERE lower(login) LIKE lower('%" + object.getLogin().trim() + "%') ";
		ORDER = " ORDER BY login ";
	}

	public static void setValues(PreparedStatement ps, Object... values)
			throws SQLException {
		for (int i = 0; i < values.length; i++) {
			ps.setObject(i + 1, values[i]);
		}
	}

	public static void printSQLException(SQLException ex) {
		for (Throwable e : ex) {
			if (e instanceof SQLException) {
				e.printStackTrace(System.err);
				System.err.println("SQLState: " + ((SQLException) e).getSQLState());
				System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
				System.err.println("Message: " + e.getMessage());
				Throwable throwable = ex.getCause();
				while (throwable != null) {
					System.out.println("Cause: " + throwable);
					throwable = throwable.getCause();
				}
			}
		}
	}

	public void insertUser(Usuarios usuario) {
		String insert = "INSERT INTO usuarios(login, nivel, senha) VALUES (?, ?, ?)";
		try {
			preparedStatement = connection.prepareStatement(insert,
					Statement.RETURN_GENERATED_KEYS);
			setValues(preparedStatement, usuario.getLogin(), usuario.getNivel(),
					usuario.getSenha());
			preparedStatement.execute();
			resultSet = preparedStatement.getGeneratedKeys();
			resultSetMetaData = resultSet.getMetaData();
			while (resultSet.next()) {
				System.out.printf("%s: %s\n",
						resultSetMetaData.getColumnName(1), resultSet.getInt(1));
			}
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
	}

	public void insertAllUsers(List<Usuarios> usuarios) {
		String insert = "INSERT INTO usuarios(login, nivel, senha) VALUES (?, ?, ?)";
		try {
			preparedStatement = connection.prepareStatement(insert,
					Statement.RETURN_GENERATED_KEYS);
			for (Usuarios usuario : usuarios) {
				setValues(preparedStatement, usuario.getLogin(), usuario.getNivel(),
						usuario.getSenha());
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
			resultSet = preparedStatement.getGeneratedKeys();
			resultSetMetaData = resultSet.getMetaData();
			while (resultSet.next()) {
				System.out.printf("%s: %s\n",
						resultSetMetaData.getColumnName(1), resultSet.getInt(1));
			}
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
	}

	public void insertProduct(Produtos produto) {
		String insert = "INSERT INTO produtos "
				+ "(descricao, unidade, valorUnitario, obs) VALUES (?, ?, ?, ?)";
		try {
			preparedStatement = connection.prepareStatement(insert,
					Statement.RETURN_GENERATED_KEYS);
			setValues(preparedStatement, produto.getDescricao(), produto.getUnidade(),
					produto.getValorUnitario(), produto.getObs());
			preparedStatement.execute();
			resultSet = preparedStatement.getGeneratedKeys();
			resultSetMetaData = resultSet.getMetaData();
			while (resultSet.next()) {
				System.out.printf("%s: %s\n",
						resultSetMetaData.getColumnName(1), resultSet.getInt(1));
			}
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
	}

	public void insertAllProducts(List<Produtos> produtos) {
		String insert = "INSERT INTO produtos "
				+ "(descricao, unidade, valorUnitario, obs) VALUES (?, ?, ?, ?)";
		try {
			preparedStatement = connection.prepareStatement(insert,
					Statement.RETURN_GENERATED_KEYS);
			for (Produtos produto : produtos) {
				setValues(preparedStatement, produto.getDescricao(), produto.getUnidade(),
						produto.getValorUnitario(), produto.getObs());
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
			resultSet = preparedStatement.getGeneratedKeys();
			resultSetMetaData = resultSet.getMetaData();
			while (resultSet.next()) {
				System.out.printf("%s: %s\n",
						resultSetMetaData.getColumnName(1), resultSet.getInt(1));
			}
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
	}

	public void updateUser(Usuarios usuario) {
		String update = "UPDATE usuarios SET "
				+ "login = ?, nivel = ?, senha = ? WHERE id = ?";
		try {
			preparedStatement = connection.prepareStatement(update);
			setValues(preparedStatement, usuario.getLogin(), usuario.getNivel(),
					usuario.getSenha(), usuario.getId());
			preparedStatement.executeUpdate();
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement);
		}
	}

	public void updateProduct(Produtos produto) {
		String update = "UPDATE produtos SET "
				+ "descricao = ?, unidade = ?, valorUnitario = ?, "
				+ "quantidade = ?, obs = ? WHERE id = ?";
		try {
			preparedStatement = connection.prepareStatement(update);
			setValues(preparedStatement, produto.getDescricao(), produto.getUnidade(),
					produto.getValorUnitario(), produto.getQuantidade(),
					produto.getObs(), produto.getId());
			preparedStatement.executeUpdate();
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement);
		}
	}

	public String[][] listAllUsers(boolean ordemAlfabetica) {
		String select = "SELECT * FROM usuarios ", order = "ORDER BY login";
		if (ordemAlfabetica) {
			select = select + order;
		}
		String[][] dadosRetorno = null;
		try {
			preparedStatement = connection.prepareStatement(select,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			resultSet = preparedStatement.executeQuery();
			resultSet.last();
			resultSetMetaData = resultSet.getMetaData();
			final int rows = resultSet.getRow();
			final int columns = resultSetMetaData.getColumnCount();
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
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
		return dadosRetorno;
	}

	public String[][] listAllProducts(boolean ordemAlfabetica) {
		String select = "SELECT * FROM produtos ", order = "ORDER BY descricao";
		if (ordemAlfabetica) {
			select = select + order;
		}
		String[][] dadosRetorno = null;
		try {
			preparedStatement = connection.prepareStatement(select,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			resultSet = preparedStatement.executeQuery();
			resultSet.last();
			resultSetMetaData = resultSet.getMetaData();
			final int rows = resultSet.getRow();
			final int columns = resultSetMetaData.getColumnCount();
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
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
		return dadosRetorno;
	}

	public String[] findUserById(int id) {
		String select = "SELECT * FROM usuarios WHERE id = ?";
		String[] dadosRetorno = null;
		try {
			preparedStatement = connection.prepareStatement(select,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			resultSetMetaData = resultSet.getMetaData();
			final int columns = resultSetMetaData.getColumnCount();
			dadosRetorno = new String[columns];
			if (resultSet.first()) {
				for (int i = 0; i < columns; i++) {
					dadosRetorno[i] = resultSet.getString(i + 1);
				}
			}
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
		return dadosRetorno;
	}

	public String[] findUserByLogin(String login) {
		String select = "SELECT * FROM usuarios WHERE lower(login) = lower(?)";
		String[] dadosRetorno = null;
		try {
			preparedStatement = connection.prepareStatement(select,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			preparedStatement.setString(1, login);
			resultSet = preparedStatement.executeQuery();
			resultSetMetaData = resultSet.getMetaData();
			final int columns = resultSetMetaData.getColumnCount();
			dadosRetorno = new String[columns];
			if (resultSet.first()) {
				for (int i = 0; i < columns; i++) {
					dadosRetorno[i] = resultSet.getString(i + 1);
				}
			}
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
		return dadosRetorno;
	}

	public String[][] findUserByLoginLike(boolean ordemAlfabetica, String login) {
		String select = "SELECT * FROM usuarios WHERE lower(login) LIKE lower(?)",
				order = "ORDER BY login";
		if (ordemAlfabetica) {
			select = select + order;
		}
		String[][] dadosRetorno = null;
		try {
			preparedStatement = connection.prepareStatement(select,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			preparedStatement.setString(1, "%" + login + "%");
			resultSet = preparedStatement.executeQuery();
			resultSet.last();
			resultSetMetaData = resultSet.getMetaData();
			final int rows = resultSet.getRow();
			final int columns = resultSetMetaData.getColumnCount();
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
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
		return dadosRetorno;
	}

	public String[] findProductById(int id) {
		String select = "SELECT * FROM produtos WHERE id = ?";
		String[] dadosRetorno = null;
		try {
			preparedStatement = connection.prepareStatement(select,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			preparedStatement.setInt(1, id);
			resultSet = preparedStatement.executeQuery();
			resultSetMetaData = resultSet.getMetaData();
			final int columns = resultSetMetaData.getColumnCount();
			dadosRetorno = new String[columns];
			if (resultSet.first()) {
				for (int i = 0; i < columns; i++) {
					dadosRetorno[i] = resultSet.getString(i + 1);
				}
			}
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
		return dadosRetorno;
	}

	public String[] findProductByDescription(String description) {
		String select = "SELECT * FROM produtos WHERE lower(descricao) = lower(?)";
		String[] dadosRetorno = null;
		try {
			preparedStatement = connection.prepareStatement(select,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			preparedStatement.setString(1, description);
			resultSet = preparedStatement.executeQuery();
			resultSetMetaData = resultSet.getMetaData();
			final int columns = resultSetMetaData.getColumnCount();
			dadosRetorno = new String[columns];
			if (resultSet.first()) {
				for (int i = 0; i < columns; i++) {
					dadosRetorno[i] = resultSet.getString(i + 1);
				}
			}
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
		return dadosRetorno;
	}

	public String[][] findProductByDecriptionLike(boolean ordemAlfabetica, String description) {
		String select = "SELECT * FROM produtos WHERE lower(descricao) LIKE lower(?) ",
				order = "ORDER BY descricao";
		if (ordemAlfabetica) {
			select = select + order;
		}
		String[][] dadosRetorno = null;
		try {
			preparedStatement = connection.prepareStatement(select,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_UPDATABLE);
			preparedStatement.setString(1, "%" + description + "%");
			resultSet = preparedStatement.executeQuery();
			resultSet.last();
			resultSetMetaData = resultSet.getMetaData();
			final int rows = resultSet.getRow();
			final int columns = resultSetMetaData.getColumnCount();
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
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
		return dadosRetorno;
	}

	public void deleteOneProductById(int id) {
		String delete = "DELETE FROM produtos WHERE id = ?";
		try {
			preparedStatement = connection.prepareStatement(delete);
			preparedStatement.setInt(1, id);
			preparedStatement.executeUpdate();
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement);
		}
	}

	public void deleteOneUserById(int id) {
		String delete = "DELETE FROM usuarios WHERE id = ?";
		try {
			preparedStatement = connection.prepareStatement(delete);
			preparedStatement.setInt(1, id);
			preparedStatement.executeUpdate();
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement);
		}
	}

	public Integer nextIdUser() {
		String select = "SELECT MAX(id) AS id FROM usuarios";
		int newCode = 0;
		try {
			preparedStatement = connection.prepareStatement(select);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next() && resultSet != null) {
				newCode = resultSet.getInt("id") + 1;
			}
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
		return newCode;
	}

	public Integer nextIdProduct() {
		String select = "SELECT MAX(id) AS id FROM produtos";
		int newCode = 0;
		try {
			preparedStatement = connection.prepareStatement(select);
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next() && resultSet != null) {
				newCode = resultSet.getInt("id") + 1;
			}
		} catch (SQLException ex) {
			printSQLException(ex);
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getMessage());
		} finally {
			JDBC.fecharConexao(connection, preparedStatement, resultSet);
		}
		return newCode;
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
			resultSet.last();
			resultSetMetaData = resultSet.getMetaData();
			final int rows = resultSet.getRow();
			final int columns = resultSetMetaData.getColumnCount();
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
			final int columns = resultSetMetaData.getColumnCount();
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
			final int columns = resultSetMetaData.getColumnCount();
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
			final int rows = resultSet.getRow();
			final int columns = resultSetMetaData.getColumnCount();
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
