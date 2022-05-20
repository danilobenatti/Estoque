package br.com.estoque.util;

import br.com.estoque.tabelas.Produtos;

/**
 *
 * @author danil
 */
public class TesteDadosProdutos {

	public static void main(String[] args) {

		Produtos p1 = new Produtos("Leite Integral 1L", "cx", 4.80, "Leite Mumu");
		Dados dados = new Dados(p1);
		dados.create();

		dados = new Dados(new Produtos("Macarrão Espaguete", "pct", 1.95, "Massas Mamamia"));
		dados.create();
	}
}
