package br.com.estoque.util;

import br.com.estoque.tabelas.Usuarios;
import java.util.Arrays;
import java.util.List;

public class TesteConsultaDadosUsuarios {

	public static void main(String[] args) {

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

		List<String> searchUser = Arrays.asList(
				new Dados().findUserByLogin("João"));
		List<String[]> searchUserLike = Arrays.asList(
				new Dados().findUserByLoginLike(false, "j"));
		if (!searchUser.isEmpty()) {
			System.out.println("===== Exemplo 1 =====");
			for (int i = 0; i < searchUser.size(); i++) {
				System.out.println(searchUser.get(i));
			}
			System.out.println("===== Exemplo 2 =====");
			for (String value : searchUser) {
				System.out.println(value);
			}
			System.out.println("===== Exemplo 3 Search User =====");
			searchUser.forEach(value -> System.out.println(value));
			System.out.println("===== Exemplo 4 Search User Like =====");
			searchUserLike.forEach(value -> System.out.println(Arrays.toString(value)));
		}

		System.out.println("===== Exemplo 5 Exigir todos =====");
		String[][] listOfUsers = new Dados().listAllUsers(true);
		List<String[]> users = Arrays.asList(listOfUsers);
		for (String[] user : users) {
			System.out.println(Arrays.toString(user));
		}
		System.out.printf("Total de %d usuários%n", users.size());
		System.out.println("Próximo id " + new Dados().nextIdUser());
	}
}
