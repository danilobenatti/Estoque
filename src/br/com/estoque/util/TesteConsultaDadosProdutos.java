package br.com.estoque.util;

import br.com.estoque.tabelas.Produtos;
import java.util.Arrays;
import java.util.List;

public class TesteConsultaDadosProdutos {

	public static void main(String[] args) {

		List<String> data = Arrays.asList(new Dados().findProductById(1));
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
			System.out.println(" <- Dados do produto " + data.get(0));
		}

		List<String> searchProduct = Arrays.asList(
				new Dados().findProductByDescription("Leite Semidesnatado 1L"));
		List<String[]> searchProductLike = Arrays.asList(
				new Dados().findProductByDecriptionLike(true, "desnatado"));
		if (!searchProduct.isEmpty() || !searchProduct.isEmpty()) {
			System.out.println("===== Exemplo 1 =====");
			for (int i = 0; i < searchProduct.size(); i++) {
				System.out.println(searchProduct.get(i));
			}
			System.out.println("===== Exemplo 2 =====");
			for (String value : searchProduct) {
				System.out.println(value);
			}
			System.out.println("===== Exemplo 3 =====");
			searchProduct.forEach(value -> System.out.println(value));
			System.out.println("===== Exemplo 4 =====");
			searchProductLike.forEach(value -> System.out.println(Arrays.toString(value)));
		}

		System.out.println("===== Exemplo 5 Exigir todos =====");
		String[][] listOfProducts = new Dados().listAllProducts(true);
		List<String[]> products = Arrays.asList(listOfProducts);
		for (String[] product : products) {
			System.out.println(Arrays.toString(product));
		}
		System.out.printf("Total de %d produtos%n", products.size());
		System.out.println("Próximo id " + new Dados().nextIdProduct());
	}
}
