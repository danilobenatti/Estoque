package br.com.estoque.tabelas;

/**
 *
 * @author Romuel Dias de Oliveira - Java Full Teoria e Prática - pg.272
 */
public final class Produtos {

	private Integer idProdutos;
	private String descricao;
	private String unidade;
	private Double valorUnitario;
	private Integer quantidade;
	private String obs;

	public Produtos() {
	}

	public Produtos(Integer idProdutos) {
		setIdProdutos(idProdutos);
	}

	public Produtos(String descricao) {
		setDescricao(descricao);
	}

	public Produtos(String descricao, String unidade, Double valorUnitario, String obs) {
		setDescricao(descricao);
		setUnidade(unidade);
		setValorUnitario(valorUnitario);
		setObs(obs);
	}

	public Produtos(Integer idProdutos, String descricao, Double valorUnitario, Integer quantidade, String obs) {
		setIdProdutos(idProdutos);
		setDescricao(descricao);
		setValorUnitario(valorUnitario);
		setObs(obs);
	}

	public Produtos(Integer idProdutos, String descricao, String unidade, Double valorUnitario, Integer quantidade, String obs) {
		setIdProdutos(idProdutos);
		setDescricao(descricao);
		setUnidade(unidade);
		setValorUnitario(valorUnitario);
		setQuantidade(quantidade);
		setObs(obs);
	}

	public Integer getIdProdutos() {
		if (this.idProdutos == null) {
			setIdProdutos(0);
		}
		return this.idProdutos;
	}

	public void setIdProdutos(Integer idProdutos) {
		if (this.idProdutos == null || this.idProdutos < 0) {
			this.idProdutos = 0;
		}
		this.idProdutos = idProdutos;
	}

	public String getDescricao() {
		if (this.descricao == null) {
			setDescricao("");
		}
		return this.descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getUnidade() {
		if (this.unidade == null) {
			setUnidade("");
		}
		return this.unidade;
	}

	public void setUnidade(String unidade) {
		this.unidade = unidade;
	}

	public Double getValorUnitario() {
		if (this.valorUnitario == null) {
			setValorUnitario(0.00);
		}
		return this.valorUnitario;
	}

	public void setValorUnitario(Double valorUnitario) {
		if (this.valorUnitario == null || this.valorUnitario < 0) {
			this.valorUnitario = 0.00;
		}
		this.valorUnitario = valorUnitario;
	}

	public Integer getQuantidade() {
		if (this.quantidade == null) {
			setQuantidade(0);
		}
		return this.quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		if (this.quantidade == null || this.quantidade < 0) {
			this.quantidade = 0;
		} else {
			this.quantidade = quantidade;
		}
	}

	public String getObs() {
		if (this.obs == null) {
			setObs("");
		}
		return this.obs;
	}

	public void setObs(String obs) {
		this.obs = obs;
	}
}
