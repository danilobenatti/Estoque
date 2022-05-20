package br.com.estoque.util;

import br.com.estoque.tabelas.Usuarios;

/**
 *
 * @author danil
 */
public class TesteDadosUsuarios {

	public static void main(String[] args) {

		Usuarios u1 = new Usuarios();
		u1.setLogin("José");
		u1.setNivel(0);
		u1.setSenha("123456");
		Dados dados = new Dados(u1);
		dados.create();

		Usuarios u2 = new Usuarios("Maria", 1, "654321");
		dados = new Dados(u2);
		dados.create();

		dados = new Dados(new Usuarios("João", 1, "123abc"));
		dados.create();
	}
}
