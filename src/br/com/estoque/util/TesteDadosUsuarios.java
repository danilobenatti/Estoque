package br.com.estoque.util;

import br.com.estoque.tabelas.Usuarios;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author danil
 */
public class TesteDadosUsuarios {

	public static void main(String[] args) {

		Usuarios usuario = new Usuarios();
		usuario.setLogin("José");
		usuario.setNivel(0);
		usuario.setSenha("123456");
		new Dados().insertUser(usuario);

		Usuarios u1 = new Usuarios("Carlos", 1, "987654");
		Usuarios u2 = new Usuarios("Maria", 1, "654321");

		ArrayList<Usuarios> usuarios = new ArrayList<>();
		usuarios.add(u1);
		usuarios.add(u2);
		usuarios.add(new Usuarios("Paulo", 1, "159753"));
		new Dados().insertAllUsers(usuarios);

		Usuarios u3 = new Usuarios("João", 1, "123abc");
		new Dados().insertUser(u2);

		u3.setId(5);
		u3.setNivel(0);
		u3.setSenha("999999");
		new Dados().updateUser(u3);

		List<String> data = Arrays.asList(new Dados().findUserById(5));
		if (!data.isEmpty()) {
			int size = data.size();
			int field = 1;
			for (String value : data) {
				System.out.print(value);
				if (field < size) {
					System.out.print(", ");
				}
				field++;
			}
			System.out.println(" <- Dados do usuário " + data.get(0));
		}

		String[][] listOfUsers = new Dados().listAllUsers(true);
		List<String[]> users = Arrays.asList(listOfUsers);
		for (String[] user : users) {
			System.out.println(Arrays.toString(user));
		}
		System.out.printf("Total de %d usuários%n", users.size());
	}
}
