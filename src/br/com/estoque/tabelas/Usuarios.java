package br.com.estoque.tabelas;

/**
 *
 * @author Romuel Dias de Oliveira - Java Full Teoria e Prática - pg.270
 */
public final class Usuarios {

	private Integer id;
	private String login;
	private Integer nivel;
	private String senha;

	public Usuarios() {
	}

	public Usuarios(Integer id) {
		setId(id);
	}

	public Usuarios(String login) {
		setLogin(login);
	}

	public Usuarios(String login, Integer nivel, String senha) {
		setLogin(login);
		setNivel(nivel);
		setSenha(senha);
	}

	public Usuarios(Integer id, String login, Integer nivel, String senha) {
		setId(id);
		setLogin(login);
		setNivel(nivel);
		setSenha(senha);
	}

	public Integer getId() {
		if (this.id == null) {
			setId(0);
		}
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLogin() {
		if (this.login == null) {
			setLogin("");
		}
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public Integer getNivel() {
		return nivel;
	}

	public void setNivel(Integer nivel) {
		this.nivel = nivel;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}
}
