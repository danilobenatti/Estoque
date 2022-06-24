package br.com.estoque.util;

import br.com.estoque.tabelas.Produtos;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.JOptionPane;

public class TesteDadosProdutos {

	public static void main(String[] args) {

		Produtos p1 = new Produtos("Leite Integral 1L", "cx", 4.80, "Leite Mumu");
		new Dados().insertProduct(p1);
		Produtos p2 = new Produtos("Requeijão Desnatado 250ml", "un", 3.70, "Sabor da fazenda");
		new Dados().insertProduct(p2);

		ArrayList<Produtos> produtos = new ArrayList<>();
		produtos.add(new Produtos("Macarrão Espaguete 500g", "pct", 1.95, "Massas Mamamia"));
		produtos.add(new Produtos("Vinagre Balsâmico 350ml", "lt", 5.30, "Tempêro e CIA"));
		produtos.add(new Produtos("Sal Grosso 250g", "pct", 2.50, "Salamar"));
		new Dados().insertAllProducts(produtos);

		p1.setId(1);
		p1.setDescricao("Leite Semidesnatado 1L");
		p1.setValorUnitario(2.95);
		new Dados().updateProduct(p1);

		Produtos p3 = new Produtos();
		p3.setDescricao("Sabão em pó");
		p3.setUnidade("800g");
		p3.setValorUnitario(5.80);
		p3.setObs("AMA Sabões");
		new Dados().insertProduct(p3);

		String id = JOptionPane.showInputDialog(
				"Informe um id de produto para ser excluído.",
				JOptionPane.INFORMATION_MESSAGE);
		if (id != null) {
			String[] options = {"sim", "não"}; //{0,1}
			Icon icon = null;
			int opcao = JOptionPane.showOptionDialog(null,
					"Deseja mesmo excluir o produto de ID=" + id + "?",
					"Excluir um produto", JOptionPane.DEFAULT_OPTION,
					JOptionPane.WARNING_MESSAGE, icon, options, options[0]);
			if (opcao == 0) {
				new Dados().deleteOneProductById(Integer.parseInt(id));
			}
		}
	}
}
