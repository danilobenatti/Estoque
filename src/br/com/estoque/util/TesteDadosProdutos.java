package br.com.estoque.util;

import br.com.estoque.tabelas.Produtos;
import java.util.ArrayList;

/**
 *
 * @author danil
 */
public class TesteDadosProdutos {

	public static void main(String[] args) {

		ArrayList<Produtos> produtos = new ArrayList<>();
		produtos.add(new Produtos("Leite Integral 1L", "cx", 4.80, "Leite Mumu"));
		produtos.add(new Produtos("Macarrão Espaguete", "pct", 1.95, "Massas Mamamia"));
		Dados dados = new Dados();
		dados.insertAllProducts(produtos);

	}
}
