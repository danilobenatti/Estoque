package br.com.estoque.util;

import br.com.estoque.tabelas.Usuarios;
import java.util.ArrayList;

/**
 *
 * @author danil
 */
public class TesteDadosUsuarios {

	public static void main(String[] args) {

		Dados dados = new Dados();
		
		Usuarios u1 = new Usuarios();
		u1.setLogin("José");
		u1.setNivel(0);
		u1.setSenha("123456");

		ArrayList<Usuarios> usuarios = new ArrayList<>();
		usuarios.add(u1);
		usuarios.add(new Usuarios("Maria", 1, "654321"));
		dados.insertAllUsers(usuarios);

		dados = new Dados(new Usuarios("João", 1, "123abc"));
		dados.create();
	}
}
