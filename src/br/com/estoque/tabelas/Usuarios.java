package br.com.estoque.tabelas;

/**
 *
 * @author Romuel Dias de Oliveira - Java Full Teoria e Prática - pg.270
 */
public final class Usuarios {

	private Integer idUsuarios;
	private String login;
	private Integer nivel;
	private String senha;

	public Usuarios() {
	}

	public Usuarios(Integer idUsuarios) {
		setIdUsuarios(idUsuarios);
	}

	public Usuarios(String login) {
		setLogin(login);
	}

	public Usuarios(String login, Integer nivel, String senha) {
		setLogin(login);
		setNivel(nivel);
		setSenha(senha);
	}

	public Usuarios(Integer idUsuarios, String login, Integer nivel, String senha) {
		setIdUsuarios(idUsuarios);
		setLogin(login);
		setNivel(nivel);
		setSenha(senha);
	}

	public Integer getIdUsuarios() {
		if (this.idUsuarios == null) {
			setIdUsuarios(0);
		}
		return idUsuarios;
	}

	public void setIdUsuarios(Integer idUsuarios) {
		this.idUsuarios = idUsuarios;
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
