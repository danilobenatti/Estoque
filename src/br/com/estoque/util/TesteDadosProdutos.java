package br.com.estoque.util;

import br.com.estoque.tabelas.Produtos;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author danil
 */
public class TesteDadosProdutos {

	public static void main(String[] args) {


		Produtos p1 = new Produtos("Leite Integral 1L", "cx", 4.80, "Leite Mumu");
		new Dados().insertProduct(p1);

		ArrayList<Produtos> produtos = new ArrayList<>();
		produtos.add(new Produtos("Macarrão Espaguete", "pct", 1.95, "Massas Mamamia"));
		produtos.add(new Produtos("Vinagre Balsâmico", "lt", 5.30, "Tempêro e CIA"));
		produtos.add(new Produtos("Sal Grosso", "pct", 2.50, "Salamar"));
		new Dados().insertAllProducts(produtos);

		p1.setId(1);
		p1.setDescricao("Leite Semidesnatado 1L");
		p1.setValorUnitario(2.95);
		new Dados().updateProduct(p1);
		
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

		String[][] listOfProducts = new Dados().listAllProducts(true);
		List<String[]> products = Arrays.asList(listOfProducts);
		for (String[] product : products) {
			System.out.println(Arrays.toString(product));
		}
		System.out.printf("Total de %d produtos%n", products.size());
	}
}
